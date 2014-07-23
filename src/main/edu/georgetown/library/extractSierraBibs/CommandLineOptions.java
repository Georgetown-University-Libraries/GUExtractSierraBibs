package edu.georgetown.library.extractSierraBibs;

import java.util.Date;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

public class CommandLineOptions {
	public static final Option O_Q = new Option("q", true, "Query type: ALL|ADDED|UPDATED|DELETED|ADDED_OR_UPDATED|DAILY");
	public static final Option O_START = new Option("start", false, "Initiate a new query");
	public static final Option O_RESUME = new Option("resume", false, "Resume an already running query");
	public static final Option O_END = new Option("end", true, "End date for current query in YYYYMMDD format (not applicable for ALL). Default: current date at 12:00AM");
	public static final Option O_DAYS = new Option("days", true, "Integer, used to calculate start date for current query (not applicable for ALL). Default: 1");
	public static final Option O_MAXBIB= new Option("maxBibs", true, "Maximum number of bibs to process (for testing). If more bibs are left to process, a resume file will be generated.");
	public static final Option O_MAXTIME= new Option("maxMin", true, "Number of minutes to run the process.  If the process does not complete in time, a resume file will be generated");
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
}
