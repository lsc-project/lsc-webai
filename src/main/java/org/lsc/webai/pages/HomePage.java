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
 *               (c) 2008 - 2011 LSC Project
 *         Sebastien Bahloul <seb@lsc-project.org>
 *         Thomas Chemineau <thomas@lsc-project.org>
 *         Jonathan Clarke <jon@lsc-project.org>
 *         Remy-Christophe Schermesser <rcs@lsc-project.org>
 ****************************************************************************
 */
package org.lsc.webai.pages;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.corelib.components.Select;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.lsc.Configuration;
import org.lsc.Launcher;
import org.lsc.configuration.AuditType;
import org.lsc.configuration.ConnectionType;
import org.lsc.configuration.JaxbXmlConfigurationHelper;
import org.lsc.configuration.LscConfiguration;
import org.lsc.configuration.TaskType;
import org.lsc.exception.LscConfigurationException;
import org.lsc.exception.LscException;
import org.lsc.webai.base.EditSettings;
import org.lsc.webai.components.AbstractPathEdition;
import org.lsc.webai.components.ConsultExecutionLog;
import org.lsc.webai.services.LscRemoteCommands;
import org.lsc.webai.utils.ForkProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Start page of LSC Web Administrative Interface.
 * 
 * @author Sebastien Bahloul &lt;seb@lsc-project.org&gt;
 */
public class HomePage extends AbstractPathEdition {
	
	private static Logger LOGGER = LoggerFactory.getLogger(HomePage.class); 

	public static String lscHomePath;
	
	public static String lscConfigurationPath;
	
	@SuppressWarnings("unused")
	@InjectComponent
	private ConsultExecutionLog consultExecutionLog;

	public String getLscHomePath() {
		return lscHomePath;
	}
	
	public String getLscConfigurationPath() {
		return lscConfigurationPath;
	}
	
	private String[] syncTasks;
	private String[] asyncTasks;
	
	@Property 
	private String taskName;
	
	@Property 
	private String idToSync;
	
	@SuppressWarnings("unused")
	@InjectComponent("syncTasks")
	private Select selectSyncTasks;
	
	@SuppressWarnings("unused")
	@InjectComponent("asyncTasksToStart") @Validate("required")
	private Select selectAsyncTasksToStart;
	
	@SuppressWarnings("unused")
	@InjectComponent("asyncTasksToStop") @Validate("required")
	private Select selectAsyncTasksToStop;

	@SuppressWarnings("unused")
	@Property
	@Persist(PersistenceConstants.FLASH)
	private String message;

	private static boolean lscStarting;
	
	/** When a multiple submit form is used, handle the CRUD type */
	private ActionType action;
	
	public boolean isLscStarting() {
		return lscStarting;
	}
	
	private boolean instanceStarted;
	
	private static LscInstance lscInstance;
	
	@InjectComponent
	private Zone messagesZone;
	
	/** HANDLE CONFIGURATION RELATED ACTIONS */
	
	public void onActionFromSaveConfiguration() {
		File xml = new File(Configuration.getConfigurationDirectory(), JaxbXmlConfigurationHelper.LSC_CONF_XML);
		try {
			LscConfiguration.saving();
			new JaxbXmlConfigurationHelper().saveConfiguration(xml.toString(), LscConfiguration.getInstance().getLsc());
			LscConfiguration.saved();
		} catch (IOException e) {
			message = "Unable to save to " + xml.toString() + " file ! (" + e.toString() + ")";
			LOGGER.error(e.toString(), e);
		} catch (LscConfigurationException e) {
			message = "Unable to save to " + xml.toString() + " file ! (" + e.toString() + ")";
			LOGGER.error(e.toString(), e);
		}
	}
	
	public void onActionFromRevertConfiguration() {
		LscConfiguration.revertToInitialState();
	}
	
	@SetupRender
	public void setupRender() throws LscException {
		instanceStarted = LscRemoteCommands.bind();
		if(System.getProperty("LSC_HOME") != null) {
			lscHomePath = System.getProperty("LSC_HOME");
		}
		if(instanceStarted) {
			lscConfigurationPath = LscRemoteCommands.getConfigurationDirectory();
		} else if(lscConfigurationPath == null) {
			try {
				if(System.getProperty("LSC_HOME") != null) {
					Configuration.setUp(lscHomePath + File.separator + "etc", false);
					lscConfigurationPath = Configuration.getConfigurationDirectory();
				} else {
					LOGGER.error("LSC_HOME environment variable not set. LSC configuration loading will fail !");
				}
			} catch (LscException e) {
				message = "Failed to load configuration (" + e + ")";
				LOGGER.error(e.toString(),e);
			}
		}
		// If not initialized, try to
		if(!LscConfiguration.isInitialized()) {
            Configuration.setUp(lscConfigurationPath, false);
		}
	}
	
	public boolean isLscStarted() {
		return instanceStarted;
	}
	
	public boolean isConfigurationPathChecked() {
		return lscConfigurationPath.compareTo(LscRemoteCommands.getConfigurationDirectory()) == 0;
	}
	
	public String getLscPid() {
		return LscRemoteCommands.getPid();
	}
	
	public String getLscStatus() {
		return LscRemoteCommands.instanceStatus();
	}
	
	@OnEvent(value="onStartInstance")
	public Object onStartInstance() throws InterruptedException {
		lscStarting = true;
		messages = null;
		errors = null;
		lscInstance = new LscInstance(lscConfigurationPath);
		lscInstance.start();
		Thread.sleep(3000);
		return messagesZone;
	}
	
	static String messages;
	
	public String getInstanceMessages() {
		if(lscInstance != null) {
			String msg = lscInstance.getMessages();
			if(msg != null && msg.length() > 0) {
				messages = msg.trim();
			}
		}
		return messages;
	}
	
	static String errors;
	
	public String getInstanceErrors() {
		if(lscInstance != null) {
			String msg = lscInstance.getErrorMessages();
			if(msg != null) {
				errors = msg.trim();
			}
		}
		return errors;
	}
	
	public void onSuccessFromStopInstance() {
		LscRemoteCommands.stopInstance();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
		}
	}
	
	public void onSuccessFromStartSyncTask() {
		LscRemoteCommands.startSyncTask(taskName, idToSync, new HashMap<String, String>());
	}
	
	public void onSuccessFromStartAsyncTask() {
		new Thread() {
			public void run() {
				LscRemoteCommands.startAsyncTask(taskName);
			}
		}.start();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
		}
	}
	
	public void onSuccessFromStopAsyncTask() {
		new Thread() {
			public void run() {
				LscRemoteCommands.stopAsyncTask(taskName);
			}
		}.start();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
		}
	}
	
	public SelectModel getAsyncStoppedTaskModel() {
		return getAsyncTaskModel(false);
	}
	
	public SelectModel getAsyncStartedTaskModel() {
		return getAsyncTaskModel(true);
	}
	
	public boolean isAsyncTasksNumberMoreThanOne() {
		return getAsyncTaskModel(true).getOptions().size() > 1;
	}
	
	public SelectModel getAsyncTaskModel(boolean started) {
		asyncTasks = LscRemoteCommands.getAsyncTasks();
		List<OptionModel> options = new ArrayList<OptionModel>();
		for(String task : asyncTasks) {
			boolean taskStarted = LscRemoteCommands.isAsyncTaskStarted(task);
			if((taskStarted && started) || (!taskStarted && !started)) {
				options.add(new OptionModelImpl(task, task));
			}
		}
		return new SelectModelImpl(null, options);
	}

	public SelectModel getSyncTaskModel() {
		syncTasks = LscRemoteCommands.getSyncTasks();
		List<OptionModel> options = new ArrayList<OptionModel>();
		for(String task : syncTasks) {
			options.add(new OptionModelImpl(task, task));
		}
		return new SelectModelImpl(null, options);
	}
	
	@InjectPage
	private EditTask editTask;

	public SelectModel getTasksModel() {
		Collection<TaskType> tasks = LscConfiguration.getTasks();
		List<OptionModel> options = new ArrayList<OptionModel>();
		for(TaskType task : tasks) {
			options.add(new OptionModelImpl(task.getName(), task.getName()));
		}
		return new SelectModelImpl(null, options);
	}
	
	public boolean isTasksModelAsLeastSizeTwo() {
		return getTasksModel().getOptions().size() >= 2;
	}
	
	public Object onSuccessFromEditTask() {
		switch(action) {
			case UPDATE:
				return editTask.initialize(LscConfiguration.getTask(taskName));
			case DELETE:
				LscConfiguration.removeTask(LscConfiguration.getTask(taskName));
		}
		return this;
	}

	@OnEvent(value="selected", component="editTaskButton")
	public void onEditTaskButtons(){
		action = ActionType.UPDATE;
	}
	
	@OnEvent(value="selected", component="deleteTaskButton")
	public void onDeleteTaskButtons(){
		action = ActionType.DELETE;
	}
	
	public Object onActionFromCreateTask() {
		return editTask.initialize(new TaskType());
	}
	
	
	/** HANDLE AUDIT RELATED STUFF */
	
	@Property
	private String auditName;
	
	@Property
	private String auditNewType;
	
	@InjectPage
	private EditAudit editAudit;

	public SelectModel getAuditsModel() {
		Collection<AuditType> audits = LscConfiguration.getAudits();
		List<OptionModel> options = new ArrayList<OptionModel>();
		for(AuditType audit : audits) {
			options.add(new OptionModelImpl(audit.getName(), audit.getName()));
		}
		return new SelectModelImpl(null, options);
	}
	
	public SelectModel getAvailableAuditModel() {
		List<OptionModel> options = new ArrayList<OptionModel>();
		for(Class<? extends AuditType> connectionClass : EditSettings.getReflections().getSubTypesOf(AuditType.class)) {
			options.add(new OptionModelImpl(connectionClass.getSimpleName(), connectionClass.getName()));
		}
		return new SelectModelImpl(null, options);
	}
	
	public Object onSuccessFromEditAudit() {
		switch(action) {
			case UPDATE:
				return editAudit.initialize(LscConfiguration.getAudit(auditName));
			case DELETE:
				LscConfiguration.removeAudit(LscConfiguration.getAudit(auditName));
		}
		return this;
	}
	
	@OnEvent(value="selected", component="editAuditButton")
	public void onEditAuditButtons(){
		action = ActionType.UPDATE;
	}
	
	@OnEvent(value="selected", component="deleteAuditButton")
	public void onDeleteAuditButtons(){
		action = ActionType.DELETE;
	}
	
	public Object onActionFromCreateAudit() {
		try {
			return editAudit.initialize((AuditType) Class.forName(auditNewType).newInstance());
		} catch (InstantiationException e) {
			message = "Unable to instanciate new audit (" + e + ")";
			LOGGER.error(e.toString(),e);
		} catch (IllegalAccessException e) {
			message = "Unable to instanciate new audit (" + e + ")";
			LOGGER.error(e.toString(),e);
		} catch (ClassNotFoundException e) {
			message = "Unable to instanciate new audit (" + e + ")";
			LOGGER.error(e.toString(),e);
		}
		return this;
	}
	
	/** HANDLE CONNECTIONS RELATED STUFF */
	
	@Property
	private String connectionName;
	
	@Property
	private String newConnectionType;
	
	@InjectPage
	private EditConnection editConnectionSettings;

	public SelectModel getConnectionsModel() {
		LscConfiguration.getInstance();
		Collection<ConnectionType> connections = LscConfiguration.getConnections();
		List<OptionModel> options = new ArrayList<OptionModel>();
		for(ConnectionType connection : connections) {
			options.add(new OptionModelImpl(connection.getName(), connection.getName()));
		}
		return new SelectModelImpl(null, options);
	}
	
	public SelectModel getAvailableConnectionModel() {
		List<OptionModel> options = new ArrayList<OptionModel>();
		for(Class<? extends ConnectionType> connectionClass : EditSettings.getReflections().getSubTypesOf(ConnectionType.class)) {
            options.add(new OptionModelImpl(connectionClass.getSimpleName(), connectionClass.getName()));
		}
		return new SelectModelImpl(null, options);
	}
	
	public Object onSuccessFromEditConnection() {
		switch(action) {
			case UPDATE:
				return editConnectionSettings.initialize(LscConfiguration.getConnection(connectionName));
			case DELETE:
				LscConfiguration.removeConnection(LscConfiguration.getConnection(connectionName));
		}
		return this;
	}

	@OnEvent(value="selected", component="editConnectionButton")
	public void onEditConnectionButtons(){
		action = ActionType.UPDATE;
	}
	
	@OnEvent(value="selected", component="deleteConnectionButton")
	public void onDeleteConnectionButtons(){
		action = ActionType.DELETE;
	}
	
	public Object onActionFromCreateConnection() {
		try {
			return editConnectionSettings.initialize((ConnectionType) Class.forName(newConnectionType).newInstance());
		} catch (InstantiationException e) {
			message = "Unable to instanciate new audit (" + e + ")";
			LOGGER.error(e.toString(),e);
		} catch (IllegalAccessException e) {
			message = "Unable to instanciate new audit (" + e + ")";
			LOGGER.error(e.toString(),e);
		} catch (ClassNotFoundException e) {
			message = "Unable to instanciate new audit (" + e + ")";
			LOGGER.error(e.toString(),e);
		}
		return this;
	}
	
	public LscConfiguration getLscConfiguration() {
		return LscConfiguration.getInstance();
	}

	public Object initialize(String path) {
		lscConfigurationPath = path;
		LscConfiguration.reinitialize();
		return this;
	}
}

enum ActionType {
	CREATE,
	RETRIEVE,
	UPDATE,
	DELETE,
}

class LscInstance implements Runnable {
	
	private String lscConfigurationPath;
	
	private static ForkProcess lscProcess;

	public LscInstance(String lscConfigurationPath) {
		this.lscConfigurationPath = lscConfigurationPath;
	}
		
	public String getErrorMessages() {
		return lscProcess.getErrorMessages();
	}

	public String getMessages() {
		return lscProcess.getMessages();
	}

	public synchronized void start() {
		if(lscProcess != null) {
			lscProcess.close();
		}
		new Thread(this).run();
	}

	public void run() {
		Map<String, String> environment = new HashMap<String, String>();
		if(System.getProperty("LSC_DEBUG_PORT") != null) {
			lscProcess = new ForkProcess(Integer.parseInt(System.getProperty("LSC_DEBUG_PORT")));
		} else {
			lscProcess = new ForkProcess();
		}
		environment.put("com.sun.management.jmxremote", "true");
		environment.put("com.sun.management.jmxremote.port", "1099");
		environment.put("com.sun.management.jmxremote.authenticate", "false"); 
		environment.put("com.sun.management.jmxremote.ssl", "false");
		String[] parameters = new String[] {"-a", "all", "-f", lscConfigurationPath, "-t", "1"};
		lscProcess.fork(Launcher.class.getCanonicalName(), parameters, environment);
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
		}
	}
	
}