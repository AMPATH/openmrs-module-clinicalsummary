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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableNameConstants;
import org.openmrs.module.clinicalsummary.rule.encounter.EncounterWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.pediatric.AgeWithUnitRule;
import org.openmrs.module.clinicalsummary.service.EvaluatorService;
import org.openmrs.util.OpenmrsUtil;
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
@RequestMapping("/module/clinicalsummary/utils/extendedData")
public class ExtendedDataController {

	private static final Log log = LogFactory.getLog(ExtendedDataController.class);

	private static final String INPUT_STUDY_DATA = "identifier.data";

	private static final String OUTPUT_STUDY_DATA = "extended.data";

	private static final String FIELD_SEPARATOR = ",";

	public static final String JAVA_IO_TMPDIR = "java.io.tmpdir";

	private Date studyReferenceDate;

	public ExtendedDataController() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, 2011);
		calendar.set(Calendar.MONTH, Calendar.MARCH);
		calendar.set(Calendar.DATE, 1);
		studyReferenceDate = calendar.getTime();
	}

	@RequestMapping(method = RequestMethod.GET)
	public void populatePage(final ModelMap map) {
	}

	@RequestMapping(method = RequestMethod.POST)
	public void processSubmit(final @RequestParam(required = true, value = "data") MultipartFile data) throws IOException {

		PatientService patientService = Context.getPatientService();
		PatientSetService patientSetService = Context.getPatientSetService();
		EncounterService encounterService = Context.getEncounterService();
		ObsService obsService = Context.getObsService();
		EvaluatorService evaluatorService = Context.getService(EvaluatorService.class);

		File identifierData = new File(System.getProperty(JAVA_IO_TMPDIR), INPUT_STUDY_DATA);
		FileOutputStream identifierDataStream = new FileOutputStream(identifierData);
		FileCopyUtils.copy(data.getInputStream(), identifierDataStream);

		File extendedData = new File(System.getProperty(JAVA_IO_TMPDIR), OUTPUT_STUDY_DATA);
		BufferedWriter writer = new BufferedWriter(new FileWriter(extendedData));

		String line = null;
		BufferedReader reader = new BufferedReader(new FileReader(identifierData));
		while ((line = reader.readLine()) != null) {

			Patient patient = null;
			String[] elements = StringUtils.splitPreserveAllTokens(line);
			Cohort cohort = patientSetService.convertPatientIdentifier(Arrays.asList(elements[0]));
			for (Integer integer : cohort.getMemberIds()) {
				// get the actual patient object
				patient = patientService.getPatient(integer);
				if (patient != null) {
					// create the string holder
					StringBuilder builder = new StringBuilder();
					// append number of patient actually resolved by the patient identifier
					builder.append(cohort.size()).append(FIELD_SEPARATOR);
					// append the initial identifier
					builder.append(elements[0]).append(FIELD_SEPARATOR);
					// append the patient internal id
					builder.append(patient.getPatientId()).append(FIELD_SEPARATOR);
					// append the patient names
					builder.append(patient.getPersonName().getFullName());
					// append the patient age using the age with unit rule
					Result ageWithUnitResults = evaluatorService.evaluate(patient, AgeWithUnitRule.TOKEN, new HashMap<String, Object>());
					builder.append(ageWithUnitResults.toString());
					// search for pediatric initial and return encounter
					// append the primary clinic
					Location primaryLocation = searchPrimaryLocation(patient);
					builder.append(primaryLocation == null ? StringUtils.EMPTY : primaryLocation.getName()).append(FIELD_SEPARATOR);
					// append the gender
					builder.append(patient.getGender()).append(FIELD_SEPARATOR);
					// append first encounter datetime
					Map<String, Object> parameters = new HashMap<String, Object>();
					parameters.put(EvaluableConstants.ENCOUNTER_TYPE,
							Arrays.asList(EvaluableNameConstants.ENCOUNTER_TYPE_PEDIATRIC_INITIAL, EvaluableNameConstants.ENCOUNTER_TYPE_PEDIATRIC_RETURN));
					Result encounterResults = evaluatorService.evaluate(patient, EncounterWithStringRestrictionRule.TOKEN, parameters);
					builder.append(Context.getDateFormat().format(encounterResults.earliest().getResultDate())).append(FIELD_SEPARATOR);
					// append prev module 4 visit
					// append WHO stage
					// append the cdc class
					// append whether patients on arv or not
					// append mother deceased status
					// append father deceased status
					// write the entire string to the extended data's file
					writer.write(builder.toString());
					writer.newLine();
				}
			}
		}

		reader.close();
		writer.close();
	}

	private Result searchMedications(Patient patient, String token) {
		return null;
	}

	private Result searchObservation(Patient patient, String concept) {
		EvaluatorService evaluatorService = Context.getService(EvaluatorService.class);

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(concept));
		Result observationResults = evaluatorService.evaluate(patient, ObsWithStringRestrictionRule.TOKEN, parameters);

		Integer counter = 0;
		Result result = null;
		while (counter < observationResults.size() && result == null) {
			Result encounterResult = observationResults.get(counter++);
			if (encounterResult.getResultDate().before(studyReferenceDate))
				result = encounterResult;
		}
		return result;
	}

	private Result searchVisitForLocation(Patient patient, String location) {
		EvaluatorService evaluatorService = Context.getService(EvaluatorService.class);

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(EvaluableConstants.ENCOUNTER_TYPE,
				Arrays.asList(EvaluableNameConstants.ENCOUNTER_TYPE_PEDIATRIC_INITIAL, EvaluableNameConstants.ENCOUNTER_TYPE_PEDIATRIC_RETURN));
		Result encounterResults = evaluatorService.evaluate(patient, EncounterWithStringRestrictionRule.TOKEN, parameters);

		Integer counter = 0;
		Result result = new Result();
		while (counter < encounterResults.size()) {
			Result encounterResult = encounterResults.get(counter++);
			Encounter encounter = (Encounter) encounterResult.getResultObject();
			String locationName = encounter.getLocation().getName();
			if (StringUtils.equalsIgnoreCase(location, locationName) && encounterResult.getResultDate().before(studyReferenceDate))
				result.add(encounterResult);
		}
		return result;
	}

	private Location searchPrimaryLocation(Patient patient) {
		EvaluatorService evaluatorService = Context.getService(EvaluatorService.class);

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(EvaluableConstants.ENCOUNTER_TYPE,
				Arrays.asList(EvaluableNameConstants.ENCOUNTER_TYPE_PEDIATRIC_INITIAL, EvaluableNameConstants.ENCOUNTER_TYPE_PEDIATRIC_RETURN));
		Result encounterResults = evaluatorService.evaluate(patient, EncounterWithStringRestrictionRule.TOKEN, parameters);

		Location primaryLocation = null;
		if (CollectionUtils.isNotEmpty(encounterResults)) {
			Encounter encounter = (Encounter) encounterResults.latest().getResultObject();
			Location currentLocation = primaryLocation = encounter.getLocation();

			Integer counter = 1;
			while (counter < encounterResults.size() && counter < 3) {
				encounter = (Encounter) encounterResults.get(counter++).getResultObject();
				if (OpenmrsUtil.nullSafeEquals(encounter.getLocation(), currentLocation)
						|| OpenmrsUtil.nullSafeEquals(encounter.getLocation(), primaryLocation))
					return currentLocation;

				if (!OpenmrsUtil.nullSafeEquals(encounter.getLocation(), currentLocation))
					currentLocation = encounter.getLocation();
			}
		}

		return primaryLocation;
	}
}
