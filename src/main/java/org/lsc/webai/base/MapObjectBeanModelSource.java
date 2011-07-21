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

 *    * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *    * Neither the name of the LSC Project nor the names of its
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
package org.lsc.webai.base;

import java.util.Map;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.beaneditor.PropertyModel;
import org.apache.tapestry5.services.BeanModelSource;

/**
 * 
 * @author Sebastien Bahloul &lt;seb@lsc-project.org&gt;
 * Inpired by ListBeanModelSource written by F. Armand in LinID Directory Manager
 * @param <K> keys type
 * @param <V> values type
 */
public class MapObjectBeanModelSource<K, V> {
	
	public static String CONTEXT_SEPARATOR = "/";
	
	/*
	 * The beanModelSource service to use to get the
	 * prototype bean model
	 */
	private final BeanModelSource beanModelSource;
	
	/*
	 * This bean editor generate a property field for each element
	 * of the list to edit. This name is used to generate property
	 * id. For each property, the suffix "_i" is add, where "_i" is the
	 * index of the matching property in the list.
	 */
	private final String propertyIdPrefix;

	public MapObjectBeanModelSource(final BeanModelSource beanModelSource, final String propertyIdPrefix) {
		this.beanModelSource = beanModelSource;
		this.propertyIdPrefix = propertyIdPrefix;
	}

	public BeanModel<Map> generateKeyBeanModel(Map<K, V> map, ComponentResources resources, 
			String dataType, String label, boolean edit) {
		return generateBeanModel(map, resources, dataType, label, false, edit, true);
	}

	public BeanModel<Map> generateValueBeanModel(Map<K, V> map, ComponentResources resources, 
			String dataType, String label, boolean edit) {
		return generateBeanModel(map, resources, dataType, label, false, edit, false);
	}
	
	/**
	 * Generate a bean model for just ONE property on a bean, the property
	 * MUST be a Map.
	 * Each element of the either keys is considered as a property for the bean.
	 * @param list the list of string for which we want to create a bean editor
	 * @param resources the component resources of the component using this bean model source
	 * @param edit true if it must return an edit model, false for a display model
	 * @param isKey true if the required bean model must be generated for map keys (i.e. must be false for map values)
	 * @return the bean model that enable the edition 
	 */
	public BeanModel<Map> generateBeanModel(Map<K, V> map, ComponentResources resources,
			String dataType, String label, boolean printIndex, boolean edit, boolean isKey) {
		
		BeanModel<Map> model = null;
		if (edit) {
			model = beanModelSource.createEditModel(Map.class,resources.getMessages());
		} else {
			model = beanModelSource.createDisplayModel(Map.class, resources.getMessages());
		}

		//remove all other property
		for( String s : model.getPropertyNames() ) {
			model.exclude(s);
		}

		for( int i=0; i<map.size(); i++ ) {
			Class clazz = (isKey ? map.keySet().iterator().next().getClass() : map.get(map.keySet().iterator().next()).getClass());
			PropertyModel propertyModel = model.add(propertyIdPrefix + CONTEXT_SEPARATOR + i, 
					new TreeMapObjectPropertyConduit(i,clazz,isKey)).dataType(dataType);
			if(null != label) {
				String currentLabel = label;
				if(printIndex) {
					currentLabel += " " + i; 
				}
				propertyModel.label(currentLabel);
			}
		}
		return model;
	}

	public String getPropertyIdPrefix() {
		return propertyIdPrefix;
	}
	
}
