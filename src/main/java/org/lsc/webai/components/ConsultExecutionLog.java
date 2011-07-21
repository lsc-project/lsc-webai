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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.lsc.webai.pages.HomePage;
import org.lsc.webai.utils.FileOnlyFilter;

/**
 * Start page of LSC Web User Interface.
 * 
 * @author Sebastien Bahloul &lt;seb@lsc-project.org&gt;
 */
public class ConsultExecutionLog {

	@Property @Persist
	private String _logsLocation;
	
	@Property @Persist
	private String filename;
	
	@SetupRender
	public void setupRender() {
		if(_logsLocation == null) {
			_logsLocation = HomePage.lscHomePath + File.separator + "logs";
		}
	}
	
	/**
	 * Return the log file content
	 */
	public String getFileContent() {
		if(filename != null) {
			try {
				return new String(FileUtils.readFileToByteArray(new File(filename)));
			} catch (IOException e) {
			}
		}
		return "";
	}
	
	public SelectModel getFilesModel() {
		List<OptionModel> options = new ArrayList<OptionModel>();
		if(_logsLocation != null) {
			File _logs = new File(_logsLocation);
			if(_logs.exists() && _logs.isDirectory()) {
				for(String file : _logs.list(new FileOnlyFilter(".*\\.log"))) {
					String filename = (file.indexOf(File.separator) >= 0 ? file.substring(file.lastIndexOf(File.separator)) : file);
					options.add(new OptionModelImpl(file, _logsLocation + File.separator + filename));
				}
			}
		}
		return new SelectModelImpl(null,options);
	}

	@InjectComponent
	private Zone filenameZone;
	
	@InjectComponent
	private Zone showLogZone;
	
	@Inject
	private Request _request;

	public Object onChangeOfLogsLocation() {
		_logsLocation = _request.getParameter("param");
		filename = null;
		return filenameZone;
	}

	public Object onChangeOfFileSelect() {
		filename = _request.getParameter("param");
		return showLogZone;
	}
}
