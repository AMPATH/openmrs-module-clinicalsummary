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

package org.openmrs.module.clinicalsummary.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.OpenmrsObject;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.clinicalsummary.enumeration.AgeUnit;
import org.openmrs.module.clinicalsummary.enumeration.Gender;
import org.openmrs.module.clinicalsummary.enumeration.StatusType;
import org.openmrs.module.clinicalsummary.util.obs.OrderedObs;
import org.openmrs.module.clinicalsummary.util.response.MedicationResponse;
import org.openmrs.module.clinicalsummary.util.response.ReminderResponse;
import org.openmrs.module.clinicalsummary.util.weight.WeightStandard;
import org.springframework.transaction.annotation.Transactional;

/**
 */
@Transactional
public interface UtilService extends OpenmrsService {

	/**
	 * Save a  weight standard reference to the database
	 *
	 * @param weightStandard the weight standard
	 * @return saved weight standard
	 */
	WeightStandard saveWeightStandard(final WeightStandard weightStandard) throws APIException;

	/**
	 * Get a weight standard record from the database based on the id
	 *
	 * @param id the weight standard id
	 * @return the weight standard object with the matching id or null when no record with matching id found in the database
	 */
	@Transactional(readOnly = true)
	WeightStandard getWeightStandard(final Integer id) throws APIException;

	/**
	 * Get a weight standard record based on a set of criteria. All three parameter will uniquely identify a weight standard record
	 *
	 * @param gender  the gender
	 * @param ageUnit the age unit
	 * @param age     the age value
	 * @return the matching weight standard object from the database or null when no matching record found in the database
	 */
	@Transactional(readOnly = true)
	WeightStandard getWeightStandard(final Gender gender, final AgeUnit ageUnit, final Integer age) throws APIException;

	/**
	 * Save either the result or test obs that can't be matched. A result and test can only be matched together when they are less than 24 hours apart.
	 *
	 * @param orderedObs the ordered observation object
	 * @return the saved ordered observation object
	 */
	OrderedObs saveOrderedObs(final OrderedObs orderedObs) throws APIException;

	/**
	 * Get an ordered observation based on the id
	 *
	 * @param id tje ordered observation id
	 * @return the matching ordered observation or null if there's no matching record in the database
	 */
	@Transactional(readOnly = true)
	OrderedObs getOrderedObs(final Integer id) throws APIException;

	/**
	 * Get all unmatchable ordered observation for a certain patient
	 *
	 * @param patient the patient to be searched
	 * @return list of all unmatchable ordered observations or empty list when there's no unmatchable records found in the database
	 */
	@Transactional(readOnly = true)
	List<OrderedObs> getOrderedObs(final Patient patient) throws APIException;

	/**
	 * Search ordered observations that match the search criteria
	 *
	 * @param restrictions map of ordered obs property to the list of values
	 * @param startTime    the start time of the ordered obs
	 * @param endTime      the end time of the ordered obs
	 */
	@Transactional(readOnly = true)
	List<OrderedObs> getOrderedObs(final Map<String, Collection<OpenmrsObject>> restrictions,
	                               final Date startTime, final Date endTime) throws APIException;

	/**
	 * Remove all ordered observations record for a certain patient
	 *
	 * @param patients
	 * @return total number of record deleted for the patient
	 * @throws APIException
	 */
	Integer deleteOrderedObs(final List<Patient> patients) throws APIException;

	/**
	 * Search and aggregate the ordered obs based on the projection property specified in the parameter
	 *
	 * @param restrictions       the map between ordered obs property to the list of values
	 * @param groupingProperties list of property on which the projection should be performed
	 * @param statusType
	 * @param startTime          the start time of the ordered obs
	 * @param endTime            the end time of the ordered obs
	 * @return list of object array for the specific projection
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	List<Object[]> aggregateOrderedObs(final Map<String, Collection<OpenmrsObject>> restrictions, final Collection<String> groupingProperties,
	                                   final StatusType statusType, final Date startTime, final Date endTime) throws APIException;

	/**
	 * Search and aggregate the ordered obs based on the projection property specified in the parameter
	 *
	 * @param restrictions the map between ordered obs property to the list of values
	 * @param projections  list of property on which the projection should be performed
	 * @return list of object array for the specific projection
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	List<Object[]> aggregateOrderedObs(final Map<String, Collection<OpenmrsObject>> restrictions,
	                                   final Collection<String> projections) throws APIException;

	/**
	 * Save list of all medication responses to the database
	 *
	 *
	 * @param responses@return list of all saved medication responses
	 */
	List<? extends BaseOpenmrsData> saveResponses(List<? extends BaseOpenmrsData> responses);

	/**
	 * Search medication responses by patient
	 *
	 * @param patient the patient
	 * @return list of all medication responses for the particular patient
	 */
	@Transactional(readOnly = true)
	List<MedicationResponse> getMedicationResponses(Patient patient);

	/**
	 * Search reminder responses by patient
	 *
	 * @param patient the patient
	 * @return list of all reminder responses for the particular patient
	 */
	@Transactional(readOnly = true)
	List<ReminderResponse> getReminderResponses(Patient patient);
}
