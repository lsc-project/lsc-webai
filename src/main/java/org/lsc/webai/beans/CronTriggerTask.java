package org.lsc.webai.beans;

import java.text.ParseException;
import java.util.Date;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.beaneditor.Validate;
import org.quartz.CronTrigger;
import org.quartz.Scheduler;
import org.quartz.Trigger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CronTriggerTask implements ITriggerTask {

    private static Logger log = LoggerFactory.getLogger(CronTriggerTask.class);

	@Property
	private String id;

	@Validate("required")
	@Property
	private String name;
	
	@Validate("required")
	@Property
	private Date startTime;

	@Property
	private Date endTime;

	@Property
	@Validate("cronexpr")
	private String cronExpression;

	private JobTaskBean job;

	@Override
	public Trigger getQuartzTrigger() {
		CronTrigger ct = null;
		try {
			ct = new CronTrigger(getName(), Scheduler.DEFAULT_GROUP, cronExpression);
			ct.setStartTime(startTime);
			if(endTime != null) {
				ct.setEndTime(endTime);
			}
			ct.setFireInstanceId(id);
			ct.setJobName(getJob().getName());
		} catch (ParseException e) {
            log.error("Unable to parse the cron expression: " + cronExpression);
            log.debug(e.toString(), e);
            return null;
        }
		return ct;
	}

	@Override
	public Class<? extends Trigger> getQuartzTriggerType() {
		return CronTrigger.class;
	}

	@Override
	public void loadFromQuartzTrigger(Trigger quartzTrigger) {
		if(quartzTrigger.getClass() == getQuartzTriggerType()) {
			this.name = quartzTrigger.getName();
			this.id = quartzTrigger.getFireInstanceId();
			this.startTime = ((CronTrigger)quartzTrigger).getStartTime();
			this.endTime = ((CronTrigger)quartzTrigger).getEndTime();
			this.cronExpression = ((CronTrigger)quartzTrigger).getCronExpression();
		} else {
			throw new UnsupportedOperationException("Only " + getQuartzTriggerType() + " supported !");
		}
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	@Override
	public JobTaskBean getJob() {
		return job;
	}

	@Override
	public void setJob(JobTaskBean job) {
		this.job = job;
	}
}
