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
package org.lsc.webui.pages.databaseSettings;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.lsc.webui.base.EditSettings;
import org.lsc.webui.beans.database.DatabaseSettings;
import org.lsc.webui.pages.ErrorPage;

/**
 * Edit directory settings page.
 * 
 * @author Sebastien Bahloul &lt;seb@lsc-project.org&gt;
 */
public class EditDatabaseSettings extends EditSettings {

	public static final String DB_DRIVER 	= "driver";
	public static final String DB_URL 		= "url";
	public static final String DB_USERNAME 	= "username";
	public static final String DB_PASSWORD 	= "password";

	@Property
	@Persist
	private DatabaseSettings databaseSettings;

	@InjectPage
	private ErrorPage errorPage;
	
	@Property
	@Persist
	private String message;

	/**
	 * Initialize
	 * 
	 * @param properties
	 * @return
	 */
	public Object initialize() {
		Properties localProperties = new Properties();
		message = "";
		try {
			localProperties.load(new FileInputStream("database.properties"));
		} catch (IOException e) {
			return errorPage.initialize(e);
		}
		databaseSettings = new DatabaseSettings();
		if (localProperties != null) {
			databaseSettings.setDriver(localProperties.getProperty(DB_DRIVER));
			databaseSettings.setUrl(localProperties.getProperty(DB_URL));
			databaseSettings.setUsername(localProperties.getProperty(DB_USERNAME));
			databaseSettings.setPassword(localProperties.getProperty(DB_PASSWORD));
		}
		return this;
	}
	
	public Object onSuccess() {
		Properties properties = new Properties();
		properties.setProperty(DB_DRIVER, 	databaseSettings.getDriver());
		properties.setProperty(DB_URL, 		databaseSettings.getUrl());
		properties.setProperty(DB_USERNAME, nvl(databaseSettings.getUsername(), ""));
		properties.setProperty(DB_PASSWORD, nvl(databaseSettings.getPassword(), ""));
		try {
			properties.store(new FileWriter("database.properties"), HEADER);
			message = SUCCESSFULLY;
		} catch (IOException e) {
			return errorPage.initialize(e);
		}
		return this;
	}
}
