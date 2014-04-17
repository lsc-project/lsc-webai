package org.lsc.webai.pages;

import java.io.File;

import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.ProgressiveDisplay;
import org.lsc.exception.LscException;
import org.lsc.webai.components.AbstractPathEdition;

public class Index extends AbstractPathEdition {

	@InjectPage
	private HomePage homePage;
	
	@InjectComponent
	private Form confirmLscHome;
	
	@InjectComponent
	private ProgressiveDisplay progressiveDisplay;
	
	@Property
	@Persist
	private String lscHome;
	
	Object onActivate() {
        lscHome = System.getProperty("LSC_HOME");
        if(lscHome != null && new File(lscHome, "etc/lsc.xml").isFile())  {
            HomePage.lscConfigurationPath = new File(lscHome, "etc").getAbsolutePath();
            return homePage;
        } else {
            return null;
        }
	}
	
	@OnEvent( component = EventConstants.PROGRESSIVE_DISPLAY )
	Object onEventFromProgressiveDisplay() throws LscException {
		homePage.setupRender();
		return homePage;
//		if(lscHome != null) {
//		} else {
//			return null;
//		}
	}
	
	Object onSubmitFromConfirmLscHome() {
		HomePage.lscConfigurationPath = new File(lscHome, "etc").getAbsolutePath();
		return homePage;
	}
}
