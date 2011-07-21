package org.lsc.webai.base;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.collections.SetUtils;
import org.apache.tapestry5.PropertyConduit;

/**
 * Inpired by ListObjectPropertyConduit written by F. Armand in LinID Directory Manager
 * @author Sebastien Bahloul &lt;seb@lsc-project.org&gt;
 */
@SuppressWarnings("all")
public class TreeMapObjectPropertyConduit<K, V> implements PropertyConduit {

	private Class propertyType;
	
	private int index;
	
	private boolean isKey;
	
	public TreeMapObjectPropertyConduit(int index, Class propertyType, boolean isKey) {
        assert propertyType != null;
        this.index = index;
        this.propertyType = propertyType;
        this.isKey = isKey;
	}
	

	public Object get(Object instance) {
		TreeMap<K, V> map = (TreeMap<K, V>)instance;
		if(isKey) {
			return map.keySet().toArray()[index];
		} else {
			return map.get(map.keySet().toArray()[index]);
		}
	}

	public Class getPropertyType() {
		return this.propertyType;
	}

	public void set(Object instance, Object value) {
		if(null != value && !propertyType.isAssignableFrom(value.getClass())) {
			throw new RuntimeException("Incompatible type beetween List element's type ("+propertyType.toString()+") and value ("+value.getClass()+")");
		}
		TreeMap<K, V> map = (TreeMap<K, V>)instance;
		if(isKey) {
			if(index >= map.keySet().size()) {
				map.put((K) value, null);
			} else {
				map.put((K) map.keySet().toArray()[index], null);
			}
		} else {
			map.put((K) map.keySet().toArray()[index], (V)value);
		}
	}

	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return null;
	}

}
