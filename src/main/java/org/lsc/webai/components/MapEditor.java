package org.lsc.webai.components;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.PropertyOverrides;
import org.apache.tapestry5.TrackableComponentEventCallback;
import org.apache.tapestry5.annotations.BeforeRenderTemplate;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.PageAttached;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.Service;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.corelib.components.BeanEditor;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.services.ClassPropertyAdapter;
import org.apache.tapestry5.ioc.services.PropertyAdapter;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.DataTypeAnalyzer;
import org.lsc.webai.base.MapObjectBeanModelSource;

public class MapEditor<K, V> extends BeanEditor {

    /**
     * Invoked to let the containing component(s) knows
     * that an "add" button had been selected.
     * The context of the event gives info upon 
     * related object id
     */
    public static final String ADD_VALUE = "add";
    public static final String DELETE_VALUE = "delete";
    
	@SuppressWarnings("unused")
    @Parameter(value = "this", allowNull = false)
    @Property(write = false)
    private PropertyOverrides overrides;
    
    @Parameter
	private String eventsContextSuffix;

	@Parameter(value = "message:addvalue-label", defaultPrefix = "literal")
	private String addValueLabel;

	@Parameter(value = "message:deletevalue-label", defaultPrefix = "literal")
	private String deleteValueLabel;

	@SuppressWarnings("unused")
	@Parameter(defaultPrefix = "literal")
	private String label;
	
	@Property
	private int index;

	private MapObjectBeanModelSource<K, V> mapBeanModelSource;

	@SuppressWarnings("unused")
	@Property
	@Persist(PersistenceConstants.FLASH)
	private BeanModel<?> keyModel;

	@SuppressWarnings("unused")
	@Property
	@Persist(PersistenceConstants.FLASH)
	private BeanModel<?> valueModel;

	@Parameter(required=true, allowNull=false) @Property
	private Map<K, V> object;

	private K key;

	@SuppressWarnings("unused")
	@Property
	private V value;

	/* ***********************************************************
	 * injected services&properties
	 * ***********************************************************
	 */

	@Inject
	private ComponentResources resources;

	@Inject
	private BeanModelSource beanModelSource;

	@Inject @Service("DefaultDataTypeAnalyzer")
	private DataTypeAnalyzer dataTypeAnalyzer;

    @Environmental
    private TrackableComponentEventCallback<?> eventCallback;

//	/** Private static default logger */
//	private final Logger LOGGER = LoggerFactory.getLogger(MapEditor.class);

	@PageAttached
    void initBeanModelSource() {
    }
	
	@BeforeRenderTemplate
	void beforeRenderTemplate() {
        String id = this.resources.getId();
        this.mapBeanModelSource =  new MapObjectBeanModelSource<K, V>(beanModelSource, id);
        index = 0;

        if(object.size() > 0) {
    		String keyDatatype = dataTypeAnalyzer.identifyDataType(new FakePropertyAdapter(object.keySet().iterator().next().getClass()));
    		String valueDatatype = dataTypeAnalyzer.identifyDataType(new FakePropertyAdapter(object.get(object.keySet().iterator().next()).getClass()));
    		keyModel = mapBeanModelSource.generateKeyBeanModel(object, resources, keyDatatype, addValueLabel, true);
    		valueModel = mapBeanModelSource.generateValueBeanModel(object, resources, valueDatatype, addValueLabel, true);
		}
	}
    
    /**
	 * The add context is composed : - at index 0, by the property name prefix ;
	 * - at index 1, if exists, by the eventContextSuffix
	 */
	public String getAddContext() {
		StringBuilder context = new StringBuilder();
		context.append(this.mapBeanModelSource.getPropertyIdPrefix());
		if (null != this.eventsContextSuffix) {
			context.append(MapObjectBeanModelSource.CONTEXT_SEPARATOR).append(this.eventsContextSuffix);
		}
		return context.toString();
	}

	/**
	 * The delete context is composed : 
	 * &lt;ul&gt;
	 * &lt;li&gt;- at index 0, by the property name prefix (the list name)&lt;/li&gt; 
	 * &lt;li&gt;- at index 1, by the index of the property in the list&lt;/li&gt;
	 * &lt;li&gt;- at index 2, if exists, by the eventContextSuffix&lt;/li&gt;
	 * &lt;/ul&gt;
	 */
	public String getDeleteContext() {
		// Create the named for the component
		// eg : context_key/0
		StringBuilder context = new StringBuilder();
		context.append(this.getPropertyIdPrefix()); 
		context.append(MapObjectBeanModelSource.CONTEXT_SEPARATOR);
		context.append(this.index);
		if (null != this.eventsContextSuffix) {
			context.append(MapObjectBeanModelSource.CONTEXT_SEPARATOR).append(this.eventsContextSuffix);
		}
		return context.toString();
	}

	public K getKey() {
		return key;
	}
	
	public void setKey(K key) {
		this.key = key;
		this.value = object.get(key);
	}

	public String getAddValueLabel() {
		return this.addValueLabel;
	}

	public String getDeleteValueLabel() {
		return this.deleteValueLabel;
	}

	public String getEventsContextSuffix() {
		return this.eventsContextSuffix;
	}

	public String getPropertyIdPrefix() {
		return this.mapBeanModelSource.getPropertyIdPrefix();
	}
	
    /* ***********************************************************
     *                 Event handlers&processing
     ************************************************************ */

    /**
     * Analyze a delete action request. If the request is valid, 
     * fire a new event so that top level element can handle
     * the processing of the request.
     */
    @OnEvent(component="deleteValueButton", value = EventConstants.SELECTED)
    void onSelectedFromDeleteValueButton(String context) {
            if(null != context) {
                    String[] values = context.split(MapObjectBeanModelSource.CONTEXT_SEPARATOR);
                    if(3 == values.length) {
                            resources.triggerEvent(DELETE_VALUE, values, eventCallback);
                    }
            }
    }

    @OnEvent(component="addValueButton", value = EventConstants.SELECTED)
    void onSelectedFromAddValueButton(String context) {
            resources.triggerEvent(ADD_VALUE, new String[] { context }, eventCallback);
    }
    
    public Set<K> getKeys() {
    	return object.keySet();
    }
    
    public String getPropertyName() {
    	return this.getPropertyIdPrefix() + MapObjectBeanModelSource.CONTEXT_SEPARATOR + index; 
    }
    
	
	public String getDescription() {
		return this.object.toString();
	}

}

class FakePropertyAdapter implements PropertyAdapter {
	
	Class originalType;
	
	public FakePropertyAdapter(Class originalType) {
		this.originalType = originalType;
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isRead() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Method getReadMethod() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isUpdate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Method getWriteMethod() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object get(Object instance) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void set(Object instance, Object value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Class getType() {
		return originalType;
	}

	@Override
	public boolean isCastRequired() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ClassPropertyAdapter getClassAdapter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class getBeanType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isField() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Field getField() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class getDeclaringClass() {
		// TODO Auto-generated method stub
		return null;
	}
}
