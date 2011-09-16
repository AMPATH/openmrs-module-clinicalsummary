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

package org.openmrs.module.clinicalsummary.web.controller.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableNameConstants;
import org.openmrs.module.clinicalsummary.rule.ResultCacheInstance;
import org.openmrs.module.clinicalsummary.rule.encounter.EncounterWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.medication.AntiRetroViralRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.pediatric.AgeWithUnitRule;
import org.openmrs.module.clinicalsummary.service.EvaluatorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 */
@Controller
@RequestMapping("/module/clinicalsummary/utils/extendedData")
public class ExtendedDataGeneralController {

	private static final Log log = LogFactory.getLog(ExtendedDataGeneralController.class);

	private static final String OUTPUT_STUDY_DATA = "extended.data.general";

	public static final String JAVA_IO_TMPDIR = "java.io.tmpdir";

	private Date referenceDate;

	public ExtendedDataGeneralController() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, 2011);
		calendar.set(Calendar.MONTH, Calendar.MARCH);
		calendar.set(Calendar.DATE, 1);
		referenceDate = calendar.getTime();
	}

	@RequestMapping(method = RequestMethod.GET)
	public void populatePage(final ModelMap map) {
	}

	@RequestMapping(method = RequestMethod.POST)
	public void processSubmit(final @RequestParam(required = true, value = "data") MultipartFile data) throws IOException {

		PatientService patientService = Context.getPatientService();
		PatientSetService patientSetService = Context.getPatientSetService();

		File extendedData = new File(System.getProperty(JAVA_IO_TMPDIR), OUTPUT_STUDY_DATA);
		BufferedWriter writer = new BufferedWriter(new FileWriter(extendedData));
		Cohort cohort = Context.getCohortService().getCohort("Some Cohort Name");
		for (Integer integer : cohort.getMemberIds()) {
			// get the actual patient object
			Patient patient = patientService.getPatient(integer);
			if (patient != null) {
				ExtendedData extended = new ExtendedData(referenceDate, patient);
				extended.setDuplicates(cohort.size());
				extended.setEncounterResults(searchEncounters(patient));
				extended.addConceptResult(ExtendedData.PAEDIATRICS_WHO_CATEGORY_QUERY, searchObservation(patient, ExtendedData.PAEDIATRICS_WHO_CATEGORY_QUERY));
				extended.addConceptResult(ExtendedData.PAEDIATRICS_CDC_CATEGORY_QUERY, searchObservation(patient, ExtendedData.PAEDIATRICS_CDC_CATEGORY_QUERY));
				extended.addConceptResult(ExtendedData.MOTHER_DECEASED_STATUS, searchObservation(patient, ExtendedData.MOTHER_DECEASED_STATUS));
				extended.addConceptResult(ExtendedData.FATHER_DECEASED_STATUS, searchObservation(patient, ExtendedData.FATHER_DECEASED_STATUS));
				extended.addTokenResult(AntiRetroViralRule.TOKEN, searchMedications(patient, AntiRetroViralRule.TOKEN));
				extended.addTokenResult(AgeWithUnitRule.TOKEN, evaluate(patient, AntiRetroViralRule.TOKEN, new HashMap<String, Object>()));
				writer.write(extended.generateExtededData());
				writer.newLine();
			}
			ResultCacheInstance.getInstance().clearCache(patient);
		}
		writer.close();
	}

	private Result evaluate(Patient patient, String token, Map<String, Object> parameters) {
		EvaluatorService evaluatorService = Context.getService(EvaluatorService.class);
		return evaluatorService.evaluate(patient, token, parameters);
	}

	private Result searchMedications(Patient patient, String token) {
		Result encountersResult = searchEncounters(patient);

		Integer counter = 0;
		Result result = null;
		while (counter < encountersResult.size() && result == null) {
			Result encounterResult = encountersResult.get(counter++);
			if (encounterResult.getResultDate().before(referenceDate)) {
				Map<String, Object> parameters = new HashMap<String, Object>();
				parameters.put(EvaluableConstants.OBS_ENCOUNTER, Arrays.asList(encounterResult.getResultObject()));
				result = evaluate(patient, token, parameters);
			}
		}
		return result;
	}

	private Result searchObservation(Patient patient, String concept) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(concept));

		EvaluatorService evaluatorService = Context.getService(EvaluatorService.class);
		return evaluatorService.evaluate(patient, ObsWithStringRestrictionRule.TOKEN, parameters);
	}

	private Result searchEncounters(Patient patient) {

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(EvaluableConstants.ENCOUNTER_TYPE,
				Arrays.asList(EvaluableNameConstants.ENCOUNTER_TYPE_PEDIATRIC_INITIAL, EvaluableNameConstants.ENCOUNTER_TYPE_PEDIATRIC_RETURN));

		EvaluatorService evaluatorService = Context.getService(EvaluatorService.class);
		return evaluatorService.evaluate(patient, EncounterWithStringRestrictionRule.TOKEN, parameters);
	}
}
