package edu.georgetown.library.extractSierraBibs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Date;

import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcWriter;

public class QueryOutputFile {
	FILE_OUTPUT fileOutput;
	
	File file;
	MarcWriter mwrite;
	String seq = "";
	int count = 0;
	
	public QueryOutputFile(OutputStats stats, FILE_OUTPUT fileOutput, Date date, String seq) throws FileNotFoundException {
	    this.fileOutput = fileOutput;
	    setSeq(stats, date, seq);
	}
	
	public void setSeq(OutputStats stats, Date date, String seq) throws FileNotFoundException {
		File newFile = fileOutput.getFile(date, seq);
		if (file != null) {
			if (file.getPath().equals(newFile.getPath())) return;
		}
		this.seq = seq;
		close(stats);
		file = newFile;
		mwrite = new MarcStreamWriter(new FileOutputStream(file));
	}
	
	public void close(OutputStats stats) {
		if (file != null) stats.outCounts.put(file, count);
		if (mwrite == null) return;
		mwrite.close();
		mwrite = null;
	}

	public MarcWriter getWriter() throws FileNotFoundException {
		if (mwrite==null) {
			mwrite = new MarcStreamWriter(new FileOutputStream(file));			
		}
		return mwrite;
	}
	
	
	public boolean write(IIIMarc iiiMarc) throws FileNotFoundException {
		if (!fileOutput.test(iiiMarc)) return false;
		getWriter().write(iiiMarc.marcRec);
		count++;
		return true;
	}
	
}
