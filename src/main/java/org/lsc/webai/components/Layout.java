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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.PageLoaded;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Value;
import org.lsc.webai.pages.HomePage;

/**
 * Layout page including version information from Maven build system 
 * @author Sebastien Bahloul &lt;seb@lsc-project.org&gt;
 */
public class Layout {

	@SuppressWarnings("unused")
	@Property
	private String title;

	@SuppressWarnings("unused")
	@Property
	private String favicon;
	
	@SuppressWarnings("unused")
	@Property(write=false)	
	@Inject @Value("${"+SymbolConstants.TAPESTRY_VERSION+"}")
	private String tapestryVersion;
	
	@Inject
	private ComponentResources resources;
	
	@InjectPage
	private HomePage homePage;

	private Properties versionProperties;
	
	@PageLoaded
	public void pageLoaded() throws IOException {
		versionProperties = new Properties();
		InputStream file = this.getClass().getClassLoader().getResourceAsStream("version.properties");
		if(file != null) {
			versionProperties.load(file);
		}
	}

	public String getLSCVersion() {
		return versionProperties.getProperty("Lsc-Version");
	}

	public String getLscAIVersion() {
		return versionProperties.getProperty("Implementation-Version");
	}
	
	public boolean isLoadedFromHomePage() {
		return resources.getPage().equals(homePage);
	}
}
