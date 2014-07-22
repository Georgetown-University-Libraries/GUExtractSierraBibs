package edu.georgetown.library.extractSierraBibs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class ApiConfigFile {

	File dirRoot;
	String apiRoot = "https://sandbox.iii.com/iii/sierra-api/v1/";
	String key = "";
	String secret = "";
	File locations;

	public ApiConfigFile(String cfname) throws FileNotFoundException, IOException {
		Properties prop = new Properties();
		File cf = new File(cfname);
		prop.load(new FileReader(cf));
		dirRoot = new File(prop.getProperty("ROOTDIR"));
		apiRoot = prop.getProperty("APIROOT");
		key = prop.getProperty("CLIENT_KEY");
		secret = prop.getProperty("CLIENT_SECRET");
		locations = new File(prop.getProperty("LOCATION_CODE_FILE"));
	}

}
