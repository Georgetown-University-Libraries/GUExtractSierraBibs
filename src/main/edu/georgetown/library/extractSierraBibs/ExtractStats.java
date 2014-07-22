package edu.georgetown.library.extractSierraBibs;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
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
	
	ExtractStats(int max, int maxTime) {
		this.maxSize = max;
		this.maxTime = maxTime;
		
		if (maxTime > 0) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MINUTE, maxTime * 60_000);
			endTime = cal.getTime();
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
		if (lastBatch < reqSize) return true;
		if (maxSize > 0) {
			if (totalBibs >= maxSize) return true;
		}
		if (lastBatch == 0) return true;
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
		if (totalBibs > 0) System.out.println("Total Time/Bib " + totalDur/totalBibs + "ms");
		if (totalBibs + totalItems > 0) System.out.println("Total Time/Bib&Item " + totalDur/(totalBibs+totalItems) + "ms");
		System.out.println("==============================");
	}
}