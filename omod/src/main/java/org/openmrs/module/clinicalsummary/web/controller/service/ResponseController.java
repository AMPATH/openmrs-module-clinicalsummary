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

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.module.clinicalsummary.Loggable;
import org.openmrs.module.clinicalsummary.cache.CacheUtils;
import org.openmrs.module.clinicalsummary.enumeration.MedicationType;
import org.openmrs.module.clinicalsummary.service.LoggableService;
import org.openmrs.module.clinicalsummary.service.UtilService;
import org.openmrs.module.clinicalsummary.util.response.DeviceLog;
import org.openmrs.module.clinicalsummary.util.response.MedicationResponse;
import org.openmrs.module.clinicalsummary.util.response.ReminderResponse;
import org.openmrs.module.clinicalsummary.util.response.Response;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/module/clinicalsummary/service/response")
public class ResponseController {

	private static final Log log = LogFactory.getLog(ResponseController.class);

	private static final String USERNAME = "username";

	private static final String PASSWORD = "password";

	private static final String HEADER_REMINDER = "reminder";

	private static final String HEADER_LOG = "log";

	@RequestMapping(method = RequestMethod.POST)
	public void processResponse(@RequestParam(required = false, value = USERNAME) String username,
	                            @RequestParam(required = false, value = PASSWORD) String password,
	                            HttpServletRequest request, HttpServletResponse response) throws IOException {

		log.info("Processing responses from the android devices ...");

		try {
			if (!Context.isAuthenticated())
				Context.authenticate(username, password);

			Map parameterMap = request.getParameterMap();
			for (Object parameterName : parameterMap.keySet()) {
				// skip the username and password request parameter
				if (!StringUtils.equalsIgnoreCase(USERNAME, String.valueOf(parameterName))
						&& !StringUtils.equalsIgnoreCase(PASSWORD, String.valueOf(parameterName))) {

					String id = String.valueOf(parameterName);
					String[] parameterValues = (String[]) parameterMap.get(id);

					log.info("ID: " + id);
					for (String parameterValue : parameterValues)
						log.info("Parameter Values: " + String.valueOf(parameterValue));

					Patient patient = Context.getPatientService().getPatient(NumberUtils.toInt(id));

					if (patient != null) {
						List<Response> responses = new ArrayList<Response>();
						for (String parameterValue : parameterValues) {
							String[] parameter = StringUtils.split(parameterValue, "|");
							if (StringUtils.equalsIgnoreCase(HEADER_REMINDER, parameter[0])) {
								ReminderResponse reminderResponse = new ReminderResponse();
								reminderResponse.setPatient(patient);
								reminderResponse.setProvider(Context.getAuthenticatedUser().getPerson());
								reminderResponse.setToken(parameter[1]);
								reminderResponse.setResponse(parameter[2]);
								reminderResponse.setComment(parameter[3]);
								reminderResponse.setLocation(Context.getLocationService().getLocation(parameter[4]));
								reminderResponse.setDatetime(parse(parameter[5]));
								reminderResponse.setPresent(NumberUtils.toInt(parameter[6]));
								// add to the list
								responses.add(reminderResponse);
							} else {
								// get the concept from the cache or search the database when the concept is not in the cache
								Concept concept = CacheUtils.getConcept(parameter[1]);
								// if we still can't find the concept, the log this as an error
								if (concept == null) {
									Loggable loggable = new Loggable(patient, "Unable to find concept with name: " + parameter[1] + " in the database.");
									Context.getService(LoggableService.class).saveLoggable(loggable);
								} else {
									MedicationResponse medicationResponse = new MedicationResponse();
									medicationResponse.setPatient(patient);
									medicationResponse.setProvider(Context.getAuthenticatedUser().getPerson());
									// get the correct medication type
									for (MedicationType medicationType : MedicationType.values())
										if (StringUtils.equals(medicationType.getValue(), parameter[0]))
											medicationResponse.setMedicationType(medicationType);
									medicationResponse.setMedication(concept);
									medicationResponse.setStatus(NumberUtils.toInt(parameter[2]));
									medicationResponse.setLocation(Context.getLocationService().getLocation(parameter[3]));
									medicationResponse.setDatetime(parse(parameter[4]));
									medicationResponse.setPresent(NumberUtils.toInt(parameter[5]));
									// add to the list
									responses.add(medicationResponse);
								}
							}
						}
						Context.getService(UtilService.class).saveResponses(responses);
					} else {
						// the id is not patient id but it's a device id
						List<DeviceLog> deviceLogs = new ArrayList<DeviceLog>();
						for (String parameterValue : parameterValues) {
							String[] parameter = StringUtils.split(parameterValue, "|");
							if (StringUtils.equalsIgnoreCase(HEADER_LOG, parameter[0])) {
								DeviceLog deviceLog = new DeviceLog();
								deviceLog.setDeviceId(id);
								deviceLog.setKey(parameter[1]);
								deviceLog.setValue(parameter[2]);
								deviceLog.setTimestamp(parameter[3]);
								deviceLog.setUser(Context.getAuthenticatedUser().getPerson());
								deviceLogs.add(deviceLog);
							}
							Context.getService(UtilService.class).saveDeviceLogs(deviceLogs);
						}
					}
				}
			}
			response.setStatus(HttpServletResponse.SC_OK);
		} catch (ContextAuthenticationException e) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}
	}

	private Date parse(String dateString) {
		Date date = null;
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
			date = dateFormat.parse(dateString);
		} catch (ParseException e) {
			log.error("Parsing " + dateString + " to Date object failed.");
		}
		return date;
	}

}
