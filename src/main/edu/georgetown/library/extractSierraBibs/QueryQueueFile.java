package edu.georgetown.library.extractSierraBibs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

public class QueryQueueFile {
	QUERY_TYPE queryType;
	boolean resume;
	int maxBib;
	int maxTime;
	Date end;
	int numDays;
	ExtractStats extractStats;
	ApiConfigFile apiConfig;
	Status status = Status.Running;
	File myPath;
	QueryOutputFiles queryOutputFiles;
	
	enum Status {
		Running("running"), 
		Resume("resume"), 
		Complete("complete");
		
		String path;
		Status(String path) {
			this.path = path;
		}
		
		public File getDir(ApiConfigFile apiConfig) {
			return new File(apiConfig.dirRoot, path);
		}
		
		public boolean fileExists(ApiConfigFile apiConfig) {
			File f = getDir(apiConfig);
			return f.listFiles().length > 0;
		}
		
		class QtFilenameFilter implements FilenameFilter {
			QUERY_TYPE qt;
			QtFilenameFilter(QUERY_TYPE qt) {
				this.qt = qt;
			}
			public boolean accept(File dir, String name) {
				return name.startsWith(qt.name());
			}
		}
		
		public File getFile(ApiConfigFile apiConfig, QUERY_TYPE qt) {
			File f = getDir(apiConfig);
			File[] files = f.listFiles(new QtFilenameFilter(qt));
			return files.length > 0 ? files[0] : null;
		}
	}

	public File createFile(Status status, File existing) {
		File dir = status.getDir(apiConfig);
		StringBuffer fname = new StringBuffer();
		fname.append(queryType.name());
		fname.append(".");
		fname.append(YYYYMMDD.format(end));
		fname.append(".txt");
		File f = new File(dir, fname.toString());
		if (existing != null) {
			existing.renameTo(f);
		}
		return f;
	}
	
	static SimpleDateFormat MMDDYY = new SimpleDateFormat("MMddyy");
	static SimpleDateFormat YYYYMMDD = new SimpleDateFormat("yyyyMMdd");
	public static final Option O_Q = new Option("q", true, "Query type: ALL|ADDED|UPDATED|DELETED|ADDED_OR_UPDATED|DAILY");
	public static final Option O_START = new Option("start", false, "Initiate a new query");
	public static final Option O_RESUME = new Option("resume", false, "Resume an already running query");
	public static final Option O_END = new Option("end", true, "End date for current query in YYYYMMDD format (not applicable for ALL). Default: current date at 12:00AM");
	public static final Option O_DAYS = new Option("days", true, "Integer, used to calculate start date for current query (not applicable for ALL). Default: 1");
	public static final Option O_MAXBIB= new Option("maxBibs", true, "Maximum number of bibs to process (for testing). If more bibs are left to process, a resume file will be generated.");
	public static final Option O_MAXTIME= new Option("maxTime", true, "Number of minutes to run the process.  If the process does not complete in time, a resume file will be generated");
	public static final Option O_CONFIG = new Option("config", true, "Configuration file.");
	static {
		O_Q.setRequired(true);
		O_CONFIG.setRequired(true);
		
		O_END.setType(Date.class);
		O_DAYS.setType(Integer.class);
		O_MAXBIB.setType(Integer.class);
		O_MAXTIME.setType(Integer.class);
	}
	
	public static Options getOptions() {
		Options opts = new Options();
		opts.addOption(O_Q);

		OptionGroup og = new OptionGroup();
		og.addOption(O_START);
		og.addOption(O_RESUME);
		opts.addOptionGroup(og);
		
		opts.addOption(O_END);
		opts.addOption(O_DAYS);
		
		opts.addOption(O_MAXBIB);
		opts.addOption(O_MAXTIME);
		opts.addOption(O_CONFIG);
		return opts;
	}

		
	public QueryQueueFile(ApiConfigFile apiConfig, CommandLine cl) throws IllegalArgumentException, NumberFormatException, ParseException, IIIExtractException, FileNotFoundException, IOException {
		this.apiConfig = apiConfig;
		String qt = cl.getOptionValue(O_Q.getOpt(), ""); 
		resume = cl.hasOption(O_RESUME.getOpt());
		
		queryType = QUERY_TYPE.valueOf(qt);
		maxBib = Integer.parseInt(cl.getOptionValue(O_MAXBIB.getOpt(), "0"));
		maxTime = Integer.parseInt(cl.getOptionValue(O_MAXTIME.getOpt(), "0"));

		if (Status.Running.fileExists(apiConfig)) {
			throw new IIIExtractException("A process is already running");
		}
		if (resume) {
			myPath = Status.Resume.getFile(apiConfig, queryType);
		}
		Properties prop = new Properties();
		if (myPath != null) {
			prop.load(new FileReader(myPath));
			queryType = QUERY_TYPE.valueOf(prop.getProperty("QueryType"));
		} else {
			numDays = Integer.parseInt(cl.getOptionValue(O_DAYS.getOpt(), "1"));
			
			Date now = Calendar.getInstance().getTime();
			String endDate = cl.getOptionValue(O_END.getOpt(), YYYYMMDD.format(now));
			end = YYYYMMDD.parse(endDate);			
		}

		extractStats = new ExtractStats(maxBib, maxTime, prop);
		queryOutputFiles = new QueryOutputFiles(queryType, end, prop);
		
		myPath = createFile(Status.Running, myPath);
		save();
	}
	
	public void save() throws FileNotFoundException, IOException {
		Properties prop = new Properties();
		StringBuffer buf = new StringBuffer();
		
		prop.store(new FileWriter(myPath), buf.toString());
	}
	
	public void complete(boolean resume) throws FileNotFoundException, IOException {
		myPath = createFile(resume ? Status.Resume : Status.Complete, myPath);
		save();
	}

	public String getBibQuery(int limit, int lastId, String filter) {
		StringBuffer buf = new StringBuffer();
		buf.append("bibs?limit=");
		buf.append(limit);
		buf.append("&id=[" + (lastId+1) + ",]");
		buf.append("&fields=id,varFields,fixedFields");
		buf.append(filter);
		if (GUExtractSierraBibs.isInfo()) System.out.println(buf.toString());
		return buf.toString();
	}

	public String getBibQuery() {
		return getBibQuery(extractStats.getBibLimit(), extractStats.lastId, queryType.getQuery(end, numDays));
	}

    public String getItemQuery(int limit, int offset, String bibIds) {
		StringBuffer ibuf = new StringBuffer();
		ibuf.append("items?limit=");
		ibuf.append(limit);
		ibuf.append("&offset=");
		ibuf.append(offset);
		ibuf.append("&bibIds=");
		ibuf.append(bibIds.toString());
		ibuf.append("&fields=default,varFields,fixedFields");
		if (GUExtractSierraBibs.isInfo()) System.out.println(ibuf.toString());
		return ibuf.toString();
	}
    
    public String getItemQuery(ExtractItemStats extractItemStats) {
    	return getItemQuery(extractStats.reqSize, extractItemStats.offset, extractItemStats.bibIds);
    }

}
