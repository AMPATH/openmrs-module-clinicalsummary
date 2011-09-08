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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
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
	Map<Integer, List<ReminderResponseForm>> searchResponses(final @RequestParam(required = true, value = "patientIdentifiers") String patientIdentifiers)
			throws InvocationTargetException, IllegalAccessException {

		Map<Integer, List<ReminderResponseForm>> responseMap = new HashMap<Integer, List<ReminderResponseForm>>();

		String[] patientIdentifier = StringUtils.split(patientIdentifiers);

		UtilService service = Context.getService(UtilService.class);
		Cohort cohort = Context.getPatientSetService().convertPatientIdentifier(Arrays.asList(patientIdentifier));
		for (Integer patientId : cohort.getMemberIds()) {
			Patient patient = Context.getPatientService().getPatient(patientId);
			List<ReminderResponseForm> reminderResponseForms = new ArrayList<ReminderResponseForm>();
			for (ReminderResponse reminderResponse : service.getResponses(ReminderResponse.class, patient)) {
				ReminderResponseForm reminderResponseForm = new ReminderResponseForm();
				BeanUtils.copyProperties(reminderResponseForm, reminderResponse);
				reminderResponseForm.setPatientId(reminderResponse.getPatient().getPatientId());
				reminderResponseForm.setPatientName(reminderResponse.getPatient().getPersonName().getFullName());
				reminderResponseForm.setProviderName(reminderResponse.getProvider().getPersonName().getFullName());
				reminderResponseForm.setLocationName(reminderResponse.getLocation().getName());
				reminderResponseForm.setDatetime(Context.getDateFormat().format(reminderResponse.getDatetime()));
				reminderResponseForm.setToken(reminderResponse.getToken());
				reminderResponseForm.setResponse(reminderResponse.getResponse());
				reminderResponseForm.setComment(StringUtils.isEmpty(reminderResponse.getComment()) ? StringUtils.EMPTY : reminderResponse.getComment());
				reminderResponseForms.add(reminderResponseForm);
			}
			responseMap.put(patient.getPatientId(), reminderResponseForms);
		}

		return responseMap;
	}

}
