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

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.rule.EvaluableNameConstants;
import org.openmrs.module.clinicalsummary.rule.medication.AntiRetroViralRule;
import org.openmrs.module.clinicalsummary.rule.pediatric.AgeWithUnitRule;
import org.openmrs.util.OpenmrsUtil;

public class ExtendedData {

	private static final Log log = LogFactory.getLog(ExtendedData.class);

	public static final String FIELD_SEPARATOR = ",";

	public static final String PAEDIATRICS_WHO_CATEGORY_QUERY = "PEDS WHO CATEGORY QUERY";

	public static final String PAEDIATRICS_CDC_CATEGORY_QUERY = "PEDS CDC CATEGORY QUERY";

	public static final String MOTHER_DECEASED_STATUS = "MOTHER DECEASED, CODED";

	public static final String FATHER_DECEASED_STATUS = "FATHER DECEASED, CODED";

	private Patient patient;

	private Date referenceDate;

	private Integer duplicates;

	private Result encounterResults;

	private Map<String, Result> conceptResults;

	private Map<String, Result> tokenResults;

	public ExtendedData(Date referenceDate, Patient patient) {
		this.referenceDate = referenceDate;
		this.patient = patient;
	}

	/**
	 * @return
	 */
	public Patient getPatient() {
		return patient;
	}

	/**
	 * @param patient
	 */
	public void setPatient(final Patient patient) {
		this.patient = patient;
	}

	/**
	 * @return
	 */
	public Date getReferenceDate() {
		return referenceDate;
	}

	/**
	 * @param referenceDate
	 */
	public void setReferenceDate(final Date referenceDate) {
		this.referenceDate = referenceDate;
	}

	/**
	 * @return
	 */
	public Integer getDuplicates() {
		return duplicates;
	}

	/**
	 * @param duplicates
	 */
	public void setDuplicates(final Integer duplicates) {
		this.duplicates = duplicates;
	}

	/**
	 * @return
	 */
	public Result getEncounterResults() {
		return encounterResults;
	}

	/**
	 * @param encounterResults
	 */
	public void setEncounterResults(final Result encounterResults) {
		this.encounterResults = encounterResults;
	}

	/**
	 * @return
	 */
	public Map<String, Result> getConceptResults() {
		if (conceptResults == null)
			conceptResults = new HashMap<String, Result>();
		return conceptResults;
	}

	/**
	 * @param conceptResults
	 */
	public void setConceptResults(final Map<String, Result> conceptResults) {
		this.conceptResults = conceptResults;
	}

	/**
	 * @param concept
	 * @param result
	 */
	public void addConceptResult(final String concept, final Result result) {
		getConceptResults().put(concept, result);
	}

	/**
	 * @param concept
	 * @return
	 */
	public Result getConceptResult(final String concept) {
		return getConceptResults().get(concept);
	}

	/**
	 * @return
	 */
	public Map<String, Result> getTokenResults() {
		if (tokenResults == null)
			tokenResults = new HashMap<String, Result>();
		return tokenResults;
	}

	/**
	 * @param tokenResults
	 */
	public void setTokenResults(final Map<String, Result> tokenResults) {
		this.tokenResults = tokenResults;
	}

	/**
	 * @param token
	 * @param result
	 */
	public void addTokenResult(final String token, final Result result) {
		getTokenResults().put(token, result);
	}

	/**
	 * @param token
	 * @return
	 */
	public Result getTokenResult(final String token) {
		return getTokenResults().get(token);
	}

	private Result searchValidObsResult(String concept) {
		Integer counter = 0;
		Result result = null;
		while (counter < getConceptResult(concept).size() && result == null) {
			Result observationResult = getConceptResult(concept).get(counter++);
			if (observationResult.getResultDate().before(referenceDate))
				result = observationResult;
		}
		return result;
	}

	/**
	 * Search for any encounter of a patient to a particular location
	 *
	 * @param location
	 * @return
	 */
	private Result searchVisitCountForLocation(Location location) {
		Integer counter = 0;
		Result result = new Result();
		while (counter < getEncounterResults().size()) {
			Result encounterResult = getEncounterResults().get(counter++);
			Encounter encounter = (Encounter) encounterResult.getResultObject();
			if (OpenmrsUtil.nullSafeEquals(location, encounter.getLocation())
					&& encounterResult.getResultDate().before(referenceDate))
				result.add(encounterResult);
		}
		return result;
	}

	/**
	 * Search for the patient primary clinic
	 *
	 * @return
	 */
	private Location searchPrimaryLocation() {
		Location primaryLocation = null;
		if (CollectionUtils.isNotEmpty(getEncounterResults())) {
			Encounter encounter = (Encounter) getEncounterResults().latest().getResultObject();
			Location currentLocation = primaryLocation = encounter.getLocation();

			Integer counter = 1;
			while (counter < getEncounterResults().size() && counter < 3) {
				encounter = (Encounter) getEncounterResults().get(counter++).getResultObject();
				if (OpenmrsUtil.nullSafeEquals(encounter.getLocation(), currentLocation)
						|| OpenmrsUtil.nullSafeEquals(encounter.getLocation(), primaryLocation))
					return currentLocation;

				if (!OpenmrsUtil.nullSafeEquals(encounter.getLocation(), currentLocation))
					currentLocation = encounter.getLocation();
			}
		}

		return primaryLocation;
	}

	/**
	 * @return
	 */
	private Result searchEnrollmentEncounter() {
		Integer counter = 0;
		Result result = null;
		while (counter < getEncounterResults().size() && result == null) {
			Result observationResult = getEncounterResults().get(counter++);
			if (DateUtils.isSameDay(observationResult.getResultDate(), referenceDate))
				result = observationResult;
		}
		// if we can't find the encounter yet, then we use approximation here +/- 5 days from the reference date.

		if (result == null) {
			Calendar calendar = Calendar.getInstance();

			calendar.setTime(referenceDate);
			calendar.add(Calendar.DATE, 5);
			Date startDate = calendar.getTime();

			calendar.setTime(referenceDate);
			calendar.add(Calendar.DATE, -5);
			Date endDate = calendar.getTime();

			while (counter < getEncounterResults().size() && result == null) {
				Result observationResult = getEncounterResults().get(counter++);
				if (startDate.after(observationResult.getResultDate()) && endDate.before(observationResult.getResultDate()))
					result = observationResult;
			}
		}

		return result;
	}

	/**
	 * @param concept
	 * @return
	 */
	private Result searchAfterEnrollmentObservation(String concept) {
		Integer counter = 0;
		Boolean stopSearch = Boolean.FALSE;
		Result previousResult = null;
		Result currentResult = null;
		while (counter < getConceptResult(concept).size() && !stopSearch) {
			previousResult = currentResult;
			currentResult = getConceptResult(concept).get(counter++);
			log.info("Processing enrollment observations for concept: " + concept + " dated: " + currentResult.getResultDate());
			if (currentResult.getResultDate().before(referenceDate))
				stopSearch = Boolean.TRUE;
		}
		return previousResult;

	}

	/**
	 * @param provider
	 * @return
	 */
	private Result searchProvider(Person provider) {
		Integer counter = 0;
		Result result = new Result();
		log.info("Searching for provider: " + provider.getPersonId());
		while (counter < getEncounterResults().size()) {
			Result encounterResult = getEncounterResults().get(counter++);
			Encounter encounter = (Encounter) encounterResult.getResultObject();
			log.info("Processing provider: " + encounter.getProvider().getPersonId() + " dated: " + encounterResult.getResultDate());
			if (OpenmrsUtil.nullSafeEquals(provider, encounter.getProvider())
					&& encounterResult.getResultDate().before(referenceDate))
				result.add(encounterResult);
		}
		return result;
	}

	public String generateExtededData() {
		// create the string holder
		StringBuilder builder = new StringBuilder();
		// append number of patient actually resolved by the patient identifier
		builder.append(getDuplicates()).append(FIELD_SEPARATOR);
		// append the initial identifier
		PatientIdentifier patientIdentifier = getPatient().getPatientIdentifier();
		builder.append(patientIdentifier != null ? patientIdentifier.getIdentifier() : StringUtils.EMPTY).append(FIELD_SEPARATOR);
		// append the patient internal id
		builder.append(getPatient().getPatientId()).append(FIELD_SEPARATOR);
		// append the patient names
		PersonName personName = getPatient().getPersonName();
		builder.append(personName != null ? personName.getFullName() : StringUtils.EMPTY).append(FIELD_SEPARATOR);
		// append the patient age using the age with unit rule
		builder.append("\"").append(getTokenResult(AgeWithUnitRule.TOKEN).toString()).append("\"").append(FIELD_SEPARATOR);
		// append pediatric initial encounter date
		String initialEncounterDatetime = StringUtils.EMPTY;
		if (CollectionUtils.isNotEmpty(getEncounterResults())) {
			Result initialEncounterResult = getEncounterResults().get(getEncounterResults().size() - 1);
			initialEncounterDatetime = Context.getDateFormat().format(initialEncounterResult.getResultDate());
		}
		builder.append(initialEncounterDatetime).append(FIELD_SEPARATOR);
		// append the primary clinic
		Location primaryLocation = searchPrimaryLocation();
		builder.append(primaryLocation == null ? StringUtils.EMPTY : primaryLocation.getName()).append(FIELD_SEPARATOR);
		// append the gender
		builder.append(getPatient().getGender()).append(FIELD_SEPARATOR);
		// append prev module 4 visit
		Location location = Context.getLocationService().getLocation("MTRH Module 4");
		Result result = searchVisitCountForLocation(location);
		builder.append(result.size()).append(FIELD_SEPARATOR);
		// append WHO stage
		Result whoStageResult = searchValidObsResult(PAEDIATRICS_WHO_CATEGORY_QUERY);
		builder.append(whoStageResult != null ? whoStageResult.toString() : StringUtils.EMPTY).append(FIELD_SEPARATOR);
		// append the cdc class
		Result cdcClassResult = searchValidObsResult(PAEDIATRICS_CDC_CATEGORY_QUERY);
		builder.append(cdcClassResult != null ? cdcClassResult.toString() : StringUtils.EMPTY).append(FIELD_SEPARATOR);
		// append whether patients on arv or not
		Result antiRetroViralResults = getTokenResult(AntiRetroViralRule.TOKEN);
		builder.append(CollectionUtils.isNotEmpty(antiRetroViralResults) ? "YES" : "NO").append(FIELD_SEPARATOR);
		// append mother deceased status
		Result motherStatusResult = searchValidObsResult(MOTHER_DECEASED_STATUS);
		builder.append(motherStatusResult != null ? motherStatusResult.toString() : StringUtils.EMPTY).append(FIELD_SEPARATOR);
		// append father deceased status
		Result fatherStatusResult = searchValidObsResult(FATHER_DECEASED_STATUS);
		builder.append(fatherStatusResult != null ? fatherStatusResult.toString() : StringUtils.EMPTY).append(FIELD_SEPARATOR);

		Result controlResult = searchValidObsResult(EvaluableNameConstants.PEDIATRIC_STUDY_CONTROL_GROUP);
		Result interventionResult = searchValidObsResult(EvaluableNameConstants.PEDIATRIC_STUDY_INTERVENTION_GROUP);
		if (controlResult != null)
			builder.append("CONTROL").append(FIELD_SEPARATOR).append(Context.getDateFormat().format(controlResult.getResultDate()));
		if (interventionResult != null)
			builder.append("INTERVENTION").append(FIELD_SEPARATOR).append(Context.getDateFormat().format(interventionResult.getResultDate()));
		return builder.toString();
	}

	public String generateEncounterData() {
		StringBuilder builder = new StringBuilder();
		// append the initial identifier
		PatientIdentifier patientIdentifier = getPatient().getPatientIdentifier();
		builder.append(patientIdentifier != null ? patientIdentifier.getIdentifier() : StringUtils.EMPTY).append(FIELD_SEPARATOR);
		// append the patient internal id
		builder.append(getPatient().getPatientId()).append(FIELD_SEPARATOR);
		// append the patient names
		PersonName personName = getPatient().getPersonName();
		builder.append(personName != null ? personName.getFullName() : StringUtils.EMPTY).append(FIELD_SEPARATOR);
		Result enrollmentEncounter = searchEnrollmentEncounter();
		if (enrollmentEncounter != null) {
			Encounter encounter = (Encounter) enrollmentEncounter.getResultObject();
			builder.append(encounter.getEncounterId()).append(FIELD_SEPARATOR);
			builder.append(Context.getDateFormat().format(encounter.getEncounterDatetime())).append(FIELD_SEPARATOR);
			builder.append(encounter.getProvider().getPersonId()).append(FIELD_SEPARATOR);
			builder.append(encounter.getProvider().getPersonName().getFullName()).append(FIELD_SEPARATOR);
			builder.append(searchProvider(encounter.getProvider()).size()).append(FIELD_SEPARATOR);
		}

		for (String concept : getConceptResults().keySet()) {
			log.info("Searching concept name: " + concept);
			Result afterEnrollmentObservation = searchAfterEnrollmentObservation(concept);
			if (afterEnrollmentObservation != null) {
				Obs obs = (Obs) afterEnrollmentObservation.getResultObject();
				builder.append(obs.getObsId()).append(FIELD_SEPARATOR);
				builder.append(Context.getDateFormat().format(afterEnrollmentObservation.getResultDate())).append(FIELD_SEPARATOR);
				builder.append(afterEnrollmentObservation.toString());
			}
		}
		return builder.toString();
	}
}
