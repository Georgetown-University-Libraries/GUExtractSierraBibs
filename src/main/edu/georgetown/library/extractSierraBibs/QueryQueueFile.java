package edu.georgetown.library.extractSierraBibs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;

public class QueryQueueFile {
	QUERY_TYPE queryType;
	boolean resume;
	int maxBib;
	int maxTime;
	Date end;
	int numDays;
	ExtractStats extractStats;
	ApiConfigFile apiConfig;
	QueueFolder status = QueueFolder.Running;
	File myPath;
	QueryOutputFiles queryOutputFiles;
	
	public static final String P_QT = "queryType";
	public static final String P_END = "endDate";
	public static final String P_DAYS = "numDays";
	
	public File createFile(QueueFolder status, File existing) {
		File dir = status.getDir(apiConfig);
		StringBuffer fname = new StringBuffer();
		fname.append(queryType.name());
		fname.append(".");
		fname.append(YYYYMMDD.format(end));
		File f = new File(dir, fname.toString() + ".txt");
		if (status == QueueFolder.Complete) {
			for(int i=1; f.exists(); i++) {
				f = new File(dir,fname.toString()+"_"+i+".txt");
			}
		}
		if (existing != null) {
			try {
				Files.move(existing.toPath(), f.toPath(), StandardCopyOption.ATOMIC_MOVE);
			} catch (IOException e) {
				System.err.println("Rename failed from "+existing.getAbsolutePath() + " TO " + f.getAbsolutePath());
				e.printStackTrace();
				System.exit(1);
			}
		}
		return f;
	}
	
	static SimpleDateFormat MMDDYY = new SimpleDateFormat("MMddyy");
	static SimpleDateFormat YYYYMMDD = new SimpleDateFormat("yyyyMMdd");
		
	public QueryQueueFile(ApiConfigFile apiConfig, CommandLine cl) throws IllegalArgumentException, NumberFormatException, ParseException, IIIExtractException, FileNotFoundException, IOException {
		this.apiConfig = apiConfig;
		String qt = cl.getOptionValue(CommandLineOptions.O_Q.getOpt(), ""); 
		resume = cl.hasOption(CommandLineOptions.O_RESUME.getOpt());
		
		queryType = QUERY_TYPE.valueOf(qt);
		maxBib = Integer.parseInt(cl.getOptionValue(CommandLineOptions.O_MAXBIB.getOpt(), "0"));
		maxTime = Integer.parseInt(cl.getOptionValue(CommandLineOptions.O_MAXTIME.getOpt(), "0"));

		if (QueueFolder.Running.fileExists(apiConfig)) {
			throw new IIIExtractException("A process is already running: " + QueueFolder.Running.getFile(apiConfig, null).getAbsolutePath());
		}
		if (resume) {
			myPath = QueueFolder.Resume.getFile(apiConfig, queryType);
		}
		Properties prop = new Properties();
		Date now = Calendar.getInstance().getTime();
		if (myPath != null) {
			FileReader fr = new FileReader(myPath);
			prop.load(fr);
			fr.close();
			queryType = QUERY_TYPE.valueOf(prop.getProperty(P_QT));

			numDays = Integer.parseInt(prop.getProperty(P_DAYS, "1"));
			String endDate = prop.getProperty(P_END, YYYYMMDD.format(now));
			end = YYYYMMDD.parse(endDate);			
		} else {
			numDays = Integer.parseInt(cl.getOptionValue(CommandLineOptions.O_DAYS.getOpt(), "1"));
			
			String endDate = cl.getOptionValue(CommandLineOptions.O_END.getOpt(), YYYYMMDD.format(now));
			end = YYYYMMDD.parse(endDate);			
		}

		extractStats = new ExtractStats(maxBib, maxTime, prop);
		queryOutputFiles = new QueryOutputFiles(queryType, end, prop);
		
		myPath = createFile(QueueFolder.Running, myPath);
		save();
	}
	
	public void save() throws FileNotFoundException, IOException {
		Properties prop = new Properties();
		StringBuffer buf = new StringBuffer();
		buf.append("Queue file for Extract Sierra Bibs: https://github.com/Georgetown-University-Libraries/GUExtractSierraBibs");

		prop.setProperty(P_QT, queryType.name());
		prop.setProperty(P_END, YYYYMMDD.format(end));
		prop.setProperty(P_DAYS, ""+numDays);
		extractStats.updateProperties(prop);
		queryOutputFiles.updateProperties(prop);
		
		FileWriter fw = new FileWriter(myPath); 
		prop.store(fw, buf.toString());
		fw.close();
		System.out.println("RESULTS SAVED TO: " + myPath.getAbsolutePath());
	}
	
	public void complete(boolean resume) throws FileNotFoundException, IOException {
		myPath = createFile(resume ? QueueFolder.Resume : QueueFolder.Complete, myPath);
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
