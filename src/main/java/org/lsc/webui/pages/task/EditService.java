package org.lsc.webui.pages.task;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.Retain;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.lsc.webui.beans.service.DataService;
import org.lsc.webui.beans.service.DataServiceImpl;
import org.lsc.webui.beans.task.SourceServiceType;
import org.lsc.webui.beans.task.TargetServiceType;
import org.lsc.webui.beans.task.Task;

public class EditService {

	@Persist
	private DataService service;

	@InjectPage
	private EditTask editTask;

	@Persist
	private Task task;

	@Property
	@Persist
	private boolean fromSource;

	@Inject
	private BeanModelSource beanModelSource;

	@Inject
	private ComponentResources resources;

	@Property(write = false)
	@Retain
	private BeanModel<?> model;

	public Object initialize(Task task,
			SourceServiceType sourceServiceDescription) {
		if (task.getSourceService() != null) {
			service = task.getSourceService();
			model = beanModelSource.createEditModel(service.getClass(), resources.getMessages());
		} else {
			service = new DataServiceImpl();
			model = beanModelSource.createEditModel(DataServiceImpl.class, resources.getMessages());
		}
		fromSource = true;
		this.task = task;
		return this;
	}

	public Object initialize(Task task,
			TargetServiceType targetServiceDescription) {
		if (task.getSourceService() != null) {
			service = task.getTargetService();
		} else {
			service = new DataServiceImpl();
		}
		fromSource = false;
		this.task = task;
		return this;
	}

	public Object onSuccess() {
		if (fromSource) {
			task.setSourceService(service);
		} else {
			task.setTargetService(service);
		}
		return editTask.initialize(task);
	}

	public DataService getService() {
		return service;
	}

	public void setService(DataService service) {
		this.service = service;
	}

}
