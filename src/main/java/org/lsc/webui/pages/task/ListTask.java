package org.lsc.webui.pages.task;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.lsc.Configuration;
import org.lsc.webui.beans.task.Task;

public class ListTask {

	@Property
	private List<Task> tasks;

	public static final String TASKS_PREFIX = "lsc.tasks";
	
	public ListTask() {
		tasks = new ArrayList<Task>(); 
		StringTokenizer tasksName = new StringTokenizer(Configuration.getString(TASKS_PREFIX), ",");
		while(tasksName.hasMoreTokens()) {
			String taskName = tasksName.nextToken();
			tasks.add(new Task(taskName, Configuration.getAsProperties(TASKS_PREFIX + "." + taskName)));
		}
	}
}
