/*
 ****************************************************************************
 * Ldap Synchronization Connector provid es tools to synchronize
 * electronic identities from a list of data sources including
 * any database with a JDBC connector, another LDAP directory,
 * flat files...
 *
 *                  ==LICENSE NOTICE==
 * 
 * Copyright (c) 2009-2010, LSC Project 
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:

 *    * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of the LSC Project nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *                  ==LICENSE NOTICE==
 *
 *               (c) 2008 - 2011 LSC Project
 *         Sebastien Bahloul <seb@lsc-project.org>
 *         Thomas Chemineau <thomas@lsc-project.org>
 *         Jonathan Clarke <jon@lsc-project.org>
 *         Remy-Christophe Schermesser <rcs@lsc-project.org>
 ****************************************************************************
 */
package org.lsc.webai.components;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Abstract class path edition page
 * @author Sebastien Bahloul &lt;seb@lsc-project.org&gt;
 */
public abstract class AbstractPathEdition {

	private static final int MAX_SIZE = 20;

	private boolean locallyDeployed = true;

	private String filePath;
	
	public String getFilePath() {
		return filePath;
	}
	
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	/**
	 * Provide the directories listing on the application server local file system
	 * @param partial the written path
	 * @return the list of values available to select on client side
	 */
	public String[] onProvideCompletions(String partial) {
		String[] values = null;
		if( !locallyDeployed ) {
			values = new String[] { "Unavailable on distant installation !" };
		} else if( partial.lastIndexOf( File.separator ) == -1 ) {
			values = prepend( partial, new File(partial).list() );
		} else if( partial.lastIndexOf( File.separator ) == 0 ) {
			values = prepend( "/", new File("/").list() );
		} else if ( new File(partial).isDirectory() ) {
			// TODO Removing trailing /
			values = prepend( partial + (partial.endsWith(File.separator) ? "" : File.separator) , new File(partial).list(new DirectoryFilter()) );
		} else {
			String prefix = partial.substring( 0, partial.lastIndexOf( File.separator ) );
			String motif = partial.substring(partial.lastIndexOf( File.separator )+1);
			FilenameFilter filter = new DirectoryFilter(motif + ".*");
			values = prepend( prefix + File.separator, new File(prefix).list(filter) );
		}
		List<String> valuesList = Arrays.asList(values);
		Collections.sort(valuesList);
		return (String[]) valuesList.toArray(new String[valuesList.size()]);
	}

	/**
	 * Prepend prefix on each table value
	 * @param prefix the prefix
	 * @param params the values table
	 * @return the prefixed values table
	 */
	public String[] prepend(String prefix, String[] params) {
		if(params != null) {
			int max = (params.length < MAX_SIZE ? params.length : MAX_SIZE);
			String[] returns = new String[max];
			for(int i = 0; i < max; i++) {
				returns[i] = prefix + params[i];
			}
			return returns;
		} else {
			return new String[]{};
		}
	}
	
}

/**
 * Filter only directory
 */
class DirectoryFilter implements FilenameFilter {

	private String pattern;

	public DirectoryFilter() {}
	
	public DirectoryFilter(String pattern) {
		this.pattern = pattern;
	}

	public boolean accept(File dir, String name) {
		if(new File(dir, name).isDirectory()) {
			return pattern == null || name.matches(pattern);
		}
		return false;
	}
}
