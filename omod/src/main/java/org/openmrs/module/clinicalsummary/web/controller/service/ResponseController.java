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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.module.clinicalsummary.Loggable;
import org.openmrs.module.clinicalsummary.cache.CacheUtils;
import org.openmrs.module.clinicalsummary.service.LoggableService;
import org.openmrs.module.clinicalsummary.service.UtilService;
import org.openmrs.module.clinicalsummary.util.response.MedicationResponse;
import org.openmrs.module.clinicalsummary.util.response.MedicationType;
import org.openmrs.module.clinicalsummary.util.response.ReminderResponse;
import org.openmrs.module.clinicalsummary.web.controller.WebUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/module/clinicalsummary/service/response")
public class ResponseController {

	private static final Log log = LogFactory.getLog(ResponseController.class);

	private static final String USERNAME = "username";

	private static final String PASSWORD = "password";

	private static final String HEADER_REMINDER = "reminder";

	@RequestMapping(method = RequestMethod.POST)
	public void processResponse(@RequestParam(required = false, value = USERNAME) String username,
	                            @RequestParam(required = false, value = PASSWORD) String password,
	                            HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			if (!Context.isAuthenticated())
				Context.authenticate(username, password);

			List<BaseOpenmrsData> responses = new ArrayList<BaseOpenmrsData>();

			Map parameterMap = request.getParameterMap();
			for (Object parameterName : parameterMap.keySet()) {
				// skip the username and password request parameter
				if (!StringUtils.equalsIgnoreCase(USERNAME, String.valueOf(parameterName))
						&& !StringUtils.equalsIgnoreCase(PASSWORD, String.valueOf(parameterName))) {

					String patientId = String.valueOf(parameterName);
					Patient patient = Context.getPatientService().getPatient(NumberUtils.toInt(patientId));

					String[] parameterValues = (String[]) parameterMap.get(parameterName);
					for (String parameterValue : parameterValues) {
						String[] parameter = StringUtils.split(parameterValue, "|");
						if (StringUtils.equalsIgnoreCase(HEADER_REMINDER, parameter[0])) {
							ReminderResponse reminderResponse = new ReminderResponse();
							reminderResponse.setPatient(patient);
							reminderResponse.setProvider(Context.getAuthenticatedUser().getPerson());
							reminderResponse.setToken(parameter[1]);
							reminderResponse.setResponse(parameter[2]);
							reminderResponse.setReminderDatetime(WebUtils.parse(parameter[2], new Date()));
							// add to the list
							responses.add(reminderResponse);
						} else {
							MedicationResponse medicationResponse = new MedicationResponse();
							medicationResponse.setPatient(patient);
							medicationResponse.setProvider(Context.getAuthenticatedUser().getPerson());

							for (MedicationType medicationType : MedicationType.values())
								if (StringUtils.equals(medicationType.getValue(), parameter[0]))
									medicationResponse.setMedicationType(medicationType);

							// get the concept from the cache or search the database when the concept is not in the cache
							Concept concept = CacheUtils.getConcept(parameter[1]);
							// if we still can't find the concept, the log this as an error
							if (concept == null) {
								Loggable loggable = new Loggable(patient, "Concept name: " + parameter[1] + " not found in the database");
								Context.getService(LoggableService.class).saveLoggable(loggable);
								continue;
							}
							medicationResponse.setConcept(concept);

							medicationResponse.setMedicationDatetime(WebUtils.parse(parameter[2], new Date()));
							medicationResponse.setStatus(NumberUtils.toInt(parameter[3]));
							// add to the list
							responses.add(medicationResponse);
						}
					}
					Context.getService(UtilService.class).saveResponses(responses);
				}
			}
			response.setStatus(HttpServletResponse.SC_OK);
		} catch (ContextAuthenticationException e) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}
	}

}
