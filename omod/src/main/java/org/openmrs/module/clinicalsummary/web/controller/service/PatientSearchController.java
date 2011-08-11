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
import org.openmrs.module.clinicalsummary.web.controller.service.xstream.PatientConverter;
import org.openmrs.module.clinicalsummary.web.controller.service.xstream.PatientIdentifierConverter;
import org.openmrs.module.clinicalsummary.web.controller.service.xstream.PersonNameConverter;
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
@RequestMapping("/module/clinicalsummary/service/patient/search")
public class PatientSearchController {

	private static final Log log = LogFactory.getLog(PatientSearchController.class);

	/**
	 * @param term
	 * @param response
	 * @should should return patients with name search term
	 * @should should return patients with identifier search term
	 * @should should return empty list when no patient match search term
	 */
	@RequestMapping(method = RequestMethod.GET)
	public void searchPatient(@RequestParam(required = false, value = "username") String username,
	                          @RequestParam(required = false, value = "password") String password,
	                          @RequestParam(required = true, value = "term") String term,
	                          HttpServletResponse response) throws IOException {
		try {
			if (!Context.isAuthenticated())
				Context.authenticate(username, password);

			// search for patients with the matching search term
			List<Patient> patients = Context.getPatientService().getPatients(term);
			// serialize the the search result
			XStream xStream = new XStream();
			xStream.alias("results", List.class);
			xStream.alias("patient", Patient.class);
			xStream.registerConverter(new PatientConverter());
			xStream.registerConverter(new PatientIdentifierConverter());
			xStream.registerConverter(new PersonNameConverter());
			xStream.toXML(patients, response.getOutputStream());
		} catch (ContextAuthenticationException e) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}
	}
}
