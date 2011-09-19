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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
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
import org.openmrs.module.clinicalsummary.service.EvaluatorService;
import org.openmrs.module.clinicalsummary.web.controller.WebUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 */
@Controller
@RequestMapping("/module/clinicalsummary/utils/extendedDataEncounter")
public class ExtendedDataEncounterController {

	private static final Log log = LogFactory.getLog(ExtendedDataEncounterController.class);

	private static final String INPUT_STUDY_DATA = "identifier.data";

	private static final String OUTPUT_STUDY_DATA = "extended.data.encounter";

	public static final String JAVA_IO_TMPDIR = "java.io.tmpdir";

	@RequestMapping(method = RequestMethod.GET)
	public void populatePage(final ModelMap map) {
		map.put("cohorts", Context.getCohortService().getAllCohorts());
	}

	@RequestMapping(method = RequestMethod.POST)
	public void processSubmit(final @RequestParam(required = true, value = "data") MultipartFile data) throws IOException {

		PatientService patientService = Context.getPatientService();
		PatientSetService patientSetService = Context.getPatientSetService();

		File identifierData = new File(System.getProperty(JAVA_IO_TMPDIR), INPUT_STUDY_DATA);
		FileOutputStream identifierDataStream = new FileOutputStream(identifierData);
		FileCopyUtils.copy(data.getInputStream(), identifierDataStream);

		File extendedData = new File(System.getProperty(JAVA_IO_TMPDIR), OUTPUT_STUDY_DATA);
		BufferedWriter writer = new BufferedWriter(new FileWriter(extendedData));

		String line;
		BufferedReader reader = new BufferedReader(new FileReader(identifierData));
		while ((line = reader.readLine()) != null) {

			Patient patient = null;
			String[] elements = StringUtils.splitPreserveAllTokens(line);
			Cohort cohort = patientSetService.convertPatientIdentifier(Arrays.asList(elements[0]));
			Date referenceDate = WebUtils.parse(elements[1], new Date());
			for (Integer integer : cohort.getMemberIds()) {
				// get the actual patient object
				patient = patientService.getPatient(integer);
				if (patient != null) {
					ExtendedData extended = new ExtendedData(referenceDate, patient);
					extended.setDuplicates(cohort.size());
					extended.setEncounterResults(searchEncounters(patient));
					writer.write(extended.generateEncounterData());
					writer.newLine();
				}
			}
			ResultCacheInstance.getInstance().clearCache(patient);
		}

		reader.close();
		writer.close();
	}

	private Result searchEncounters(Patient patient) {

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(EvaluableConstants.ENCOUNTER_TYPE,
				Arrays.asList(EvaluableNameConstants.ENCOUNTER_TYPE_PEDIATRIC_INITIAL, EvaluableNameConstants.ENCOUNTER_TYPE_PEDIATRIC_RETURN));

		EvaluatorService evaluatorService = Context.getService(EvaluatorService.class);
		return evaluatorService.evaluate(patient, EncounterWithStringRestrictionRule.TOKEN, parameters);
	}
}
