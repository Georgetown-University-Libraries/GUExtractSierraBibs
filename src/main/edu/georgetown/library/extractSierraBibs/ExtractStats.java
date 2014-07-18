package edu.georgetown.library.extractSierraBibs;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.TreeMap;

class ExtractStats {
	int lastId = -1;
	int totalBibs = 0;
	int totalItems = 0;
	int reqSize = 50;
	int lastBatch = -1;
	int maxSize = 1;
	
	long totalDur = 0;
	long start = 0;
	
	ExtractStats(int max) {
		this.maxSize = max;
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
		if (totalBibs >= maxSize) {
			if (maxSize > 0) return true;
		} 
		return (lastBatch == 0);
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