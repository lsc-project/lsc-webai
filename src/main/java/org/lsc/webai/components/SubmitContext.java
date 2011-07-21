package org.lsc.webai.components;

import java.util.List;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.ClientElement;
import org.apache.tapestry5.ComponentAction;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.TrackableComponentEventCallback;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.services.Heartbeat;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

/**
 * Corresponds to &lt;input type="submit"&gt;, a client-side element that can
 * force the enclosing form to submit. The submit responsible for the form
 * submission will post a notification that allows the application to know that
 * it was the responsible entity. The notification is named "selected" and has a
 * String context.
 */
public final class SubmitContext implements ClientElement {

	/**
	 * If true, then any notification sent by the component will be deferred
	 * until the end of the form submission (this is usually desirable).
	 */
	@Parameter
	private boolean _defer = true;

	@Parameter
	private List<String> _context;

    /**
     * If provided, the component renders an input tag with type "image". Otherwise "submit".
     */
    @Parameter(defaultPrefix = BindingConstants.ASSET)
    private Asset image;
    
	@Environmental
	private Heartbeat heartbeat;
	
    @Environmental
    private TrackableComponentEventCallback<?> eventCallback;

    @Environmental(false)
    private FormSupport formSupport;
    
    @Inject
    private ComponentResources resources;

    @Inject
    private JavaScriptSupport javascriptSupport;
    
    /**
     * The id used to generate a page-unique client-side identifier for the component. If a component renders multiple
     * times, a suffix will be appended to the to id to ensure uniqueness. The uniqued value may be accessed via the
     * {@link #getClientId() clientId property}.
     */
//    @Parameter(value = "prop:componentResources.id", defaultPrefix = BindingConstants.LITERAL)
    private String clientId;


    /**
     * If true, then the field will render out with a disabled attribute (to turn off client-side behavior). Further, a
     * disabled field ignores any value in the request when the form is submitted.
     */
    @Parameter("false")
    private boolean disabled;
    
    @Inject
    private Request request;
    
    @Parameter
    private String value;

    public SubmitContext() {
	}

	SubmitContext(Request request) {
		this.request = request;
	}
	
    @SetupRender
    final void setup() {
    	clientId = javascriptSupport.allocateClientId(resources);
    }
    
	void beginRender(MarkupWriter writer) {

        // convert context to a string : contexta/contextb/contextc ...
		StringBuilder contextStringBuilder = new StringBuilder("");
		if (null != _context) {
			int j;
			for (int i = 0; (j = _context.size() - i) > 0; i++) {
				contextStringBuilder.append(_context.get(i));
				if (j > 1) {
					contextStringBuilder.append("/");
				}
			}
		}

		// write a hidden input for the context
        String elementName = formSupport.allocateControlName(resources.getId());
		writer.element("input", "type", "hidden", "name", elementName + "X",
				"value", contextStringBuilder.toString(), "class", "t-invisible");
		writer.end();

		// now the submit
        String type = (image != null ? "image" : "submit");

        writer.element("input", "type", type, "name", elementName,
				"id", clientId, "value", value);

		if (disabled)
            writer.attributes("disabled", "disabled");

        if (image != null)
            writer.attributes("src", image.toClientURL());


		formSupport.store(this, new ProcessSubmission(getClientId(), elementName));
		resources.renderInformalParameters(writer);
	}

	private static class ProcessSubmission<T> implements ComponentAction<T> {
			private static final long serialVersionUID = 2757425046754531400L;
			private final String clientId, elementName;

	        public ProcessSubmission(String clientId, String elementName) {
	            this.clientId = clientId;
	            this.elementName = elementName;
	        }

	        public void execute(T component) {
	        	((SubmitContext)component).processSubmission(clientId, elementName);
	        }
    }

	void processSubmission(String clientId, final String elementName) {
        if (disabled || !selected(clientId, elementName))
            return;

		Runnable sendNotification = new Runnable() {
			
			public void run() {
				resources.triggerEvent(EventConstants.SELECTED, new Object[] { 
						request.getParameter(elementName + "X")
					}, eventCallback);
			}
		};

		// When not deferred, don't wait, fire the event now (actually, at the
		// end of the current
		// heartbeat). This is most likely because the Submit is inside a Loop
		// and some contextual
		// information will change if we defer. Another option might be to wait
		// until the next
		// heartbeat?

		if (_defer)
			formSupport.defer(sendNotification);
		else
			heartbeat.defer(sendNotification);

	}

	// For testing:

	void setDefer(boolean defer) {
		_defer = defer;
	}

	void setup(ComponentResources resources, FormSupport support, Heartbeat heartbeat) {
		this.resources = resources;
		this.formSupport = support;
		this.heartbeat = heartbeat;
	}

    private boolean selected(String clientId, String elementName) {
        // Case #1: via JavaScript, the client id is passed up.

        if (clientId.equals(request.getParameter("t:submit")))
            return true;

        // Case #2: No JavaScript, look for normal semantic (non-null value for the element's name).
        // If configured as an image submit, look for a value for the x position. Ah, the ugliness
        // of HTML.

        String name = image == null ? elementName : elementName + ".x";

        String value = request.getParameter(name);

        return value != null;
    }

    void afterRender(MarkupWriter writer) {
        writer.end();
    }

	@Override
    /**
     * Returns the component's client id. This must be called after the component has rendered.
     * 
     * @return client id for the component
     */
    public String getClientId() {
        return clientId;
    }
}
