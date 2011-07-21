/*
 ****************************************************************************
 * Ldap Synchronization Connector provides tools to synchronize
 * electronic identities from a list of data sources including
 * any database with a JDBC connector, another LDAP directory,
 * flat files...
 *
 *                  ==LICENSE NOTICE==
 * 
 * Copyright (c) 2008 - 2011 LSC Project 
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
 *               (c) 2008 - 2010 LSC Project
 *         Sebastien Bahloul <seb@lsc-project.org>
 *         Thomas Chemineau <thomas@lsc-project.org>
 *         Jonathan Clarke <jon@lsc-project.org>
 *         Remy-Christophe Schermesser <rcs@lsc-project.org>
 ****************************************************************************
 */
package org.lsc.webai.services;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.MalformedURLException;
import java.util.Map;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnector;
import javax.naming.ServiceUnavailableException;

import org.apache.commons.lang.ArrayUtils;
import org.lsc.jmx.LscServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LscRemoteCommands {

	/** The local logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(LscRemoteCommands.class);

	/** The different asynchronous task operation types */
	public enum OperationType {
		START, STOP, STATUS, TASKS_LIST, UNKNOWN,
	}
	
	/** Specify the URL */
	private JMXServiceURL url;
	/** The RMI connector */
	private RMIConnector jmxC;
	/** The MBean server connection */
	private MBeanServerConnection jmxc;
	/** The LSC Server MBean */
	private LscServer lscServer;
	/** The IP hostname */
	private String hostname;
	/** The TCP port */
	private String port;
	/** Identifier to synchronize */
	private String idToSync;
	/** Identifier to synchronize */
	private Map<String, String> attrsToSync;

	private static LscRemoteCommands instance;
	
	static {
		instance = new LscRemoteCommands();
		// Default parameters
		instance.hostname = "localhost";
		instance.port = "1099";
	}
	
	private LscRemoteCommands() {
	}
	
	/**
	 * Bind to the JMX Server 
	 */
	public static boolean bind() {
		return instance.jmxBind();
	}
	
	private boolean jmxBind() {
		if(lscServer != null) {
			try {
				return lscServer.ping();
			} catch(UndeclaredThrowableException ce) {
				
			}
		}
		try {
			String sUrl = "service:jmx:rmi:///jndi/rmi://" + hostname + ":" + port + "/jmxrmi";
			LOGGER.info("Connecting to remote engine on : " + sUrl);
			url = new JMXServiceURL(sUrl);
			jmxC = new RMIConnector(url, null);
			jmxC.connect();
			jmxc = jmxC.getMBeanServerConnection();
			ObjectName lscServerName = new ObjectName("org.lsc.jmx:type=LscServer");
			lscServer = JMX.newMXBeanProxy(jmxc, lscServerName, LscServer.class, true);
			return true;
		} catch (MalformedObjectNameException e) {
			LOGGER.error(e.toString(), e);
		} catch (NullPointerException e) {
			LOGGER.error(e.toString(), e);
		} catch (MalformedURLException e) {
			LOGGER.error(e.toString(), e);
		} catch (IOException e) {
			if(!(e.getCause() instanceof ServiceUnavailableException)) {
				LOGGER.error(e.toString(), e);
			}
		}
		return false;
	}

	public int run(String taskName, OperationType operation) {
		if(!jmxBind()) {
//			System.exit(1);
			return 1;
		}
		switch(operation) {
			case START:
				if(idToSync != null) {
					if(lscServer.launchSyncTaskById(taskName, idToSync, attrsToSync)) {
						LOGGER.info("Synchronization per id successfully achieved.");
					} else {
						LOGGER.error("Synchronization per id failed !");
						return 2;
					}
				} else {
					lscServer.startAsyncTask(taskName);
				}
				break;
			case STOP:
				lscServer.shutdownAsyncTask(taskName);
				break;
			case STATUS:
				if(ArrayUtils.contains(lscServer.getAsyncTasksName(), taskName)) {
					LOGGER.info("Asynchronous task " + taskName + " is " + ( lscServer.isAsyncTaskRunning(taskName) ? "running" : "stopped"));
				} else 	if(ArrayUtils.contains(lscServer.getSyncTasksName(), taskName)) {
					LOGGER.info("Synchronous task " + taskName + " is " + ( lscServer.isAsyncTaskRunning(taskName) ? "running" : "stopped"));
				} else {
					LOGGER.error("Unknown or synchronous task name: " + taskName);
					return 3;
				}
				break;
			case TASKS_LIST:
				LOGGER.info("Available asynchronous tasks are: ");
				for(String task: lscServer.getAsyncTasksName()) {
					LOGGER.info(" - " + task);
				}
				LOGGER.info("Available synchronous tasks are: ");
				for(String task: lscServer.getSyncTasksName()) {
					LOGGER.info(" - " + task);
				}
				break;
			default:
				
		}
		jmxUnbind();
		return 0;
	}
	
	public static boolean unbind() {
		return instance.jmxUnbind();
	}

	/**
	 * Unbind from the JMX Server 
	 */
	protected boolean jmxUnbind() {
		try {
			jmxC.close();
			return true;
		} catch (IOException e) {
			LOGGER.error(e.toString(), e);
		}
		return false;
	}

	public static String getPid() {
		return instance.lscServer.getPid();
	}
	
	public static String instanceStatus() {
		return instance.lscServer.status();
	}
	
	public static void stopInstance() {
		instance.lscServer.stop();
		instance.jmxUnbind();
	}
	
	public static String[] getAsyncTasks() {
		return instance.lscServer.getAsyncTasksName();
	}

	public static String[] getSyncTasks() {
		return instance.lscServer.getSyncTasksName();
	}
	
	public static boolean isAsyncTaskStarted(String taskName) {
		return instance.lscServer.isAsyncTaskRunning(taskName);
	}
	
	public static void startAsyncTask(String taskName) {
		instance.lscServer.startAsyncTask(taskName);
	}
	
	public static void stopAsyncTask(String taskName) {
		instance.lscServer.shutdownAsyncTask(taskName);
	}
	
	public static void startSyncTask(String taskName, String id, Map<String, String> attributes) {
		instance.lscServer.launchSyncTaskById(taskName, id, attributes);
	}
	
	public static String getConfigurationDirectory() {
		return instance.lscServer.getConfigurationDirectory();
	}
	
	public static void launchCleanTask(String taskName) throws Exception {
		instance.lscServer.launchCleanTask(taskName);
	}

}
