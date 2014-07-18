package edu.georgetown.library.extractSierraBibs;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public enum QUERY_TYPE {
	ALL("", IncludeDeleted.N),
	ADDED("createdDate", IncludeDeleted.N),
	UPDATED("updatedDate", IncludeDeleted.N) {
		public String getQuery(Date endDate, int days) {
			StringBuffer buf = new StringBuffer();
			buf.append(super.getQuery(endDate, days));
			buf.append("&createdDate=[,");
			buf.append(df.format(getStartDate(endDate, days)));
			buf.append("]");
			return buf.toString();
		}
	},
	ADDED_OR_UPDATED("updatedDate", IncludeDeleted.N),
	DELETED("deletedDate", IncludeDeleted.Y);
	
	enum IncludeDeleted {Y,N,NA;}
	
	static DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	String dateField;
	IncludeDeleted includeDeleted;
	QUERY_TYPE(String dateField, IncludeDeleted includeDeleted) {
		this.dateField = dateField;
		this.includeDeleted = includeDeleted;
	}
	public String getQuery(Date endDate, int days) {
		StringBuffer buf = new StringBuffer();
		if (includeDeleted == IncludeDeleted.Y) {
			buf.append("&deleted=true");
		} else if (includeDeleted == IncludeDeleted.N) {
			buf.append("&deleted=false");					
		}
		buf.append(getDateFilter(dateField, endDate, days));
		return buf.toString();
	}
	
	Date getStartDate(Date endDate, int days) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(endDate);
		cal.add(Calendar.DATE, -days);
		return cal.getTime();
	}
	
	private String getDateFilter(String field, Date endDate, int days) {
		if (field.isEmpty()) return "";
		Date startDate = getStartDate(endDate, days);
		StringBuffer buf = new StringBuffer();
		buf.append("&")
		   .append(field)
		   .append("=")
		   .append("[")
	       .append(df.format(startDate))
		   .append(",")
		   .append(df.format(endDate))
		   .append("]");
		return buf.toString();
	}
}