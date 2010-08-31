/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.clinicalsummary.extension.html;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.Extension;

public class SummaryDashboardHeaderExt extends Extension {
	
	private static final Log log = LogFactory.getLog(SummaryDashboardHeaderExt.class);
	
	private String patientId = "";
	
	@Override
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
		return " &nbsp;<a href='module/clinicalsummary/generate.form?patientId=" + patientId + "'>View Patient Summary</a>";
	}
	
}
