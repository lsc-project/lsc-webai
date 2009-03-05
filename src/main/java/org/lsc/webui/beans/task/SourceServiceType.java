package org.lsc.webui.beans.task;

public class SourceServiceType {
	
	protected Type type;
	
	protected String implementation;
	

	public enum Type {
		Custom,
		SimpleJndiSrcService,
	}
	
	public SourceServiceType(Type type, String implementation) {
		this.type = type;
		this.implementation = implementation;
	}

	public static SourceServiceType valueOf(String value) {
		try {
			return new SourceServiceType(Type.valueOf(value.substring(value.lastIndexOf(".") + 1)), value);
		} catch(IllegalArgumentException iae) {
//			throw new IllegalArgumentException("No recognized type: " + SourceServiceType.class.getName() + "." + value, iae);
			return new SourceServiceType(Type.Custom, value);
		}
	}
}

