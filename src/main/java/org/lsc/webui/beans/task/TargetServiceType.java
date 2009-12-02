package org.lsc.webui.beans.task;

public class TargetServiceType {

	protected Type type;
	
	protected String implementation;
	
	public enum Type {
		SimpleJndiDstService,
		FullDNJndiDstService,
		Other
	}

	public TargetServiceType(Type type, String implementation) {
		this.type = type;
		this.implementation = implementation;
	}

	public static TargetServiceType valueOf(String value) {
		try {
			return new TargetServiceType(Type.valueOf(value.substring(value.lastIndexOf(".") + 1)), value);
		} catch(IllegalArgumentException iae) {
			return new TargetServiceType(Type.Other, value);
		//	throw new IllegalArgumentException("No recognized type: " + SourceServiceType.class.getName() + "." + value, iae);
		}
	}
}
