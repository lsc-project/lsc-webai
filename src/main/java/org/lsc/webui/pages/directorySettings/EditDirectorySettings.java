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
package org.lsc.webui.pages.directorySettings;

import java.util.Properties;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.lsc.Configuration;
import org.lsc.webui.base.EditSettings;
import org.lsc.webui.beans.directory.AliasesHandling;
import org.lsc.webui.beans.directory.AuthenticationType;
import org.lsc.webui.beans.directory.DirectorySettings;
import org.lsc.webui.beans.directory.LdapVersion;
import org.lsc.webui.beans.directory.ReferralHandling;
import org.lsc.webui.pages.ErrorPage;

/**
 * Edit directory settings page.
 * 
 * @author Sebastien Bahloul &lt;seb@lsc-project.org&gt;
 */
public class EditDirectorySettings extends EditSettings {

	public static final String SUN_LDAP_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";

	public static final String LDAP_CLASS_HANDLER = "java.naming.factory.initial";
	public static final String LDAP_URL = "java.naming.provider.url";
	public static final String LDAP_PRINCIPAL = "java.naming.security.principal";
	public static final String LDAP_CREDENTIALS = "java.naming.security.credentials";
	public static final String LDAP_AUTHENTICATION_TYPE = "java.naming.security.authentication";
	public static final String LDAP_REFERRAL_HANDLING = "java.naming.referral";
	public static final String LDAP_VERSION = "java.naming.ldap.version";
	public static final String LDAP_ALIASES_HANDLING = "java.naming.ldap.derefAliases";

	@Property
	@Persist
	private DirectorySettings directorySettings;

	@Property
	@Persist
	private String propertiesPrefix;
	
	@InjectPage
	private ErrorPage errorPage;
	
	@Property
	@Persist
	private String message;
	
//	@Component(id = "form")
//	private Form _form;
	
	/**
	 * Initialize
	 * 
	 * @param properties
	 * @return
	 */
	public Object initialize(String prefix, Properties localProperties) {
		message = "";
		propertiesPrefix = prefix;
		directorySettings = new DirectorySettings();
		if (localProperties != null) {
			directorySettings.setAliasesHandling(AliasesHandling
					.valueOf((String) localProperties
							.getProperty(LDAP_ALIASES_HANDLING).toUpperCase()));
			directorySettings.setAuthenticationType(AuthenticationType
					.valueOf((String) localProperties
							.getProperty(LDAP_AUTHENTICATION_TYPE).toUpperCase()));
			directorySettings.setCredentials((String) localProperties
					.getProperty(LDAP_CREDENTIALS));
			directorySettings.setDirectoryURL((String) localProperties
					.getProperty(LDAP_URL));
			directorySettings.setPrincipal((String) localProperties
					.getProperty(LDAP_PRINCIPAL));
			directorySettings.setReferralHandling(ReferralHandling
					.valueOf((String) localProperties
							.getProperty(LDAP_REFERRAL_HANDLING).toUpperCase()));
			if(localProperties.getProperty(LDAP_VERSION).compareToIgnoreCase("2") == 0) {
				directorySettings.setVersion(LdapVersion.VERSION_2);
			} else if (localProperties.getProperty(LDAP_VERSION).compareToIgnoreCase("3") == 0) {
				directorySettings.setVersion(LdapVersion.VERSION_3);
			}
			directorySettings.setReferralHandling(ReferralHandling
					.valueOf((String) localProperties
							.getProperty(LDAP_REFERRAL_HANDLING).toUpperCase()));
		}
		return this;
	}
	
	public Object onSuccess() {
		Properties properties = new Properties();
		properties.setProperty(LDAP_CLASS_HANDLER, SUN_LDAP_FACTORY );
		properties.setProperty(LDAP_ALIASES_HANDLING, directorySettings.getAliasesHandling().name());
		properties.setProperty(LDAP_AUTHENTICATION_TYPE, directorySettings.getAuthenticationType().name());
		properties.setProperty(LDAP_CREDENTIALS, nvl(directorySettings.getCredentials(), ""));
		properties.setProperty(LDAP_PRINCIPAL, nvl(directorySettings.getPrincipal(), ""));
		properties.setProperty(LDAP_REFERRAL_HANDLING, directorySettings.getReferralHandling().name());
		properties.setProperty(LDAP_URL, directorySettings.getDirectoryURL());
		if(directorySettings.getVersion() == LdapVersion.VERSION_2) {
			properties.setProperty(LDAP_VERSION, "2");
		} else {
			properties.setProperty(LDAP_VERSION, "3");
		}
		try {
			Configuration.setProperties(propertiesPrefix, properties);
			message = SUCCESSFULLY ;
		} catch (ConfigurationException e) {
			return errorPage.initialize(e);
		}
		return this;
	}
	
//	void onValidateForm() {
//		try {
//			new LdapName(directorySettings.getDirectoryURL());
//		} catch (InvalidNameException e) {
//			_form.recordError("Please provide a valid LDAP url !");
//		}
//	}
}
