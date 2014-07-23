package edu.georgetown.library.extractSierraBibs;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.TreeMap;

class ExtractStats {
	int lastId = -1;
	int totalBibs = 0;
	int currentBibs = 0;
	int totalItems = 0;
	int currentItems = 0;
	int reqSize = 50;
	int lastBatch = -1;
	int maxSize = 1;
	int maxTime = 0;
	
	long totalDur = 0;
	long currentDur = 0;
	long start = 0;
	
	Date endTime = null;
	
	public static final String P_totalBibs = "totalBibs";
	public static final String P_totalItems = "totalItems";
	public static final String P_totalTime = "totalTime";
	public static final String P_lastBibId = "lastBibId";

	public static final String PCALC_timePerBib = "timePerBib";
	public static final String PCALC_timePerBibItem = "timePerBibItem";

	ExtractStats(int max, int maxTime, Properties prop) throws NumberFormatException {
		this.maxSize = max;
		this.maxTime = maxTime;
		
		if (maxTime > 0) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MINUTE, maxTime);
			endTime = cal.getTime();
		}
		
		if (prop != null) {
			totalBibs = Integer.parseInt(prop.getProperty(P_totalBibs, "0"));
			totalItems = Integer.parseInt(prop.getProperty(P_totalItems, "0"));
			totalDur = Long.parseLong(prop.getProperty(P_totalTime, "0"));
			lastId = Integer.parseInt(prop.getProperty(P_lastBibId, "-1"));
		}
	}
	
	public void start() {
		start = Calendar.getInstance().getTime().getTime();
	}
	
	public long end() {
		if (start == 0) return 0;
		long dur = Calendar.getInstance().getTime().getTime() - start;
		totalDur += dur;
		currentDur += dur;
		if (GUExtractSierraBibs.isInfo()) System.out.println(NumberFormat.getInstance().format(dur) + " ms");
		return dur;
	}
	
	int getBibLimit() {
		return  maxSize == 0 ? reqSize : Math.min(reqSize,maxSize);
	}
	
	boolean allBibsRetrieved() {
		if (lastBatch == -1) return false;
		if (isResumeNeeded()) return true;
		if (lastBatch < reqSize) return true;
		if (lastBatch == 0) return true;
		return false;
	}
	
	boolean isResumeNeeded() {
		if (maxSize > 0) {
			if (currentBibs >= maxSize) return true;
		}
		if (endTime != null){
			if (Calendar.getInstance().getTime().compareTo(endTime) >= 0) return true; 			
		}
		return false;
	}
	
	ExtractItemStats tallyAndListBibs(TreeMap<Integer,IIIMarc> bibMarcMap) {
		StringBuffer bibIds = new StringBuffer();
		
		lastBatch = 0;
		for(Integer bibId: bibMarcMap.keySet()) {
			if (bibIds.length()>0) bibIds.append(",");
			bibIds.append(bibId);
			lastId = bibId;
			totalBibs++;
			currentBibs++;
			lastBatch++;
		}
		return new ExtractItemStats(this, bibIds.toString());
	}
	
	public static final NumberFormat nf = NumberFormat.getNumberInstance();
	void line(String label, String val, String unit) {
		System.out.println(String.format("%25s:%10s %s", label, val, unit));
	}

	void line(String label, Integer val, String unit) {
		line(label, nf.format(val), unit);
	}

	void line(String label, Long val, String unit) {
		line(label, nf.format(val), unit);
	}
	void lineHeader() {
		System.out.println("=================================================");
	}
	
	void report() {
		lineHeader();
		line("Current Bibs", currentBibs, "");
		line("Current Items", currentItems, "");
		line("Current Time", currentDur, "ms");
		if (currentBibs > 0) line("Current Time/Bib", currentTimePerBib(), "ms/bib");
		if (currentBibs + currentItems > 0) line("Current Time/Bib&Item", currentTimePerBibItem(), "ms/bib+item");

		line("Total Bibs", totalBibs, "");
		line("Total Items", totalItems, "");
		line("Total Time", totalDur, "ms");
		if (totalBibs > 0) line("Total Time/Bib", timePerBib(), "ms/bib");
		if (totalBibs + totalItems > 0) line("Total Time/Bib&Item", timePerBibItem(), "ms/bib+item");
		lineHeader();
	}
	
	public long timePerBib() {
		if (totalBibs == 0) return 0;
		return totalDur/totalBibs;
	}
	public long timePerBibItem() {
		if (totalBibs+totalItems == 0) return 0;
		return totalDur/(totalBibs+totalItems);
	}
	public long currentTimePerBib() {
		if (currentBibs == 0) return 0;
		return currentDur/currentBibs;
	}
	public long currentTimePerBibItem() {
		if (currentBibs+currentItems == 0) return 0;
		return currentDur/(currentBibs+currentItems);
	}

	public void updateProperties(Properties prop) {
		prop.setProperty(P_lastBibId, ""+lastId);
		prop.setProperty(P_totalBibs, ""+totalBibs);
		prop.setProperty(P_totalItems, ""+totalItems);
		prop.setProperty(P_totalTime, ""+totalDur);
		prop.setProperty(PCALC_timePerBib, ""+timePerBib());
		prop.setProperty(PCALC_timePerBibItem, ""+timePerBibItem());
	}
}