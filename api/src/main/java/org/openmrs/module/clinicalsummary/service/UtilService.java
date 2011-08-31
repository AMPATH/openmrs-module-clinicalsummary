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

import org.openmrs.Location;
import org.openmrs.OpenmrsObject;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.clinicalsummary.enumeration.AgeUnit;
import org.openmrs.module.clinicalsummary.enumeration.Gender;
import org.openmrs.module.clinicalsummary.enumeration.StatusType;
import org.openmrs.module.clinicalsummary.util.obs.OrderedObs;
import org.openmrs.module.clinicalsummary.util.response.DeviceLog;
import org.openmrs.module.clinicalsummary.util.response.Response;
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
	 * @throws APIException
	 */
	WeightStandard saveWeightStandard(final WeightStandard weightStandard) throws APIException;

	/**
	 * Get a weight standard record from the database based on the id
	 *
	 * @param id the weight standard id
	 * @return the weight standard object with the matching id or null when no record with matching id found in the database
	 * @throws APIException
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
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	WeightStandard getWeightStandard(final Gender gender, final AgeUnit ageUnit, final Integer age) throws APIException;

	/**
	 * Save either the result or test obs that can't be matched. A result and test can only be matched together when they are less than 24 hours apart.
	 *
	 * @param orderedObs the ordered observation object
	 * @return the saved ordered observation object
	 * @throws APIException
	 */
	OrderedObs saveOrderedObs(final OrderedObs orderedObs) throws APIException;

	/**
	 * Get an ordered observation based on the id
	 *
	 * @param id the ordered observation id
	 * @return the matching ordered observation or null if there's no matching record in the database
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	OrderedObs getOrderedObs(final Integer id) throws APIException;

	/**
	 * Get all unmatchable ordered observation for a certain patient
	 *
	 * @param patient the patient to be searched
	 * @return list of all unmatchable ordered observations or empty list when there's no unmatchable records found in the database
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	List<OrderedObs> getOrderedObs(final Patient patient) throws APIException;

	/**
	 * Search ordered observations that match the search criteria
	 *
	 * @param restrictions map of ordered obs property to the list of values
	 * @param startTime    the start time of the ordered obs
	 * @param endTime      the end time of the ordered obs
	 * @return list of all matching ordered obs
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	List<OrderedObs> getOrderedObs(final Map<String, Collection<OpenmrsObject>> restrictions,
	                               final Date startTime, final Date endTime) throws APIException;

	/**
	 * Remove all ordered observations record for a certain patient
	 *
	 * @param patients the patients
	 * @return total number of record deleted for the patient
	 * @throws APIException
	 */
	Integer deleteOrderedObs(final List<Patient> patients) throws APIException;

	/**
	 * Search and aggregate the ordered obs based on the projection property specified in the parameter
	 *
	 * @param restrictions       the map between ordered obs property to the list of values
	 * @param groupingProperties list of property on which the projection should be performed
	 * @param statusType         the status type
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
	 * @param responses the responses to be saved
	 * @return list of all saved medication responses
	 * @throws APIException
	 */
	<T extends Response> List<T> saveResponses(final List<T> responses) throws APIException;

	/**
	 * Get an ordered observation based on the id
	 *
	 * @param response the response to be saved
	 * @return the matching ordered observation or null if there's no matching record in the database
	 * @throws APIException
	 */
	<T extends Response> T saveResponse(final T response) throws APIException;

	/**
	 * Get an ordered observation based on the id
	 *
	 * @param id    the id of the response
	 * @param clazz the class of the response to be retrieved
	 * @return the matching ordered observation or null if there's no matching record in the database
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	<T extends Response> T getResponse(final Class<T> clazz, final Integer id) throws APIException;

	/**
	 * Search medication responses by patient
	 *
	 * @param clazz   the class to be retrieved
	 * @param patient the patient
	 * @return list of all medication responses for the particular patient
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	<T extends Response> List<T> getResponses(final Class<T> clazz, final Patient patient) throws APIException;

	/**
	 * Search medication responses by patient
	 *
	 * @param clazz     the class to be retrieved
	 * @param location  the location
	 * @param startDate the earliest date of when the response was created
	 * @param endDate   the latest date of when the response was created
	 * @return list of all medication responses for the particular patient
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	<T extends Response> List<T> getResponses(final Class<T> clazz, final Location location,
	                                          final Date startDate, final Date endDate) throws APIException;


	/**
	 * Save list of device log to the database
	 *
	 * @param deviceLogs the device
	 * @return list of all saved device log
	 * @throws APIException
	 */
	List<DeviceLog> saveDeviceLogs(List<DeviceLog> deviceLogs);
}
