package edu.georgetown.library.extractSierraBibs;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.TreeMap;

class ExtractStats {
	int lastId = -1;
	int totalBibs = 0;
	int totalItems = 0;
	int reqSize = 50;
	int lastBatch = -1;
	int maxSize = 1;
	int maxTime = 0;
	
	long totalDur = 0;
	long start = 0;
	
	Date endTime = null;
	
	public static final String P_totalBibs = "totalBibs";
	public static final String P_totalItems = "totalItems";
	public static final String P_totalTime = "totalTime";
	public static final String P_timePerBib = "timePerBib";
	public static final String P_timePerBibItem = "timePerBibItem";
	public static final String P_lastBibId = "lastBibId";
	
	ExtractStats(int max, int maxTime, Properties prop) throws NumberFormatException {
		this.maxSize = max;
		this.maxTime = maxTime;
		
		if (maxTime > 0) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MINUTE, maxTime * 60_000);
			endTime = cal.getTime();
		}
		
		if (prop != null) {
			totalBibs = Integer.parseInt(prop.getProperty(P_totalBibs, "0"));
			totalItems = Integer.parseInt(prop.getProperty(P_totalItems, "0"));
			totalDur = Long.parseLong(prop.getProperty(P_totalTime, "0"));
		}
	}
	
	public void start() {
		start = Calendar.getInstance().getTime().getTime();
	}
	
	public long end() {
		if (start == 0) return 0;
		long dur = Calendar.getInstance().getTime().getTime() - start;
		totalDur += dur;
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
			if (totalBibs >= maxSize) return true;
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
			lastBatch++;
		}
		return new ExtractItemStats(this, bibIds.toString());
	}
	
	void report() {
		System.out.println("==============================");
		System.out.println("Total Bibs:    " + totalBibs);
		System.out.println("Total Items:   " + totalItems);
		System.out.println("Total Time:    " + totalDur + "ms");
		if (totalBibs > 0) System.out.println("Total Time/Bib " + timePerBib() + "ms");
		if (totalBibs + totalItems > 0) System.out.println("Total Time/Bib&Item " + totalDur/(totalBibs+totalItems) + "ms");
		System.out.println("==============================");
	}
	
	public long timePerBib() {
		if (totalBibs == 0) return 0;
		return totalDur/totalBibs;
	}
	public long timePerBibItem() {
		if (totalBibs+totalItems == 0) return 0;
		return totalDur/(totalBibs+totalItems);
	}

	public void updateProperties(Properties prop) {
		prop.setProperty(P_lastBibId, ""+lastId);
		prop.setProperty(P_totalBibs, ""+totalBibs);
		prop.setProperty(P_totalItems, ""+totalItems);
		prop.setProperty(P_totalTime, ""+totalDur);
		prop.setProperty(P_timePerBib, ""+timePerBib());
		prop.setProperty(P_timePerBibItem, ""+timePerBibItem());
	}
}