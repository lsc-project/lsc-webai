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
package org.lsc.webai.beans;

import java.util.Map;
import java.util.TreeMap;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.beaneditor.Validate;
import org.lsc.configuration.TaskType;
import org.lsc.webai.utils.LscJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;

public class JobTaskBean {
	
	@SuppressWarnings("unused")
	@Property(write=false)
	private String id;
	
	@Property
	@Validate("required")
	private String name;
	
	/**
	 * This is the flag to prevent entries add operation in the target
	 * directory.
	 */
	@Property
	private boolean nocreate;

	/**
	 * This is the flag to prevent entries update operation in the target
	 * data source.
	 */
	@Property
	private boolean noupdate = false;

	/**
	 * This is the flag to prevent entries delete operation in the target
	 * data source.
	 */
	@Property
	private boolean nodelete = false;

	/**
	 * This is the flag to prevent data from change id operation in the target
	 * data source.
	 */
	@Property
	private boolean nochangeid = false;
	
	private TaskType task;

	private Map<String, String> context;
	
	public JobTaskBean() {
		context = new TreeMap<String, String>();
	}
	
	public static JobTaskBean getInstance(JobDetail jobDetail) {
		JobTaskBean instance = new JobTaskBean();
		instance.name = jobDetail.getName();
		for(Object key: jobDetail.getJobDataMap().keySet()) { 
			instance.context.put((String) key, jobDetail.getJobDataMap().getString((String)key));
		}
		return instance;
	}
	
	public JobDetail getJobDetail() {
		JobDetail jd = new JobDetail(name, Scheduler.DEFAULT_GROUP, LscJob.class);
		JobDataMap jdm = jd.getJobDataMap();
		for(String key: context.keySet()) {
			jdm.put(key, context.get(key));
		}
		return jd;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isNoCreate() {
		return nocreate;
	}
	
	public void setNoCreate(boolean noCreate) {
		this.nocreate = noCreate;
	}

	public boolean isNoUpdate() {
		return noupdate;
	}
	
	public void setNoUpdate(boolean noUpdate) {
		this.noupdate = noUpdate;
	}

	public boolean isNoDelete() {
		return nodelete;
	}
	
	public void setNoDelete(boolean noDelete) {
		this.nodelete = noDelete;
	}

	public boolean isNoChangeId() {
		return nochangeid;
	}
	
	public void setNoChangeId(boolean noChangeId) {
		this.nochangeid = noChangeId;
	}

	public void setTask(TaskType task) {
		this.task = task;
	}
	
	public TaskType getTask() {
		return task;
	}
	
	public Map<String, String> getContext() {
		return context;
	}
	
	public void setContext(Map<String, String> context) {
		this.context = context;
	}
}
