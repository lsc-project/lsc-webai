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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.corelib.components.Grid;
import org.apache.tapestry5.corelib.components.ProgressiveDisplay;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.lsc.utils.ClasstypeFinder;
import org.lsc.webai.base.EditSettings;
import org.lsc.webai.beans.ITriggerTask;
import org.lsc.webai.beans.JobTaskBean;
import org.lsc.webai.services.LscScheduler;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Edit job triggers
 * 
 * @author Sebastien Bahloul &lt;seb@lsc-project.org&gt;
 */
public class EditTriggers extends EditSettings {

	private final Logger LOGGER = LoggerFactory.getLogger(EditTriggers.class);

	@Property
	@Persist
	private JobTaskBean job;
	
	@Property
	@Persist
	private Collection<ITriggerTask> triggers;
	
	@Property
	@Persist
	private ITriggerTask trigger;

	@SuppressWarnings("unused")
	@Property
	@Persist("flash")
	private String message;
	
	@Property
	private String triggerType;

	@SuppressWarnings("unused")
	@Property
	@Persist
	private BeanModel<?> triggerModel;

	@Inject
	private BeanModelSource beanModelSource;

	@Inject
	private ComponentResources resources;
	
	@Inject
	private LscScheduler lscScheduler;
	
	@InjectComponent
	private ProgressiveDisplay addTriggerProgressiveDisplay;
	
	@InjectComponent
	private ProgressiveDisplay editTriggerProgressiveDisplay;
	
	@InjectComponent
	private Grid triggersGrid;

	@SuppressWarnings("unused")
	@Property
	private String triggerIdToEdit;
	
	@SuppressWarnings("unused")
	@Property
	@Persist
	private BeanModel<?> triggersModel;
	
	public Object initialize(JobTaskBean job) {
		this.job = job;
		triggers = new ArrayList<ITriggerTask>();
		for(Trigger quartzTrigger : lscScheduler.getTriggers(job.getName())) {
			triggers.add(lscScheduler.getLscTrigger(quartzTrigger));
		}
		return this;
	}

	@SetupRender
	public void initialize() {
		if(trigger != null) {
			triggerModel = beanModelSource.createEditModel(trigger.getClass(), resources.getMessages());
		}
		triggersModel = beanModelSource.createDisplayModel(ITriggerTask.class, resources.getMessages());
	}

	public Map<String, String> getTriggerTypesModel() {
		Map<String, String> triggerTypesModel = new HashMap<String, String>();
		for (String triggerType : ClasstypeFinder.getInstance().findExtensions(ITriggerTask.class)) {
			triggerTypesModel.put(triggerType, triggerType.substring(triggerType.lastIndexOf(".")+1));
		}
		return triggerTypesModel;
	}

	@OnEvent(component="editTriggerAction", value=EventConstants.ACTION)
	Object OnActionFromEditTriggerAction(String triggerId) {
		for(ITriggerTask triggerToEdit: triggers) {
			if(triggerToEdit.getName().equals(triggerId)) {
				trigger = triggerToEdit;
				break;
			}
		}
		return editTriggerProgressiveDisplay;
	}

	@OnEvent(component="editNewTrigger", value=EventConstants.SUCCESS)
	Object OnSuccessFromEditNewTrigger() {
		trigger.setJob(job);
		triggers.add(trigger);
		lscScheduler.store(trigger);
		return triggersGrid;
	}
	
	@OnEvent(component="triggerTypeForm", value=EventConstants.SUCCESS)
	Object OnSuccessFromTriggerTypeForm() {
		try {
			trigger = (ITriggerTask) Class.forName(triggerType).newInstance();
		} catch (InstantiationException e) {
			LOGGER.error(e.toString(), e);
		} catch (IllegalAccessException e) {
			LOGGER.error(e.toString(), e);
		} catch (ClassNotFoundException e) {
			LOGGER.error(e.toString(), e);
		}
		triggerModel = beanModelSource.createEditModel(trigger.getClass(), resources.getMessages());
		return addTriggerProgressiveDisplay;
	}
	
	@OnEvent(component="deleteTriggerAction", value=EventConstants.ACTION)
	Object OnActionFromDeleteTriggerAction(String triggerId) {
		for(ITriggerTask triggerToEdit: triggers) {
			if(triggerToEdit.getName().equals(triggerId)) {
				trigger = triggerToEdit;
				break;
			}
		}
		lscScheduler.unschedule(trigger);
		triggers.remove(trigger);
		return triggersGrid;
	}
}
