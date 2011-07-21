package org.lsc.webai.beans;

import java.util.Date;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.beaneditor.Validate;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

public class SimpleTriggerTask implements ITriggerTask {

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
	private long repeatInterval;

	@Property
	private int repeatCount;

	private JobTaskBean job;
	
	@Override
	public Trigger getQuartzTrigger() {
		SimpleTrigger st = new SimpleTrigger(getName());
		st.setStartTime(startTime);
		if(endTime != null) {
			st.setEndTime(endTime);
		}
		st.setRepeatInterval(repeatInterval);
		st.setRepeatCount(repeatCount);
		st.setFireInstanceId(id);
		st.setJobName(getJob().getName());
		return st;
	}

	@Override
	public Class<? extends Trigger> getQuartzTriggerType() {
		return SimpleTrigger.class;
	}

	@Override
	public void loadFromQuartzTrigger(Trigger quartzTrigger) {
		if(quartzTrigger.getClass() == getQuartzTriggerType()) {
			this.name = quartzTrigger.getName();
			this.id = quartzTrigger.getFireInstanceId();
			this.startTime = ((SimpleTrigger)quartzTrigger).getStartTime();
			this.endTime = ((SimpleTrigger)quartzTrigger).getEndTime();
			this.repeatCount = ((SimpleTrigger)quartzTrigger).getRepeatCount();
			this.repeatInterval = ((SimpleTrigger)quartzTrigger).getRepeatInterval();
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

	public long getRepeatInterval() {
		return repeatInterval;
	}

	public void setRepeatInterval(long repeatInterval) {
		this.repeatInterval = repeatInterval;
	}

	public int getRepeatCount() {
		return repeatCount;
	}

	public void setRepeatCount(int repeatCount) {
		this.repeatCount = repeatCount;
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
