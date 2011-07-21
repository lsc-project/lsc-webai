/*
 ****************************************************************************
 * Ldap Synchronization Connector provid es tools to synchronize
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

import java.util.HashMap;
import java.util.Map;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.annotations.BeforeRenderTemplate;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.Retain;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.corelib.components.BeanEditForm;
import org.apache.tapestry5.corelib.components.ProgressiveDisplay;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.lsc.configuration.objects.SyncOptions;
import org.lsc.configuration.objects.Task;
import org.lsc.webai.base.EditSettings;
import org.lsc.webai.pages.EditTask;
import org.lsc.webai.utils.ClasstypeFinder;

/**
 * Edit synchronization options
 * @author Sebastien Bahloul &lt;seb@lsc-project.org&gt;
 */
public class EditSyncOptions extends EditSettings {

	@Property
	@Persist
	private SyncOptions syncOptions;

	@InjectPage
	private EditTask editTask;

	@Parameter
	private Task task;

	@Inject
	private BeanModelSource beanModelSource;

	@Inject
	private ComponentResources resources;
	
	@SuppressWarnings("unused")
	@Property
	@Retain
	private BeanModel<?> model;

	@Property
	@Persist
	private String syncOptionsType;
	
	@InjectComponent
	private ProgressiveDisplay progressiveDisplay;
	
	@InjectComponent
	private BeanEditForm editNewSyncOptions;
	
	@Property
	private boolean update;
	
	@BeforeRenderTemplate
	public void beforeRender() {
		if(task.getSyncOptions() != null) {
			this.syncOptions = task.getSyncOptions();
			this.update = true;
			model = beanModelSource.createEditModel(syncOptions.getClass(), resources.getMessages());
		}
	}

	Object onSuccessFromTypeForm() {
		return progressiveDisplay;
	}
	
	@OnEvent(EventConstants.PROGRESSIVE_DISPLAY)
	public Object onEventFromProgressiveDisplay() {
//		Connection connection = LscConfiguration.getInstance().getConnection(syncOptionsType);
		try {
			if(syncOptionsType != null) {
				syncOptions = (SyncOptions) Class.forName(syncOptionsType).newInstance();
				model = beanModelSource.createEditModel(syncOptions.getClass(), resources.getMessages());
			} else {
				return null;
			}
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return this.editNewSyncOptions;
	}

	public Map<String, String> getSyncOptionsTypeModel() {
		Map<String, String> syncOptionsTypeModel = new HashMap<String, String>();
		for (String className: ClasstypeFinder.getInstance().findExtensions(SyncOptions.class)) {
			syncOptionsTypeModel.put(className, className.substring(className.lastIndexOf(".")+1));
		}
		return syncOptionsTypeModel;
	}

	public Object onSuccessFromEditNewSyncOptions() {
		return onSuccessFromEditSyncOptions();
	}
	public Object onSuccessFromEditExistingSyncOptions() {
		return onSuccessFromEditSyncOptions();
	}

	public Object onSuccessFromEditSyncOptions() {
		task.setSyncOptions(syncOptions);
		return editTask.initialize(task);
	}
}
