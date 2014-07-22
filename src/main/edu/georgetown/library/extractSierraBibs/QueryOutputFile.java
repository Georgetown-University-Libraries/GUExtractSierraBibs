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
	
	public QueryOutputFile(OutputStats stats, FILE_OUTPUT fileOutput, Date date, String seq) throws FileNotFoundException {
	    this.fileOutput = fileOutput;
	    setSeq(stats, date, seq);
	}
	
	public QueryOutputFile(OutputStats stats, File f) throws FileNotFoundException {
		file = f;
		mwrite = new MarcStreamWriter(new FileOutputStream(file, true));
	}

	public void setSeq(OutputStats stats, Date date, String seq) throws FileNotFoundException {
		File newFile = fileOutput.getFile(date, seq);
		if (file != null) {
			if (file.getPath().equals(newFile.getPath())) return;
		}
		this.seq = seq;
		close();
		file = newFile;
		mwrite = new MarcStreamWriter(new FileOutputStream(file));
	}
	
	public void close() {
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
	
	
	public boolean write(IIIMarc iiiMarc, OutputStats stats) throws FileNotFoundException {
		if (!fileOutput.test(iiiMarc)) return false;
		getWriter().write(iiiMarc.marcRec);
		stats.increment(file);
		return true;
	}
	
}
