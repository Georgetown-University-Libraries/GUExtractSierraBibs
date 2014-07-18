package edu.georgetown.library.oauth;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

/**
 * Abstract class handling the import of a character-delimited text file allowing for individual values to be wrapped by quotation marks.
 * @author TBrady
 *
 */
public class DelimitedFileReader {
	BufferedReader br;
	String sep;
	String pline;
	public DelimitedFileReader(File f, String sep) throws FileNotFoundException, UnsupportedEncodingException {
		br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
		this.sep = sep;
	}

	public DelimitedFileReader(InputStream is, String sep) throws FileNotFoundException, UnsupportedEncodingException {
		br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		this.sep = sep;
	}

	public Vector<String> getRow() throws IOException {
		pline = br.readLine();
		if (pline == null) {
			br.close();
			return null;
		}
		if (!sep.trim().equals("")) pline = pline.trim();
		Vector<String> cols = new Vector<String>();
		while(pline!=null){
			if (!sep.trim().equals("")) pline = pline.trim();
			String tpline = getNextString(pline);
			cols.add(normalize(tpline));
			if (pline.length() == tpline.length()) break;
			pline = pline.substring(tpline.length()+sep.length());
		}
		return cols;
	}
	
	protected String getNextString(String in) throws IOException {
		return getNextString(in,0);
	}
	protected String getNextString(String in, int start) throws IOException {
		int pos = -1;
		if (in.startsWith("\"")) {
			int qpos = in.indexOf("\"", (start==0) ? 1 : start);
			int qqpos = in.indexOf("\"\"", (start==0) ? 1 : start);
			if ((qpos==qqpos)&&(qqpos >= 0)) {
				return getNextString(in,qqpos+2);
			}
			if (qpos == in.length()) {
				return in;
			}
			if (qpos == -1) {
				String s = br.readLine();
				if (s == null) return pline;
				pline += s;
				return getNextString(pline, start);
			}
			pos = in.indexOf(sep,qpos+1);
		} else {
			pos = in.indexOf(sep, 0);
		}
		if (pos == -1) return in;
		return in.substring(0,pos);
	}
	
	public static Vector<Vector<String>> parseFile(File f, String sep) throws IOException{
		return parseFile(f,sep,false);
	}
	public static Vector<Vector<String>> parseFile(File f, String sep, boolean skipFirstLine) throws IOException{
		DelimitedFileReader dfr = new DelimitedFileReader(f, sep);
		Vector<Vector<String>> rows = new Vector<Vector<String>>();
		Vector<String> hrow = new Vector<String>();
		if (skipFirstLine) hrow = dfr.getRow();
		if (hrow != null) {
			for(Vector<String> row = dfr.getRow(); row!=null; row = dfr.getRow()){
				rows.add(row);
			}			
		}
		return rows;
	}
	
	public static Vector<Vector<String>> parseStream(InputStream is, String sep, boolean skipFirstLine) throws IOException{
		DelimitedFileReader dfr = new DelimitedFileReader(is, sep);
		Vector<Vector<String>> rows = new Vector<Vector<String>>();
		Vector<String> hrow = new Vector<String>();
		if (skipFirstLine) hrow = dfr.getRow();
		if (hrow != null) {
			for(Vector<String> row = dfr.getRow(); row!=null; row = dfr.getRow()){
				rows.add(row);
			}			
		}
		return rows;
	}
	
	protected static String normalize(String val) {
		val = val.trim();
		if (val.startsWith("\"")) {
			val = val.substring(1);
		} else if (val.startsWith("'")) {
			val = val.substring(1);
		}
		if (val.endsWith("\"")) {
			val = val.substring(0,val.length()-1);
		} else if (val.endsWith("'")) {
			val = val.substring(0,val.length()-1);
		}
		
		val = val.replaceAll("\"\"", "\"");
		return val;
	}
	
}
