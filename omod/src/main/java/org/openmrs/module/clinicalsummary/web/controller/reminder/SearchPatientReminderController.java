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

package org.openmrs.module.clinicalsummary.web.controller.reminder;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.Reminder;
import org.openmrs.module.clinicalsummary.service.ReminderService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/module/clinicalsummary/reminder/searchPatientReminder")
public class SearchPatientReminderController {

	private static final Log log = LogFactory.getLog(SearchPatientReminderController.class);

	private static Map<String, Integer> reminderIds;

	static {
		reminderIds = new HashMap<String, Integer>();
		reminderIds.put("Adult Cluster Reminder", 18);
		reminderIds.put("Adult Creatinine Reminder", 19);
		reminderIds.put("Adult Hemoglobin Reminder", 20);
		reminderIds.put("Adult SGPT Reminder", 21);
		reminderIds.put("Adult CXR Reminder", 22);
		reminderIds.put("Cluster Anti Retro Viral Reminder", 23);
		reminderIds.put("Adult Syphilis Reminder", 24);
		reminderIds.put("Falling CD4 On Anti Retro Viral Reminder", 25);
		reminderIds.put("Adult High Risk Express Care Referral Reminder", 26);
		reminderIds.put("Low Corpuscular Volume Reminder", 27);
		reminderIds.put("Repeat Creatinine Reminder", 28);
		reminderIds.put("High Creatinine Referral Reminder", 29);
		reminderIds.put("Falling Weight Reminder", 30);
		reminderIds.put("Abnormal CXR Reminder", 31);
		reminderIds.put("Abnormal CXR On Anti Retro Viral Reminder", 32);
		reminderIds.put("Low CD4 On Anti Retro Viral Reminder", 33);
		reminderIds.put("Stop Izoniazid Reminder", 34);
		reminderIds.put("Rifampin Nevirapine Contraindication Reminder", 35);
		reminderIds.put("Rifampin Aluvia Contraindication Reminder", 36);
		reminderIds.put("Tenofovir Creatinine Monitoring Reminder", 37);
		reminderIds.put("Over 400 CD4 On Anti Retro Viral Reminder", 38);
		reminderIds.put("Over 500 CD4 No Anti Retro Viral Reminder", 39);
		reminderIds.put("Below 400 CD4 On Anti Retro Viral Reminder", 40);
		reminderIds.put("Below 500 CD4 No Anti Retro Viral Reminder", 41);
		reminderIds.put("Over 300 CD4 Express Care Referral Reminder", 42);
			
	}

	@RequestMapping(method = RequestMethod.POST)
	public
	@ResponseBody
	Map<Integer, List<PatientReminderForm>> searchResponses(final @RequestParam(required = true, value = "patientIdentifier") String patientIdentifier)
			throws InvocationTargetException, IllegalAccessException {

		Map<Integer, List<PatientReminderForm>> responseMap = new HashMap<Integer, List<PatientReminderForm>>();
		List<Patient> patients = Context.getPatientService().getPatients(patientIdentifier);

		ReminderService service = Context.getService(ReminderService.class);
		Cohort cohort = Context.getPatientSetService().convertPatientIdentifier(Arrays.asList(patientIdentifier));
		for (Patient patient : patients) {
			List<PatientReminderForm> patientReminderForms = new ArrayList<PatientReminderForm>();
			for (Reminder reminder : service.getLatestReminders(patient)) {
				PatientReminderForm patientReminderForm = new PatientReminderForm();
				BeanUtils.copyProperties(patientReminderForm, reminder);
				patientReminderForm.setPatientId(reminder.getPatient().getPatientId());
				patientReminderForm.setPatientName(reminder.getPatient().getPersonName().getFullName());
				patientReminderForm.setProviderName(reminder.getProvider().getPersonName().getFullName());
				patientReminderForm.setLocationName(reminder.getLocation().getName());
				patientReminderForm.setDatetime(Context.getDateFormat().format(reminder.getReminderDatetime()));
				patientReminderForm.setReminderId(reminderIds.get(reminder.getToken()));
				patientReminderForm.setToken(reminder.getToken());
				patientReminderForms.add(patientReminderForm);
			}
			responseMap.put(patient.getPatientId(), patientReminderForms);
		}

		return responseMap;
	}

}
