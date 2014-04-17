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

import org.apache.tapestry5.FieldValidator;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.TextField;
import org.lsc.exception.LscConfigurationException;
import org.lsc.webai.pages.HomePage;
import org.lsc.webai.utils.LSCPathValidator;

/**
 * Edit global parameters page.
 * 
 * @author Sebastien Bahloul &lt;seb@lsc-project.org&gt;
 */
public class EditConfigurationPath extends AbstractPathEdition {
	
	@InjectPage
	private HomePage index;
	
	@InjectComponent("form")
    private Form form;

	public EditConfigurationPath() {
	}
	
	public Object onActionFromForm() {
		if(getFilePath() != null) {
			try {
                return index.initialize(getFilePath());
            } catch (LscConfigurationException e) {
                Throwable origin = e.getCause();
                while(origin != null && origin.getLocalizedMessage() == null) {
                    origin = origin.getCause();
                }
                if(origin.getLocalizedMessage() != null) {
                    form.recordError(origin.getLocalizedMessage());
                } else {
                    form.recordError(e.getLocalizedMessage());
                }
                return this;
            }
		} else {
			return this;
		}
	}
	
	public void setupRender() {
		setFilePath(HomePage.lscConfigurationPath);
	}

	@Component(parameters = {"value=filePath","validate=prop:fileValidator"} )
	private TextField fileCompletion;
	
	public FieldValidator getFileValidator() {
		return new LSCPathValidator();
	}
}
