package org.openmrs.module.clinicalsummary.extension.html;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.Extension;

public class SummaryDashboardHeaderExt extends Extension {
	
	private Log log = LogFactory.getLog(SummaryDashboardHeaderExt.class);
	
	private String patientId = "";

	public Extension.MEDIA_TYPE getMediaType() {
		return Extension.MEDIA_TYPE.html;
	}
	
	@Override
	public void initialize(Map<String, String> parameters) {
		patientId = parameters.get("patientId");
		log.debug("patientId: " + patientId);
		log.debug("parameters: " + parameters.keySet());
	}
	
	@Override
	public String getOverrideContent(String bodyContent) {
		return " &nbsp;<a href='moduleServlet/clinicalsummary/generate?patientId=" + patientId + "'>View Patient Summary</a>";
	}
	
}
