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
package org.lsc.webai.components;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.ajax.MultiZoneUpdate;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.corelib.components.BeanEditForm;
import org.apache.tapestry5.corelib.components.Grid;
import org.apache.tapestry5.corelib.components.ProgressiveDisplay;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.annotations.InjectService;
import org.lsc.configuration.objects.LscConfiguration;
import org.lsc.configuration.objects.Task;
import org.lsc.service.IAsynchronousService;
import org.lsc.webai.base.EditSettings;
import org.lsc.webai.beans.ITriggerTask;
import org.lsc.webai.beans.JobTaskBean;
import org.lsc.webai.pages.EditTriggers;
import org.lsc.webai.services.LscScheduler;
import org.quartz.JobDetail;
import org.quartz.SchedulerFactory;

/**
 * Edit jobs
 * 
 * @author Sebastien Bahloul &lt;seb@lsc-project.org&gt;
 */
public class EditJobs extends EditSettings {

	@Persist
	private Map<String, JobTaskBean> jobs;

	@Persist
	private Map<String, Map<String, ITriggerTask>> triggers;

	@Property
	@Persist(PersistenceConstants.FLASH)
	private JobTaskBean job;

	@SuppressWarnings("unused")
	@Property
	@Persist("flash")
	private String message;

	@InjectComponent
	private BeanEditForm addJob;

	@InjectComponent
	private ProgressiveDisplay progressiveDisplay;

	@InjectComponent
	private Grid jobsGrid;

	@Property
	@Persist
	private String taskName;

	@InjectService("scheduler")
	private LscScheduler lscScheduler;

	@InjectPage
	private EditTriggers editTriggers;

	@InjectComponent
	private Zone jobZone;

	@InjectComponent
	private Zone jobsZone;

	@SetupRender
	public void setupRender() {
		if (jobs == null) {
			// Load from Scheduler factory
			jobs = new HashMap<String, JobTaskBean>();
			SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
			for (JobDetail jd : lscScheduler.getJobDetails()) {
				jobs.put(jd.getName(), JobTaskBean.getInstance(jd));
			}
		}
	}

	Object onSuccessFromSelectTask() {
		job = new JobTaskBean();
		job.setTask(LscConfiguration.getTask(taskName));
		int jobId = 1;
		while (jobs.get(taskName + "-" + jobId) != null) {
			jobId++;
		}
		job.setName(taskName + "-" + jobId);
		return progressiveDisplay;
	}

	Object onActionFromEditJob(String jobName) {
		job = jobs.get(jobName);
		return progressiveDisplay;
	}
	
	Object onActionFromProgressiveDisplay() {
		return addJob;
	}

	@OnEvent(component = "deleteJob", value = EventConstants.ACTION)
	Object onActionFromDeleteJob(String jobName) {
		job = jobs.get(jobName);
		if (job != null) {
			jobs.remove(jobName);
		}
		lscScheduler.unschedule(jobName);
		job = null;
		return jobsGrid;
	}

	public boolean isJobScheduled(String jobName) {
		return lscScheduler.isStarted() && lscScheduler.isJobScheduled(jobName);
	}

	Object onActionFromSyncJob(String jobName) {
		// JobTaskBean jobC = jobs.get(jobName);
		// try {
		// if (jobC != null) {
		// lscScheduler.scheduleSync(jobC.getJobDetail(),
		// new CronTrigger(jobC.getName(), Scheduler.DEFAULT_GROUP,
		// jobC.getCronStylePlanification()));
		// }
		// } catch (SchedulerException se) {
		// message = se.toString();
		// LOGGER.error(se.toString(), se);
		// } catch (ParseException pe) {
		// message = "Unable to use the provided cron style planification : " +
		// pe.getMessage();
		// LOGGER.error(pe.toString(), pe);
		// }
		return jobsGrid;
	}

	Object onSuccessFromAddForm() {
		job = new JobTaskBean();
		taskName = null;
		return addJob;
	}

	// @OnEvent(component="addJob", value=EventConstants.ACTION)
	// Object onActionFromAddJob() {
	// JobTaskBean jobC = jobs.get(job.getName());
	// if (jobC == null) {
	// jobs.put(job.getName(), job);
	// }
	// job.setTask(LscConfiguration.getTask(taskName));
	// job = null;
	// return jobsGrid;
	// }

	public Map<String, String> getTasksModel() {
		Map<String, String> tasksModel = new HashMap<String, String>();
		for (Task task : LscConfiguration.getTasks()) {
			if (task.getSourceService() != null
					&& IAsynchronousService.class.isAssignableFrom(task.getSourceService().getImplementation())) {
				tasksModel.put(task.getName(), task.getName());
			}
		}
		return tasksModel;
	}

	public Collection<JobTaskBean> getJobs() {
		return jobs.values();
	}

	public Object onActionFromEditTriggers(String jobName) {
		JobTaskBean jtb = jobs.get(jobName);
		lscScheduler.store(jtb);
		return editTriggers.initialize(jtb);
	}

	/* ***********************************************************
	 * Event handlers&processing
	 * ***********************************************************
	 */

	/**
	 * Analyze a delete action request. If the request is valid, fire a new
	 * event so that top level element can handle the processing of the request.
	 */
    Object onDeleteFromContextEditor(String componentId, String valueIndex, String objectId) {
		job.getContext().remove(job.getContext().keySet().toArray()[Integer.parseInt(valueIndex)]);
		return addJob;
    }

	Object onAddFromContextEditor(String context) {
		if(!job.getContext().containsKey("new-key")) {
			job.getContext().put("new-key", "new-value");
		}
		return addJob;
	}
	
	MultiZoneUpdate getJobsZones() {
		MultiZoneUpdate mzu = new MultiZoneUpdate(jobsZone.getClientId(), jobsGrid);
		mzu.add(jobZone.getClientId(), progressiveDisplay);
		return mzu;
	}
	
	Object onSuccessFromAddJob() {
		jobs.put(job.getName(), job);
		job = null;
		return this;//getJobsZones();
	}
}
