package edu.georgetown.library.extractSierraBibs;

import java.util.Vector;

public enum QUERY_PARAMS {
	ALL(QUERY_TYPE.ALL),
	ADDED(QUERY_TYPE.ADDED),
	UPDATED(QUERY_TYPE.UPDATED), 
	ADDED_OR_UPDATED(QUERY_TYPE.ADDED_OR_UPDATED),
	DELETED(QUERY_TYPE.DELETED),
	DAILY();
	
	Vector<QUERY_TYPE> queries = new Vector<>();
	QUERY_PARAMS(QUERY_TYPE qt) {
		queries.add(qt);
	}
	QUERY_PARAMS() {
		queries.add(QUERY_TYPE.ADDED);
		queries.add(QUERY_TYPE.UPDATED);
		queries.add(QUERY_TYPE.ADDED_OR_UPDATED);
		queries.add(QUERY_TYPE.DELETED);
	}
	
}