package edu.georgetown.library.extractSierraBibs;

import java.io.File;
import java.util.Comparator;
import java.util.TreeMap;

public class OutputStats {
	TreeMap<File,Integer> outCounts = new TreeMap<>(
		new Comparator<File>(){
			public int compare(File o1, File o2) {
				return o1.getPath().compareTo(o2.getPath());
			}
		}
	);
	
	public void report() {
		System.out.println("\n");
		System.out.println("==============================");
		for(File f: outCounts.keySet()) {
			Integer val = outCounts.get(f);
			System.out.println(String.format("%-50.50s: ", f.getPath()) + val);
		}
		System.out.println("==============================");
	}

	public void increment(File file) {
		Integer count = outCounts.get(file);
		if (count == null) {
			count = 0;
			outCounts.put(file, count);
		}
		count++;
	}
}
