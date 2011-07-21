package org.lsc.webai.utils;

import java.io.File;
import java.io.FilenameFilter;

public class FileOnlyFilter implements FilenameFilter {
	
	private String pattern;

	public FileOnlyFilter() {}
	
	public FileOnlyFilter(String pattern) {
		this.pattern = pattern;
	}

	public boolean accept(File dir, String name) {
		if(new File(dir, name).isFile()) {
			return pattern != null && name.matches(pattern);
		}
		return false;
	}
	
}

