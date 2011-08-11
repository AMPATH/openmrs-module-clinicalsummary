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

package org.openmrs.module.clinicalsummary.web.controller.service;

import com.thoughtworks.xstream.XStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.module.clinicalsummary.Index;
import org.openmrs.module.clinicalsummary.Summary;
import org.openmrs.module.clinicalsummary.service.IndexService;
import org.openmrs.module.clinicalsummary.web.controller.service.xstream.IndexConverter;
import org.openmrs.module.clinicalsummary.web.controller.service.xstream.PatientConverter;
import org.openmrs.module.clinicalsummary.web.controller.service.xstream.PatientIdentifierConverter;
import org.openmrs.module.clinicalsummary.web.controller.service.xstream.PersonNameConverter;
import org.openmrs.module.clinicalsummary.web.controller.service.xstream.SummaryConverter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 */
@Controller
@RequestMapping("/module/clinicalsummary/service/patient/index")
public class PatientIndexController {

	private static final Log log = LogFactory.getLog(PatientIndexController.class);

	/**
	 * @param patientId
	 * @param response
	 * @should return indexes for the patient
	 * @should return empty list when no index found for the patient
	 */
	@RequestMapping(method = RequestMethod.GET)
	public void searchIndex(@RequestParam(required = false, value = "username") String username,
	                        @RequestParam(required = false, value = "password") String password,
	                        @RequestParam(required = true, value = "patientId") Integer patientId,
	                        HttpServletResponse response) throws IOException {
		try {
			if (!Context.isAuthenticated())
				Context.authenticate(username, password);

			List<Index> indexes = Context.getService(IndexService.class).getIndexes(Context.getPatientService().getPatient(patientId));
			// serialize the the search result or seria
			XStream xStream = new XStream();
			xStream.alias("results", List.class);
			xStream.alias("result", Index.class);
			xStream.alias("patient", Patient.class);
			xStream.alias("summary", Summary.class);
			xStream.registerConverter(new IndexConverter());
			xStream.registerConverter(new SummaryConverter());
			xStream.registerConverter(new PatientConverter());
			xStream.registerConverter(new PatientIdentifierConverter());
			xStream.registerConverter(new PersonNameConverter());
			xStream.toXML(indexes, response.getOutputStream());
		} catch (ContextAuthenticationException e) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}
	}
}
