package org.lsc.webui.beans.service;

import java.util.List;

import org.apache.tapestry5.annotations.Property;
import org.lsc.jndi.SimpleJndiSrcService;

public class DataServiceImpl implements DataService {

	@Override
	public String getDescription() {
		return "Simple JNDI";
	}

	@Override
	public String getName() {
		return SimpleJndiSrcService.class.getSimpleName();
	}

	@Override
	public String getImplementation() {
		return SimpleJndiSrcService.class.getName();
	}

	@Property
	private List<String> pivotAttributes;
	
	@Property
	private List<String> attributes;
	
	@Property
	private String filterId;
	
	@Property
	private String filterAll;
	
	@Property
	private String baseDn;
	
	public List<String> getPivotAttributes() {
		return pivotAttributes;
	}

	public void setPivotAttributes(List<String> pivotAttributes) {
		this.pivotAttributes = pivotAttributes;
	}

	public List<String> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<String> attributes) {
		this.attributes = attributes;
	}

	public String getFilterId() {
		return filterId;
	}

	public void setFilterId(String filterId) {
		this.filterId = filterId;
	}

	public String getFilterAll() {
		return filterAll;
	}

	public void setFilterAll(String filterAll) {
		this.filterAll = filterAll;
	}

	public String getBaseDn() {
		return baseDn;
	}

	public void setBaseDn(String baseDn) {
		this.baseDn = baseDn;
	}
}
