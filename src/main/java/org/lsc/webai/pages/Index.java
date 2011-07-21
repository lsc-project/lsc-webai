package org.lsc.webai.pages;

import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.ProgressiveDisplay;

public class Index {

	@InjectPage
	private HomePage homePage;
	
	@InjectComponent
	private ProgressiveDisplay progressiveDisplay;
	
	@Property
	@Persist
	private String lscHome;
	
	void setupRender() {
		lscHome = System.getProperty("LSC_HOME");
	}

	@OnEvent(EventConstants.PROGRESSIVE_DISPLAY)
	Object onEventFromProgressiveDisplay() {
		if(lscHome != null) {
			homePage.setupRender();
			return homePage;
		} else {
			return null;
		}
	}
	
	Object onSubmitFromConfirmLscHome() {
		homePage.setFilePath(lscHome);
		return progressiveDisplay;
	}
}
