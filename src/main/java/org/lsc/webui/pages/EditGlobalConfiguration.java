/*
 ****************************************************************************
 * Ldap Synchronization Connector provides tools to synchronize
 * electronic identities from a list of data sources including
 * any database with a JDBC connector, another LDAP directory,
 * flat files...
 *
 *                  ==LICENSE NOTICE==
 * 
 * Copyright (c) 2009, LSC Project 
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
 *               (c) 2008 - 2009 LSC Project
 *         Sebastien Bahloul <seb@lsc-project.org>
 *         Thomas Chemineau <thomas@lsc-project.org>
 *         Jonathan Clarke <jon@lsc-project.org>
 *         Remy-Christophe Schermesser <rcs@lsc-project.org>
 ****************************************************************************
 */
package org.lsc.webui.pages;

import java.util.Properties;

import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.lsc.Configuration;
import org.lsc.webui.pages.databaseSettings.EditDatabaseSettings;
import org.lsc.webui.pages.directorySettings.EditDirectorySettings;

/**
 * Edit global parameters page.
 * 
 * @author Sebastien Bahloul &lt;seb@lsc-project.org&gt;
 */
public class EditGlobalConfiguration {
	
	@Property
	private Properties sourceDirectoryProperties;
	
	@Property
	private Properties databaseProperties;
	
	@Property
	private Properties targetDirectoryProperties;
	
	@InjectPage
	private EditDirectorySettings editDirectorySettings;

	@InjectPage
	private EditDatabaseSettings editDatabaseSettings;

	@InjectPage
	private ErrorPage errorPage;

	public EditGlobalConfiguration() {
		Configuration.setLocation("etc/");

		if(sourceDirectoryProperties == null) {
			sourceDirectoryProperties = Configuration.getSrcProperties();
			if(sourceDirectoryProperties.size() == 0) {
				sourceDirectoryProperties = null;
			}
		}
		if(targetDirectoryProperties == null) {
			targetDirectoryProperties = Configuration.getDstProperties();
			if(targetDirectoryProperties.size() == 0) {
				targetDirectoryProperties = null;
			}
		}
		if(databaseProperties == null) {
			databaseProperties = new Properties();
			if(databaseProperties.size() == 0) {
				databaseProperties = null;
			}
		}
	}
	
	public Object onActionFromEditSourceDirectorySettings() {
		return editDirectorySettings.initialize("src", sourceDirectoryProperties);  
	}

	public Object onActionFromCreateSourceDirectorySettings() {
		return editDirectorySettings.initialize("src", sourceDirectoryProperties);  
	}

	public Object onActionFromEditTargetDirectorySettings() {
		return editDirectorySettings.initialize("dst", targetDirectoryProperties);  
	}

	public Object onActionFromEditDatabaseSettings() {
		return editDatabaseSettings.initialize();
	}
}
