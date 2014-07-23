package edu.georgetown.library.extractSierraBibs;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;

public class QueryOutputFiles {
	QUERY_TYPE queryType;
	Vector<QueryOutputFile> files = new Vector<>();
	OutputStats stats = new OutputStats();
	File SKIP = new File("NoMatch");
	
	static NumberFormat nf = NumberFormat.getIntegerInstance();
	static {nf.setMinimumIntegerDigits(2);}
	
	int seq = 0;
	int skipCount = 0;
	
	public QueryOutputFiles(QUERY_TYPE queryType, Date date, Properties prop) throws NumberFormatException, FileNotFoundException {
	    this.queryType = queryType;
	    this.seq = Integer.parseInt(prop.getProperty("seq", "0"));
		for(Object key: prop.keySet()) {
			String skey = key.toString();
			if (!skey.startsWith("output")) continue;
			File f = new File(skey);
			QueryOutputFile qof = new QueryOutputFile(stats, f);
			stats.outCounts.put(f, Integer.parseInt(prop.getProperty(key.toString())));
			files.add(qof);
		}

		for(FILE_OUTPUT fileOutput: FILE_OUTPUT.values()) {
			if (queryType != fileOutput.queryType) continue;
			files.add(new QueryOutputFile(stats, fileOutput, date, nf.format(seq)));
		}
	}
	
	public void setSeq(Date date, int seq) throws FileNotFoundException {
		for(QueryOutputFile qof: files) {
			qof.setSeq(stats, date, nf.format(seq));
		}
	}
	
	public void closeAll() {
		for(QueryOutputFile qof: files) {
			qof.close();
		}
		stats.outCounts.put(SKIP, skipCount);
	}

	public void writeAll(IIIMarc iiiMarc) throws FileNotFoundException {
		boolean skip = true;
		for(QueryOutputFile qof: files) {
			if (qof.write(iiiMarc, stats)) skip = false;
		}
		if (skip) skipCount++;
	}

	public void updateProperties(Properties prop) {
		for(File f: stats.outCounts.keySet()) {
			prop.setProperty(f.getPath(), stats.outCounts.get(f).toString());
		}
	}

}
