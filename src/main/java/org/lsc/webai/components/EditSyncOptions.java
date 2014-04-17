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

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
import org.apache.tapestry5.corelib.components.Grid;
import org.apache.tapestry5.corelib.components.ProgressiveDisplay;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.lsc.configuration.DatasetType;
import org.lsc.configuration.LscConfiguration;
import org.lsc.configuration.PropertiesBasedSyncOptionsType;
import org.lsc.configuration.SyncOptionsType;
import org.lsc.configuration.TaskType;
import org.lsc.exception.LscConfigurationException;
import org.lsc.webai.base.EditSettings;
import org.lsc.webai.pages.EditTask;

/**
 * Edit synchronization options
 * @author Sebastien Bahloul &lt;seb@lsc-project.org&gt;
 */
public class EditSyncOptions extends EditSettings {

	@Property
	@Persist(PersistenceConstants.FLASH)
	private SyncOptionsType syncOptions;

	@InjectPage
	private EditTask editTask;

	@Parameter
	private TaskType task;

	@Inject
	private BeanModelSource beanModelSource;

	@Inject
	private ComponentResources resources;
	
	@InjectComponent
	private Grid datasetsGrid;
	
	@Property
    @Persist(PersistenceConstants.FLASH)
	private BeanModel<?> model;

    @Property
    @Persist(PersistenceConstants.FLASH)
    private DatasetType datasetGridRow;

    @Property
    @Persist(PersistenceConstants.FLASH)
    private DatasetType dataset;

    @Property
    @Persist(PersistenceConstants.FLASH)
    private BeanModel<?>  datasetModel;

	@Property
    @Persist(PersistenceConstants.FLASH)
	private String syncOptionsType;
	
	@InjectComponent
	private ProgressiveDisplay progressiveDisplay;
	
	@InjectComponent
	private BeanEditForm editNewSyncOptions;
	
	@InjectComponent
	private BeanEditForm editNewDataset;
	
	@SuppressWarnings("unused")
    @Property
	private boolean update;
	
	@BeforeRenderTemplate
	public void beforeRender() {
		if(task.getForceSyncOptions() != null) {
			this.syncOptions = task.getForceSyncOptions();
			this.update = true;
		} else if (task.getPropertiesBasedSyncOptions() != null) {
            this.syncOptions = task.getPropertiesBasedSyncOptions();
            this.update = true;
        } else if (task.getPluginSyncOptions() != null) {
            this.syncOptions = task.getPluginSyncOptions();
            this.update = true;
		}
	    if(syncOptions != null) {
            model = beanModelSource.createEditModel(syncOptions.getClass(), resources.getMessages());
            model.exclude("id"); 
	    }
 	}

	
	Object onSuccessFromTypeForm() {
		return progressiveDisplay;
	}
	
	@OnEvent(EventConstants.PROGRESSIVE_DISPLAY)
	public Object onEventFromProgressiveDisplay() {
		try {
			if(syncOptionsType != null) {
				syncOptions = (SyncOptionsType) Class.forName(syncOptionsType).newInstance();
				model = beanModelSource.createEditModel(syncOptions.getClass(), resources.getMessages());
				model.exclude("id"); 
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
		for (Class<? extends SyncOptionsType> syncOptionsClass: getReflections().getSubTypesOf(SyncOptionsType.class)) {
			syncOptionsTypeModel.put(syncOptionsClass.getName(), syncOptionsClass.getSimpleName());
		}
		return syncOptionsTypeModel;
	}
	
	public Object onSuccessFromEditNewSyncOptions() throws LscConfigurationException {
		return onSuccessFromEditSyncOptions();
	}
	public Object onSuccessFromEditExistingSyncOptions() throws LscConfigurationException {
		return onSuccessFromEditSyncOptions();
	}

	public Object onSuccessFromEditSyncOptions() throws LscConfigurationException {
	    LscConfiguration.setSyncOptions(task, syncOptions);
		return editTask.initialize(task);
	}

    public boolean isPropertiesBasedSyncOptions() {
        return syncOptions != null && syncOptions instanceof PropertiesBasedSyncOptionsType;
    }

    public Set<DatasetType> getPbsoDataset() {
        if(syncOptions == null) {
            syncOptions = task.getPropertiesBasedSyncOptions();
        }
        TreeSet<DatasetType> datasetSet = new TreeSet<DatasetType>(new DatasetComparator());
        datasetSet.addAll(task.getPropertiesBasedSyncOptions().getDataset());
        return datasetSet;
    }
    
    @OnEvent(component="editNewDataset", value=EventConstants.CANCELED)
    public Object onCancelFromEditNewDataset() {
        dataset = null;
        return datasetsGrid;
    }
    
    @OnEvent(component="editNewDataset", value=EventConstants.SUCCESS)
    public Object onSuccessFromEditNewDataset() {
        if(dataset != null) {
            DatasetType matchedDataset = null;
            for(DatasetType localDataset: getPbsoDataset()) {
                if(localDataset.getName().equals(dataset.getName())) {
                    matchedDataset = localDataset;
                    break;
                }
            }
            if(matchedDataset != null) {
               getPbsoDataset().remove(matchedDataset); 
            }
             getPbsoDataset().add(dataset);
        }
        return datasetsGrid;
    }
    
    @OnEvent(component="deleteDatasetAction", value=EventConstants.ACTION)
    Object OnActionFromDeleteTriggerAction(String datasetName) {
        for(DatasetType datasetToEdit: getPbsoDataset()) {
            if(datasetToEdit.getName().equals(datasetName)) {
                dataset = datasetToEdit;
                break;
            }
        }
        getPbsoDataset().remove(dataset);
        return datasetsGrid;
    }

    @OnEvent(component="editDatasetAction", value=EventConstants.ACTION)
    Object OnActionFromEditTriggerAction(String triggerId) {
        for(DatasetType datasetToEdit: getPbsoDataset()) {
            if(datasetToEdit.getName().equals(triggerId)) {
                dataset = new DatasetType();
                dataset.setCreateValues(datasetToEdit.getCreateValues());
                dataset.setDefaultValues(datasetToEdit.getDefaultValues());
                dataset.setDelimiter(datasetToEdit.getDelimiter());
                dataset.setForceValues(datasetToEdit.getForceValues());
                dataset.setId(datasetToEdit.getId());
                dataset.setName(datasetToEdit.getName());
                dataset.setPolicy(datasetToEdit.getPolicy());
                break;
            }
        }
        return editNewDataset;
    }
}

class DatasetComparator implements Comparator<DatasetType> {

    @Override
    public int compare(DatasetType arg0, DatasetType arg1) {
        return arg0.getName().compareTo(arg1.getName());
    }
    
}