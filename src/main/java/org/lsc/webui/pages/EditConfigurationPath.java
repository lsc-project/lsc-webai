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

import java.io.File;

import org.apache.tapestry5.FieldValidator;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.ValidationException;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Mixins;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.TextField;
import org.lsc.Configuration;

/**
 * Edit global parameters page.
 * 
 * @author Sebastien Bahloul &lt;seb@lsc-project.org&gt;
 */
public class EditConfigurationPath {
	
	@Property
	private String filePath;
	
	@InjectPage
	private ErrorPage errorPage;

	public EditConfigurationPath() {
	}
	
	public Object onActionFromForm() {
		if(filePath != null) {
			Configuration.setLocation(filePath);
			return Index.class;
		} else {
			return this;
		}
	}
	
	public void setupRender() {
		filePath = Configuration.getConfigurationDirectory();
	}

	@Component(parameters = {"value=filePath","validate=prop:fileValidator"} )
	@Mixins({"Autocomplete"})
	private TextField fileCompletion;
	
	@Component(id="form")
	private Form form;

	private boolean locallyDeployed = true;

	private static final int MAX_SIZE = 20;

	@SuppressWarnings("unchecked")
	public FieldValidator getFileValidator() {
		return new LSCPathValidator();
	}
	/**
	 * Prepend prefix on each table value
	 * @param prefix the prefix
	 * @param params the values table
	 * @return the prefixed values table
	 */
	public String[] prepend(String prefix, String[] params) {
		if(params != null) {
			int max = (params.length < MAX_SIZE ? params.length : MAX_SIZE);
			String[] returns = new String[max];
			for(int i = 0; i < max; i++) {
				returns[i] = prefix + params[i];
			}
			return returns;
		} else {
			return new String[]{};
		}
	}
	
	/**
	 * Provide the files / directories listing on the application server local filesystem
	 * @param partial the written path
	 * @return the list of values available to select on client side
	 */
	public String[] onProvideCompletions(String partial) {
		if( !locallyDeployed ) {
			return new String[] { "Unavailable on distant installation !" };
		} else if( partial.lastIndexOf( File.separator ) == -1 ) {
			return prepend( partial, new File(partial).list() );
		} else if( partial.lastIndexOf( File.separator ) == 0 ) {
			return prepend( "/", new File("/").list() );
		} else if ( new File(partial).isDirectory() ) { 
			return prepend( partial + File.separator, new File(partial).list() );
		} else {
			String prefix = partial.substring( 0, partial.lastIndexOf( File.separator ) );
			return prepend( prefix + File.separator, new File(prefix).list() );
		}
	}
	
	class LSCPathValidator implements FieldValidator<String> {

		@Override
		public boolean isRequired() {
			return true;
		}

		@Override
		public void render(MarkupWriter writer) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void validate(String value) throws ValidationException {
			if(!new File(value.toString(), Configuration.PROPERTIES_FILENAME).exists()) {
				throw new ValidationException("Missing LSC configuration file in " + value.toString() + "!");
			}
		}
	}
}
