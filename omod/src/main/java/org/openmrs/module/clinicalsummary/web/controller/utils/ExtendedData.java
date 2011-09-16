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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.logic.result.Result;
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

	public String generateExtededData() {
		// create the string holder
		StringBuilder builder = new StringBuilder();
		// append number of patient actually resolved by the patient identifier
		builder.append(getDuplicates()).append(FIELD_SEPARATOR);
		// append the initial identifier
		builder.append(getPatient().getPatientIdentifier().getIdentifier()).append(FIELD_SEPARATOR);
		// append the patient internal id
		builder.append(getPatient().getPatientId()).append(FIELD_SEPARATOR);
		// append the patient names
		builder.append(getPatient().getPersonName().getFullName()).append(FIELD_SEPARATOR);
		// append the patient age using the age with unit rule
		builder.append(getTokenResult(AgeWithUnitRule.TOKEN).toString()).append(FIELD_SEPARATOR);
		// append pediatric initial encounter date
		String initialEncounterDatetime = StringUtils.EMPTY;
		if (CollectionUtils.isNotEmpty(getEncounterResults())) {
			Result initialEncounterResult = getEncounterResults().earliest();
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
		builder.append(searchValidObsResult(PAEDIATRICS_WHO_CATEGORY_QUERY).toString()).append(FIELD_SEPARATOR);
		// append the cdc class
		builder.append(searchValidObsResult(PAEDIATRICS_CDC_CATEGORY_QUERY).toString()).append(FIELD_SEPARATOR);
		// append whether patients on arv or not
		builder.append(getTokenResult(AntiRetroViralRule.TOKEN).size() > 0 ? "Yes" : "No").append(FIELD_SEPARATOR);
		// append mother deceased status
		builder.append(searchValidObsResult(MOTHER_DECEASED_STATUS).toString()).append(FIELD_SEPARATOR);
		// append father deceased status
		builder.append(searchValidObsResult(FATHER_DECEASED_STATUS).toString()).append(FIELD_SEPARATOR);
		return builder.toString();
	}

	/**
	 * Returns a string representation of the object. In general, the
	 * <code>toString</code> method returns a string that
	 * "textually represents" this object. The result should
	 * be a concise but informative representation that is easy for a
	 * person to read.
	 * It is recommended that all subclasses override this method.
	 * <p/>
	 * The <code>toString</code> method for class <code>Object</code>
	 * returns a string consisting of the name of the class of which the
	 * object is an instance, the at-sign character `<code>@</code>', and
	 * the unsigned hexadecimal representation of the hash code of the
	 * object. In other words, this method returns a string equal to the
	 * value of:
	 * <blockquote>
	 * <pre>
	 * getClass().getName() + '@' + Integer.toHexString(hashCode())
	 * </pre></blockquote>
	 *
	 * @return a string representation of the object.
	 */
	@Override
	public String toString() {
		// create the string holder
		StringBuilder builder = new StringBuilder();
		// append number of patient actually resolved by the patient identifier
		builder.append(getDuplicates()).append(FIELD_SEPARATOR);
		// append the initial identifier
		builder.append(getPatient().getPatientIdentifier().getIdentifier()).append(FIELD_SEPARATOR);
		// append the patient internal id
		builder.append(getPatient().getPatientId()).append(FIELD_SEPARATOR);
		// append the patient names
		builder.append(getPatient().getPersonName().getFullName()).append(FIELD_SEPARATOR);
		// append the patient age using the age with unit rule
		builder.append(getTokenResult(AgeWithUnitRule.TOKEN).toString()).append(FIELD_SEPARATOR);
		// append pediatric initial encounter date
		String initialEncounterDatetime = StringUtils.EMPTY;
		if (CollectionUtils.isNotEmpty(getEncounterResults())) {
			Result initialEncounterResult = getEncounterResults().earliest();
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
		builder.append(searchValidObsResult(PAEDIATRICS_WHO_CATEGORY_QUERY).toString()).append(FIELD_SEPARATOR);
		// append the cdc class
		builder.append(searchValidObsResult(PAEDIATRICS_CDC_CATEGORY_QUERY).toString()).append(FIELD_SEPARATOR);
		// append whether patients on arv or not
		builder.append(getTokenResult(AntiRetroViralRule.TOKEN).size() > 0 ? "Yes" : "No").append(FIELD_SEPARATOR);
		// append mother deceased status
		builder.append(searchValidObsResult(MOTHER_DECEASED_STATUS).toString()).append(FIELD_SEPARATOR);
		// append father deceased status
		builder.append(searchValidObsResult(FATHER_DECEASED_STATUS).toString()).append(FIELD_SEPARATOR);
		return builder.toString();
	}
}
