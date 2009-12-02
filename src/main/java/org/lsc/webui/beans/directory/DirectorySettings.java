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
package org.lsc.webui.beans.directory;

import org.apache.tapestry5.beaneditor.Validate;

/**
 * Directory settings parameters.
 * 
 * @author Sebastien Bahloul &lt;seb@lsc-project.org&gt;
 */
public class DirectorySettings {

	/** Method to use to bind to the directory */
	@Validate("required")
	private AuthenticationType authenticationType;

	/**
	 * A simple directory URL with protocol, host and port, ie
	 * ldap://ldap.openldap.org:389/
	 */
	@Validate("required,regexp=^ldap(s)?://[_a-zA-A0-9][_a-zA-Z0-9\\-\\.]+(:\\d+)?(/\\S*)?$")
	private String directoryURL;

	/** Identity information to bind with (null if anonymous) */
	private String principal;

	/** Authentication information to bind with (null if anonymous) */
	private String credentials;

	/** */
	@Validate("required")
	private ReferralHandling referralHandling;

	/** */
	@Validate("required")
	private AliasesHandling aliasesHandling;
	
	/** LDAP Version */
	@Validate("required")
	private LdapVersion version;

	public AuthenticationType getAuthenticationType() {
		return authenticationType;
	}

	public String getDirectoryURL() {
		return directoryURL;
	}

	public String getPrincipal() {
		return principal;
	}

	public String getCredentials() {
		return credentials;
	}

	public LdapVersion getVersion() {
		return version;
	}

	public AliasesHandling getAliasesHandling() {
		return aliasesHandling;
	}

	public ReferralHandling getReferralHandling() {
		return referralHandling;
	}

	public void setPrincipal(String principal) {
		this.principal = principal;
	}

	public void setCredentials(String credentials) {
		this.credentials = credentials;
	}

	public void setAuthenticationType(AuthenticationType authenticationType) {
		this.authenticationType = authenticationType;
	}

	public void setAuthenticationType(String authenticationType) {
		if(authenticationType != null) {
			this.authenticationType = AuthenticationType.valueOf(authenticationType.toUpperCase());
		} else {
			this.authenticationType = AuthenticationType.ANONYMOUS;
		}
	}

	public void setDirectoryURL(String directoryURL) {
		this.directoryURL = directoryURL;
	}

	public void setReferralHandling(ReferralHandling referralHandling) {
		this.referralHandling = referralHandling;
	}
	
	public void setReferralHandling(String referralHandling) {
		if(referralHandling != null) {
			this.referralHandling = ReferralHandling.valueOf(referralHandling.toUpperCase());
		} else {
			this.referralHandling = ReferralHandling.IGNORE;
		}
	}

	public void setAliasesHandling(AliasesHandling aliasesHandling) {
		this.aliasesHandling = aliasesHandling;
	}

	public void setAliasesHandling(String aliasesHandling) {
		if(aliasesHandling != null) {
			this.aliasesHandling = AliasesHandling.valueOf(aliasesHandling.toUpperCase());
		} else {
			this.aliasesHandling = AliasesHandling.NEVER;
		}
	}

	public void setVersion(LdapVersion version) {
		this.version = version;
	}
}
