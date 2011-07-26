/*
 ****************************************************************************
 * Ldap Synchronization Connector provides tools to synchronize
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
package org.lsc.webai.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle Java fork processing
 * @author Sebastien Bahloul &lt;seb@lsc-project.org&gt;
 */
public class ForkProcess implements Runnable {

	private static Logger LOGGER = LoggerFactory.getLogger(ForkProcess.class); 

	private Process process;

	private DataOutputStream output;

	private BufferedReader input;

	private BufferedReader error;
	
	private int debugPort;
	
	private boolean debug;
	
	private ProcessBuilder builder;

	public ForkProcess() {
		debug = false;
	}

	public ForkProcess(int debugPort) {
		if(debugPort > 0) {
			debug = true;
			this.debugPort = debugPort;
		}
	}

	public void fork(String className, String[] params, Map<String, String> env) {
		builder = new ProcessBuilder();
		builder.directory(new File(this.getClass().getClassLoader().getResource(".").getPath()));
		List<String> commands = new ArrayList<String>();
		if(SystemUtils.IS_OS_WINDOWS) {
			if(debug) {
				commands.add("c:\\windows\\system32\\cmd.exe");
				commands.add("/K");
			}
			commands.add('"' + System.getProperty("java.home") + "\\bin\\java.exe" + '"');
		} else {
			commands.add(System.getProperty("java.home") + "/bin/java");
		}
		commands.add("-classpath ");
		// Add the Run Jetty Run classpath
		commands.add(getClasspath());
		for(Entry<String, String> envItem : env.entrySet()) {
			commands.add("-D" + envItem.getKey() + "=" + envItem.getValue());
		}
		if(debug) {
			commands.add("-Xdebug");
			commands.add("-Xrunjdwp:transport=dt_socket,address=" + debugPort + ",server=y,suspend=y");
		}
		commands.add(className);
		Collections.addAll(commands, params);
		builder.command(commands);
		builder.environment().putAll(env);
		this.run();
		while(process == null) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		output = new DataOutputStream(process.getOutputStream());
		input = new BufferedReader(new InputStreamReader(process.getInputStream()));
		error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
	}
	
	public void run() {
		try {
			process = builder.start();
		} catch (IOException e) {
			LOGGER.error(e.toString(),e);
		}
	}
	
	public String getMessages() {
		String messages = readMessages(input);
		input = null;
		return messages;
	}

	public String getErrorMessages() {
		String messages = readMessages(error);
		error = null;
		return messages;
	}
	
	private String readMessages(BufferedReader input) {
		StringBuffer messages = new StringBuffer();
		try {
			if(input != null) {
				String line = null;
				while(input.ready() && (line = input.readLine()) != null) {
					messages.append(line.trim()).append("\n");
				}
			}
			return messages.toString();
		} catch (IOException e) {
			LOGGER.error(e.toString(),e);
			return null;
		} finally {
			try {
				if(input != null) {
					input.close();
				}
			} catch (IOException e) {
				LOGGER.error(e.toString(),e);
			}
		}
	}

	public synchronized void close() {
		try {
			if(output != null) {
				output.close(); 
				output = null;
			}
			if(input != null) {
				input.close(); 
				input = null;
			}
			if(error != null) {
				error.close(); 
				error = null;
			}
		} catch (IOException ignore) {
		}
		if(process != null) {
			process.destroy();
		}
	}
	
	private String getClasspath() {
		// Hack to handle Run Jetty Run Eclipse plugin
		String rjrClassPath = System.getProperty("rjrclasspath", null);
		if(rjrClassPath != null) {
			try {
				return new URI(rjrClassPath).getPath();
			} catch (URISyntaxException e) {
			}
		}
		if(System.getProperty("LSC_HOME") != null) {
			StringBuffer cp = new StringBuffer();
			File lscHome = new File(System.getProperty("LSC_HOME"), "jetty" + File.separator + "webapps" + File.separator + "lsc-webai" + File.separator + "WEB-INF" + File.separator + "lib");
			if(lscHome.exists() && lscHome.isDirectory()) {
				for(String filename: lscHome.list(new SuffixFileFilter(".jar"))) {
					if(cp.length() > 0) {
						if(SystemUtils.IS_OS_WINDOWS) {
							cp.append(";");
						} else {
							cp.append(":");
						}
					}
					cp.append(lscHome.getAbsolutePath() + File.separator + filename);
				}
				return cp.toString();
			}
		}
		return System.getProperty("java.class.path", null);
	}
}
