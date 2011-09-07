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
package org.lsc.webai.services;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.Validator;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Local;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.RequestFilter;
import org.apache.tapestry5.services.RequestHandler;
import org.apache.tapestry5.services.Response;
import org.lsc.utils.ClasstypeFinder;
import org.lsc.webai.utils.CronExpressionTriggerValidator;
import org.quartz.SchedulerException;
import org.slf4j.Logger;

/**
 * This module is automatically included as part of the Tapestry IoC Registry,
 * it's a good place to configure and extend Tapestry, or to place your own
 * service definitions.
 * 
 * @author Sebastien Bahloul &lt;seb@lsc-project.org&gt;
 */
public class AppModule {
	public static void bind(ServiceBinder binder) {
		// binder.bind(MyServiceInterface.class, MyServiceImpl.class);

		// Make bind() calls on the binder object to define most IoC services.
		// Use service builder methods (example below) when the implementation
		// is provided inline, or requires more initialization than simply
		// invoking the constructor.
	}

	public static void contributeApplicationDefaults(
			MappedConfiguration<String, String> configuration) {
		// Contributions to ApplicationDefaults will override any contributions
		// to
		// FactoryDefaults (with the same key). Here we're restricting the
		// supported
		// locales to just "en" (English). As you add localised message catalogs
		// and other assets,
		// you can extend this list of locales (it's a comma separated series of
		// locale names;
		// the first locale name is the default when there's no reasonable
		// match).

		configuration.add(SymbolConstants.SUPPORTED_LOCALES, "en");

		// The factory default is true but during the early stages of an
		// application
		// overriding to false is a good idea. In addition, this is often
		// overridden
		// on the command line as -Dtapestry.production-mode=false
		configuration.add(SymbolConstants.PRODUCTION_MODE, "false");
	}

	/**
	 * This is a service definition, the service will be named "TimingFilter".
	 * The interface, RequestFilter, is used within the RequestHandler service
	 * pipeline, which is built from the RequestHandler service configuration.
	 * Tapestry IoC is responsible for passing in an appropriate Logger
	 * instance. Requests for static resources are handled at a higher level, so
	 * this filter will only be invoked for Tapestry related requests.
	 * <p>
	 * Service builder methods are useful when the implementation is inline as
	 * an inner class (as here) or require some other kind of special
	 * initialization. In most cases, use the static bind() method instead.
	 * <p>
	 * If this method was named "build", then the service id would be taken from
	 * the service interface and would be "RequestFilter". Since Tapestry
	 * already defines a service named "RequestFilter" we use an explicit
	 * service id that we can reference inside the contribution method.
	 */
	public RequestFilter buildTimingFilter(final Logger log) {
		return new RequestFilter() {
			public boolean service(Request request, Response response,
					RequestHandler handler) throws IOException {
				long startTime = System.currentTimeMillis();

				try {
					// The responsibility of a filter is to invoke the
					// corresponding method
					// in the handler. When you chain multiple filters together,
					// each filter
					// received a handler that is a bridge to the next filter.

					return handler.service(request, response);
				} finally {
					long elapsed = System.currentTimeMillis() - startTime;
					log.info(String.format("%s (XHR: %s, time: %dms) : %s", request.getMethod(), request.isXHR(), elapsed, request.getPath()));
				}
			}
		};
	}

	/**
	 * This is a contribution to the RequestHandler service configuration. This
	 * is how we extend Tapestry using the timing filter. A common use for this
	 * kind of filter is transaction management or security. The @Local
	 * annotation selects the desired service by type, but only from the same
	 * module. Without @Local, there would be an error due to the other
	 * service(s) that implement RequestFilter (defined in other modules).
	 */
	public void contributeRequestHandler(
			OrderedConfiguration<RequestFilter> configuration,
			@Local RequestFilter filter) {
		// Each contribution to an ordered configuration has a name, When
		// necessary, you may
		// set constraints to precisely control the invocation order of the
		// contributed filter
		// within the pipeline.

		configuration.add("Timing", filter);
	}

	// /**
	// * This is a contribution to the type of editor that can be used in bean
	// * editor or similar components. Here, we declare that the page
	// * SimplePropertyEditBlocks contribute editor blocks.
	// *
	// * @see SimplePropertyEditBlocks
	// */
	// public static void contributeDefaultDataTypeAnalyzer(
	// MappedConfiguration<Class<?>, String> configuration) {
	// configuration.add(HashMap.class, "stringsMap");
	// }

	// public static void contributeEditorAnalyzer(MappedConfiguration<String,
	// String> configuration) {
	// configuration.add("stringsMap", "stringsMap");
	// }

	// public static void contributeViewerAnalyzer(
	// MappedConfiguration<String, String> configuration) {
	// configuration.add("stringsMap", "stringsMap");
	// }

	// public static void contributeBeanBlockSource(
	// Configuration<BeanBlockContribution> configuration) {
	// configuration.add(new BeanBlockContribution("stringsMap",
	// "edit/StringsMapBlock", "stringsMap", true));
	// configuration.add(new BeanBlockContribution("stringsMap",
	// "display/StringsMapBlock", "stringsMap", false));
	// }
	
	public ClasstypeFinder buildClasspathFinder() {
		ClasstypeFinder cf = ClasstypeFinder.getInstance();
		URL baseDirectory = this.getClass().getClassLoader().getResource("org/lsc");
		if(baseDirectory == null) {
			throw new ExceptionInInitializerError("Unable to locate base classpath ! Aborting ...");
		}
		File libSubDirectory = new File(baseDirectory.getFile(), "../../../lib");
		cf.setupClasspath(libSubDirectory);
		return cf;
	}
	
	public LscScheduler buildScheduler(ClasstypeFinder cf) {
		try {
			return new LscScheduler(cf);
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public void contributeFieldValidatorSource(MappedConfiguration<String, Validator<?,?>> configuration) {
		configuration.add("cronexpr", new CronExpressionTriggerValidator());
	}
}
