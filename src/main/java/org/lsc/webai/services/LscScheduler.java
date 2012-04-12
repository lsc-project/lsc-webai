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
package org.lsc.webai.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.lsc.webai.base.EditSettings;
import org.lsc.webai.beans.ITriggerTask;
import org.lsc.webai.beans.JobTaskBean;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.UnableToInterruptJobException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Sebastien Bahloul &lt;seb@lsc-project.org&gt;
 */
public class LscScheduler {

	private final Logger LOGGER = LoggerFactory.getLogger(LscScheduler.class);

	private SchedulerFactory schedFactory;
	
	private Scheduler scheduler ;
	
	private Map<String, Class<? extends ITriggerTask>> lscTriggerTypes;

	private Map<Class<? extends Trigger>, Class<? extends ITriggerTask>> quartzTypesEquivalence;
	
	public LscScheduler() throws SchedulerException {
		this.lscTriggerTypes = new HashMap<String, Class<? extends ITriggerTask>>();
		this.quartzTypesEquivalence = new HashMap<Class<? extends Trigger>, Class<? extends ITriggerTask>>();
		schedFactory = new StdSchedulerFactory();
		scheduler = schedFactory.getScheduler();
		Set<Class<? extends ITriggerTask>> lscTriggerTypes = EditSettings.getReflections().getSubTypesOf(ITriggerTask.class);
		try {
			for(Class<? extends ITriggerTask> lscTriggerTypeClass: lscTriggerTypes) {
				this.lscTriggerTypes.put(lscTriggerTypeClass.getName(), lscTriggerTypeClass);
				this.quartzTypesEquivalence.put(lscTriggerTypeClass.newInstance().getQuartzTriggerType(), lscTriggerTypeClass);
			}
		} catch (InstantiationException e) {
			LOGGER.error(e.toString(), e);
		} catch (IllegalAccessException e) {
			LOGGER.error(e.toString(), e);
		}
	}
	
	public ITriggerTask getLscTrigger(Trigger quartzTrigger) {
		Class<? extends ITriggerTask> lscTriggerImpl = quartzTypesEquivalence.get(quartzTrigger.getClass());
		ITriggerTask itt = null;
		try {
			itt = (ITriggerTask) lscTriggerImpl.newInstance();
			itt.loadFromQuartzTrigger(quartzTrigger);
		} catch (InstantiationException e) {
			LOGGER.error(e.toString(), e);
		} catch (IllegalAccessException e) {
			LOGGER.error(e.toString(), e);
		}
		return itt;
	}

	public void scheduleSync(JobDetail jobDetail, Trigger trigger) throws SchedulerException {
		scheduler.scheduleJob(jobDetail, trigger);

		if(!scheduler.isStarted()) {
			scheduler.start();
		}
	}
	
	public boolean isStarted() {
		try {
			return scheduler.isStarted();
		} catch (SchedulerException e) {
			LOGGER.error(e.toString(), e);
		}
		return false;
	}
	
	public boolean isJobScheduled(String jobName) {
		Trigger trigger;
		try {
			trigger = scheduler.getTrigger(jobName, Scheduler.DEFAULT_GROUP);
			return trigger != null;
		} catch (SchedulerException e) {
			LOGGER.error(e.toString(), e);
		}
		return false;
	}

	public Collection<JobDetail> getJobDetails() {
		Collection<JobDetail> jobDetails = new ArrayList<JobDetail>();
		try {
			for(String jobName : scheduler.getJobNames(Scheduler.DEFAULT_GROUP)) {
				jobDetails.add(scheduler.getJobDetail(jobName, Scheduler.DEFAULT_GROUP));
			}
		} catch(SchedulerException se) {
			LOGGER.error(se.toString(), se);
		}
		return jobDetails;
	}

	public void unschedule(String jobName) {
		try {
			if(scheduler.getTrigger(jobName, Scheduler.DEFAULT_GROUP) != null) {
				scheduler.getTrigger(jobName, Scheduler.DEFAULT_GROUP).setEndTime(new Date());
				scheduler.interrupt(jobName, Scheduler.DEFAULT_GROUP);
			}
		} catch (UnableToInterruptJobException e) {
			LOGGER.error(e.toString(), e);
		} catch (SchedulerException e) {
			LOGGER.error(e.toString(), e);
		}
	}

	public Trigger[] getTriggers(String jobName) {
		try {
			return scheduler.getTriggersOfJob(jobName, Scheduler.DEFAULT_GROUP);
		} catch (SchedulerException e) {
			LOGGER.error(e.toString(), e);
		}
		return null;
	}

	public void store(ITriggerTask trigger) {
		try {
			scheduler.scheduleJob(trigger.getQuartzTrigger());
		} catch (SchedulerException e) {
			LOGGER.error(e.toString(), e);
		}
	}

	public void store(JobTaskBean jtb) {
		try {
			scheduler.addJob(jtb.getJobDetail(), true);
		} catch (SchedulerException e) {
			LOGGER.error(e.toString(), e);
		}
	}

	public void unschedule(ITriggerTask trigger) {
		try {
			if(scheduler.getTrigger(trigger.getName(), Scheduler.DEFAULT_GROUP) != null) {
				scheduler.unscheduleJob(trigger.getName(), Scheduler.DEFAULT_GROUP);
			}
		} catch (UnableToInterruptJobException e) {
			LOGGER.error(e.toString(), e);
		} catch (SchedulerException e) {
			LOGGER.error(e.toString(), e);
		}
	}
}
