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

package org.openmrs.module.clinicalsummary.web.controller.response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.service.UtilService;
import org.openmrs.module.clinicalsummary.util.response.ReminderResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/module/clinicalsummary/response/reminderResponseSearch")
public class SearchReminderResponseController {

	private static final Log log = LogFactory.getLog(SearchReminderResponseController.class);

	@RequestMapping(method = RequestMethod.POST)
	public
	@ResponseBody
	Map<Integer, List<ReminderResponse>> searchResponses(final @RequestParam(required = true, value = "patientId") String patientId) {
		Map<Integer, List<ReminderResponse>> responseMap = new HashMap<Integer, List<ReminderResponse>>();

		Patient patient = Context.getPatientService().getPatient(NumberUtils.toInt(patientId));

		UtilService service = Context.getService(UtilService.class);
		List<ReminderResponse> responses = service.getResponses(ReminderResponse.class, patient);
		responseMap.put(patient.getPatientId(), responses);

		return responseMap;
	}

}
