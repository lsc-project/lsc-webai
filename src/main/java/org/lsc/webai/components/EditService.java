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
package org.lsc.webai.components;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.BeforeRenderTemplate;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.corelib.components.BeanEditForm;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.ProgressiveDisplay;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.lsc.configuration.ConnectionType;
import org.lsc.configuration.LscConfiguration;
import org.lsc.configuration.ServiceType;
import org.lsc.configuration.TaskType;
import org.lsc.webai.base.EditSettings;
import org.lsc.webai.pages.EditTask;

@SuppressWarnings("unused")
public class EditService extends EditSettings {

	@Property
	@Persist(PersistenceConstants.FLASH)
	private ServiceType service;

	@InjectPage
	private EditTask editTask;

	@Parameter(required=true)
	private TaskType task;

	@Property
	@Parameter
	private boolean fromSource;

	@Inject
	private BeanModelSource beanModelSource;

	@Inject
	private ComponentResources resources;
	
	@Property
	@Persist(PersistenceConstants.FLASH)
	private BeanModel<?> model;

	@InjectComponent
	private Form serviceTypeForm;
	
	@Property @Persist
	private String connectionName;
	
	@Property @Persist
	private String serviceTypeName;
	
	@InjectComponent
	private ProgressiveDisplay progressiveDisplay;
	
	@InjectComponent
	private BeanEditForm editNewService;
	
	@Property
	private boolean update;
	
	@BeforeRenderTemplate
	public void beforeRender() {
		if(task != null) {
			if(service == null) {
				if(fromSource) {
					service = LscConfiguration.getSourceService(task);
				} else {
					service = LscConfiguration.getDestinationService(task);
				}
			}
			if(service != null) {
				update = true;
				model = beanModelSource.createEditModel(service.getClass(), resources.getMessages());
			}
		}
	}

	@Persist(PersistenceConstants.FLASH)
	private Class<?> correspondingServiceType;
	
	Object onSuccessFromConnectionTypeForm() {
		ConnectionType connection = LscConfiguration.getConnection(connectionName);
		/** TODO
		 * Fix commented code
		 */
//		if(connection != null && connection.getService(fromSource) != null && !Modifier.isAbstract(connection.getService(fromSource).getModifiers())) {
//			correspondingServiceType = connection.getService(fromSource);
//		} else {
//			return null;
//		}
		return serviceTypeForm;
	}
	
	Object onSuccessFromServiceTypeForm() {
		return progressiveDisplay;
	}
	
	@OnEvent(EventConstants.PROGRESSIVE_DISPLAY)
	public Object onEventFromProgressiveDisplay() {
		ConnectionType connection = LscConfiguration.getConnection(connectionName);
		try {
			if(serviceTypeName != null) {
				service = (ServiceType  ) Class.forName(serviceTypeName).newInstance();
				/** TODO
				 * Fix commented code
				 */
//				service.setName(task.getName() + "-" + (fromSource ? "src" : "dst"));
//				service.setConnection(connection);
				model = beanModelSource.createEditModel(service.getClass(), resources.getMessages());
			}
			return this.editNewService;
		} catch (InstantiationException e) {
//			message = "Cannot instantiate source";
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
//	public SelectModel getAvailableServicesModel() {
//		List<OptionModel> options = new ArrayList<OptionModel>();
//		for(String serviceClass : ClasstypeFinder.getInstance().findExtensions(correspondingServiceType)) {
//			options.add(new OptionModelImpl(serviceClass.substring(serviceClass.lastIndexOf(".") + 1), serviceClass));
//		}
//		return new SelectModelImpl(null, options);
//	}

	public Map<String, String> getServiceTypesModel() {
		Map<String, String> servicesModel = new HashMap<String, String>();
		if(correspondingServiceType != null) {
            for(Class<?> serviceClass : getReflections().getSubTypesOf(correspondingServiceType)) {
				servicesModel.put(serviceClass.getName(), serviceClass.getSimpleName());
			}
			if(!Modifier.isAbstract(correspondingServiceType.getModifiers())) {
				String cstname = correspondingServiceType.getName();
				servicesModel.put(cstname, cstname.substring(cstname.lastIndexOf(".") + 1));
			}
		}
		return servicesModel;
	}

	public Map<String, String> getConnectionsModel() {
		Map<String, String> connectionsModel = new HashMap<String, String>();
		for (ConnectionType connection : LscConfiguration.getConnections()) {
			
//			if(connection.getService(fromSource) != null) {
//				connectionsModel.put(connection.getName(), connection.getName());
//			}
		}
		return connectionsModel;
	}
	
	/** TODO
	 * Fix commented code
	 */
//	Object onSuccessFromEditNewService() {
//		return onSuccessFromEditService();
//	}
//	Object onSuccessFromEditExistingService() {
//		return onSuccessFromEditService();
//	}
//
//	Object onSuccessFromEditService() {
//		if (fromSource) {
//			task.setSourceService(service);
//		} else {
//			task.setDestinationService(service);
//		}
//		return editTask.initialize(task);
//	}
//	
//	Object onActionFromEditTask() {
//		return  editTask.initialize(task);
//	}
}
