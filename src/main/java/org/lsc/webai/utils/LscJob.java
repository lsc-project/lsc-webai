/*
 ****************************************************************************
 * Ldap Synchronization Connector provides tools to synchronize
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
package org.lsc.webai.utils;

import java.util.Arrays;
import java.util.List;

import org.lsc.Task;
import org.lsc.Task.Mode;
import org.lsc.webai.services.LscRemoteCommands;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Quartz job used to handle LSC task (either sync or async)
 * @author Sebastien Bahloul &lt;seb@lsc-project.org&gt;
 */
public class LscJob implements Job {

	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		
		JobDataMap jdm = context.getMergedJobDataMap();
		Task.Mode mode = (Mode) jdm.get(Task.Mode.class.toString());
		String taskName = jdm.getString("taskName");
		
		if(LscRemoteCommands.bind()) {
			switch(mode) {
				case async:
					List<String> asyncTasks = Arrays.asList(LscRemoteCommands.getAsyncTasks());
					if(asyncTasks.contains(taskName)) {
						if(LscRemoteCommands.isAsyncTaskStarted(taskName)) {
							
						} else {
							LscRemoteCommands.startAsyncTask(taskName);
							// Wait for 5 seconds that the task starts
							sleep(5);
							while(LscRemoteCommands.isAsyncTaskStarted(taskName)) {
								// Wait for the end of the synchronization
								sleep(1);
							}
						}
					} else {
						// Unable to find the task
					}
					break;
				case sync:
					List<String> syncTasks = Arrays.asList(LscRemoteCommands.getSyncTasks());
					if(syncTasks.contains(taskName)) {
					
					} else {
						// Unable to find the task
					}
					break;
				case clean:
					try {
						LscRemoteCommands.launchCleanTask(taskName);
					} catch (Exception e) {
						throw new JobExecutionException(e);
					}
			}
		}
	}
	
	private void sleep(int seconds) {
		try {
			Thread.sleep(1000*seconds);
		} catch (InterruptedException e) {
		}
	}
}
