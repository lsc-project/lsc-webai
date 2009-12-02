/*
 ****************************************************************************
 * Ldap Synchronization Connector provides tools to synchronize
 * electronic identities from a list of data sources including
 * any database with a JDBC connector, another LDAP directory,
 * flat files...
 *
 *                  ==LICENSE NOTICE==
 * 
 * Copyright (c) 2008, LSC Project 
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
 *               (c) 2008 - 2009 LSC Project
 *         Sebastien Bahloul <seb@lsc-project.org>
 *         Thomas Chemineau <thomas@lsc-project.org>
 *         Jonathan Clarke <jon@lsc-project.org>
 *         Remy-Christophe Schermesser <rcs@lsc-project.org>
 ****************************************************************************
 */
package org.lsc.webui.beans.task;

import java.util.Properties;

import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.beaneditor.Validate;
import org.lsc.Configuration;
import org.lsc.webui.beans.service.DataService;

/**
 * LSC Task
 * @author Sebastien Bahloul &lt;seb@lsc-project.org&gt;
 */
public class Task {

	public static final String TASKS_PREFIX = "lsc.tasks";
	
/*	@Validate("required")*/
	private String name;

	@Validate("required")
	@Parameter
	private TaskType type;
	
	@Parameter
	private String bean;
	
	@Parameter
	private String object;

	@Validate("required")
	private SourceServiceType sourceServiceDescription;
	private DataService sourceService;

	@Validate("required")
	private TargetServiceType targetServiceDescription;
	private DataService targetService;

	public Task() {

	}
	
	public Task(String taskName) {
		name = taskName;
		setProperties(Configuration.getAsProperties(TASKS_PREFIX + "." + taskName));
	}
	
	public void setProperties(Properties properties)  {
		type = TaskType.valueOf(properties.getProperty("type").toUpperCase());
		sourceServiceDescription = SourceServiceType.valueOf(properties.getProperty("srcService")); 
		targetServiceDescription = TargetServiceType.valueOf(properties.getProperty("dstService"));
		bean = properties.getProperty("bean");
		object = properties.getProperty("object");
	}

	public TaskType getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	@Validate("required")
	public String getBean() {
		return bean;
	}

	@Validate("required")
	public String getObject() {
		return object;
	}

	public SourceServiceType getSourceServiceDescription() {
		return sourceServiceDescription;
	}

	public TargetServiceType getTargetServiceDescription() {
		return targetServiceDescription;
	}

	public void setType(TaskType type) {
		this.type = type;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSourceServiceDescription(
			SourceServiceType sourceServiceDescription) {
		this.sourceServiceDescription = sourceServiceDescription;
	}

	public void setTargetServiceDescription(
			TargetServiceType targetServiceDescription) {
		this.targetServiceDescription = targetServiceDescription;
	}

	public DataService getSourceService() {
		return sourceService;
	}

	public void setSourceService(DataService sourceService) {
		this.sourceService = sourceService;
	}

	public DataService getTargetService() {
		return targetService;
	}

	public void setTargetService(DataService targetService) {
		this.targetService = targetService;
	}

	public void setBean(String bean) {
		this.bean = bean;
	}

	public void setObject(String object) {
		this.object = object;
	}
}
