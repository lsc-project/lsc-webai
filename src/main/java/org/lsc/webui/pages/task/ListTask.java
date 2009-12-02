package org.lsc.webui.pages.task;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.tapestry5.annotations.Property;
import org.lsc.Configuration;
import org.lsc.webui.beans.task.Task;

public class ListTask {

	@Property
	private List<Task> tasks;
	
	@Property
	private Task task;

	public ListTask() {
		tasks = new ArrayList<Task>(); 
		StringTokenizer tasksName = new StringTokenizer(Configuration.getString(Task.TASKS_PREFIX), ",");
		while(tasksName.hasMoreTokens()) {
			tasks.add(new Task(tasksName.nextToken()));			
		}
	}
}
