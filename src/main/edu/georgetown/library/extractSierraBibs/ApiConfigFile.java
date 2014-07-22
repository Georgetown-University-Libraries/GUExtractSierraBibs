package edu.georgetown.library.extractSierraBibs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

public class ApiConfigFile {

	File dirRoot;
	String apiRoot = "https://sandbox.iii.com/iii/sierra-api/v1/";
	String key = "";
	String secret = "";
	File locations;
	HashMap<String,String> locationMap = new HashMap<>();

	public ApiConfigFile(String cfname) throws FileNotFoundException, IOException {
		Properties prop = new Properties();
		File cf = new File(cfname);
		prop.load(new FileReader(cf));
		dirRoot = new File(prop.getProperty("ROOTDIR"));
		if (!dirRoot.exists()) {
			throw new FileNotFoundException("ROOTDIR does not exist: "+dirRoot.getAbsolutePath());
		}
		apiRoot = prop.getProperty("APIROOT");
		key = prop.getProperty("CLIENT_KEY");
		secret = prop.getProperty("CLIENT_SECRET");
		locations = new File(prop.getProperty("LOCATION_CODE_FILE"));
		if (!locations.exists()) {
			throw new FileNotFoundException("locations file does not exist: "+locations.getAbsolutePath());
		}
		Vector<Vector<String>> locationData = DelimitedFileReader.parseFile(locations, ",");
		boolean header = true;
		for(Vector<String> row: locationData) {
			if (header) {
				header = false;
				continue;
			}
			if (row.size() < 3) continue;
			String item = row.get(0);
			String bib = row.get(2);
			locationMap.put(item, bib);
		}
	}

}
