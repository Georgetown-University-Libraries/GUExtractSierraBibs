package edu.georgetown.library.extractSierraBibs;

import java.io.File;
import java.io.FilenameFilter;

enum QueueFolder {
	Running("running"), 
	Resume("resume"), 
	Complete("complete");
	
	String path;
	QueueFolder(String path) {
		this.path = path;
	}
	
	public File getDir(ApiConfigFile apiConfig) {
		File q = new File(apiConfig.dirRoot, "queue");
		return new File(q, path);
	}
	
	public boolean fileExists(ApiConfigFile apiConfig) {
		File f = getDir(apiConfig);
		return f.listFiles().length > 0;
	}
	
	class QtFilenameFilter implements FilenameFilter {
		QUERY_TYPE qt;
		QtFilenameFilter(QUERY_TYPE qt) {
			this.qt = qt;
		}
		public boolean accept(File dir, String name) {
			return name.startsWith(qt.name());
		}
	}
	
	public File getFile(ApiConfigFile apiConfig, QUERY_TYPE qt) {
		File f = getDir(apiConfig);
		File[] files = qt == null ? f.listFiles() : f.listFiles(new QtFilenameFilter(qt));
		return files.length > 0 ? files[0] : null;
	}
}