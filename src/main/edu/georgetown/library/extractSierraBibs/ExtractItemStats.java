package edu.georgetown.library.extractSierraBibs;


class ExtractItemStats {
	ExtractStats parentStats;
	String bibIds;
	
	ExtractItemStats(ExtractStats parentStats, String bibIds) {
		this.parentStats = parentStats;
		this.bibIds = bibIds;
	}
	int offset=0;
	int itotal=-1;
	
	boolean isAllItemsRetrieved() {
		if (itotal < 0) return false;
		return offset >= itotal;
	}
	
	void tallyItems(int total, int batchSize) {
		itotal = total;
		offset += batchSize;
		parentStats.totalItems += batchSize;
		parentStats.currentItems += batchSize;
	}
}