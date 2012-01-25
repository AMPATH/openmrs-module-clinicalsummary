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

import java.util.Date;
import java.util.List;

import org.openmrs.Cohort;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.clinicalsummary.Constants;
import org.openmrs.module.clinicalsummary.Index;
import org.openmrs.module.clinicalsummary.Summary;
import org.springframework.transaction.annotation.Transactional;

/**
 * All service contract for operation on the Index object
 */
@Transactional
public interface IndexService extends OpenmrsService {

	/**
	 * Save an index entry in the database.
	 *
	 * @param index the index to be saved
	 * @return saved index with assigned id
	 * @throws APIException
	 * @should save the Index to the database
	 */
	@Authorized({Constants.PRIVILEGE_PRINT_SUMMARY, Constants.PRIVILEGE_VIEW_SUMMARY})
	Index saveIndex(final Index index) throws APIException;

	/**
	 * Generate an index for a patient and a summary template. The generated index then can be saved to retrieve the summary sheet in the future
	 *
	 * @param patient the patient
	 * @param summary the summary
	 * @return the generated Index object
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized({Constants.PRIVILEGE_PRINT_SUMMARY, Constants.PRIVILEGE_VIEW_SUMMARY})
	Index generateIndex(final Patient patient, final Summary summary) throws APIException;

	/**
	 * Get an index record from the database with the matching Index id
	 *
	 * @param id the Index id
	 * @return Index with the matching id or null when no index is found
	 * @throws APIException
	 * @should return index with matching id
	 * @should return null when no Index match the id
	 */
	@Transactional(readOnly = true)
	@Authorized({Constants.PRIVILEGE_PRINT_SUMMARY, Constants.PRIVILEGE_VIEW_SUMMARY})
	Index getIndex(final Integer id) throws APIException;

	/**
	 * Get an index record from the database with the matching Index id
	 *
	 * @param patient the patient
	 * @param summary the summary
	 * @return Index for the patient on a certain template or null when no index is found
	 * @throws APIException
	 * @should return index with matching patient and summary
	 * @should return null when no Index match the patient and summary
	 */
	@Transactional(readOnly = true)
	@Authorized({Constants.PRIVILEGE_PRINT_SUMMARY, Constants.PRIVILEGE_VIEW_SUMMARY})
	Index getIndex(final Patient patient, final Summary summary) throws APIException;

	/**
	 * Get all indexes for a particular patients
	 *
	 * @param patient the patient
	 * @return all indexes for the patient or empty list when no matching Index are found
	 * @throws APIException
	 * @should return list of all index for a patient
	 * @should return empty list when no index are found for the patient
	 */
	@Transactional(readOnly = true)
	@Authorized({Constants.PRIVILEGE_PRINT_SUMMARY, Constants.PRIVILEGE_VIEW_SUMMARY})
	List<Index> getIndexes(final Patient patient) throws APIException;

	/**
	 * Get all indexes based on the location of patient latest encounter
	 *
	 * @param location       the intended location
	 * @param summary the summary
	 * @param startVisitDate start date of the return visit
	 * @param endVisitDate   end date of the return visit
	 * @return all indexes for the location or empty list when no index are found for the location
	 * @throws APIException
	 * @should return all indexes for the location
	 * @should return empty list when no index are found for the location
	 */
	@Transactional(readOnly = true)
	@Authorized({Constants.PRIVILEGE_PRINT_SUMMARY, Constants.PRIVILEGE_VIEW_SUMMARY})
	List<Index> getIndexes(final Location location, final Summary summary, final Date startVisitDate, final Date endVisitDate) throws APIException;

	/**
	 * Get all indexes based on the location of patient latest encounter
	 *
	 * @param location       the intended location
	 * @param summary
	 * @param startVisitDate start date of the return visit
	 * @param endVisitDate   end date of the return visit
	 * @return all indexes for the location or empty list when no index are found for the location
	 * @throws APIException
	 * @should return all indexes for the location
	 * @should return empty list when no index are found for the location
	 */
	@Transactional(readOnly = true)
	@Authorized({Constants.PRIVILEGE_PRINT_SUMMARY, Constants.PRIVILEGE_VIEW_SUMMARY})
	Cohort getIndexCohort(final Location location, final Summary summary, final Date startVisitDate, final Date endVisitDate) throws APIException;

    /**
     * Get all indexes based on the cohort of patient
     *
     * @param cohort       the intended cohort
     * @param summary
     * @return all indexes for the cohort or empty list when no index are found for the cohort
     * @throws APIException
     * @should return all indexes for the cohort
     * @should return empty list when no index are found for the cohort
     */
    @Transactional(readOnly = true)
    @Authorized({Constants.PRIVILEGE_PRINT_SUMMARY, Constants.PRIVILEGE_VIEW_SUMMARY})
    Cohort getIndexCohort(Cohort cohort, Summary summary) throws APIException;

	/**
	 * Save the initial data value for a location. Initial date will be used to prevent printing old summaries for satellite sites.
	 *
	 * @param location the location
	 * @param date     the initial date
	 * @throws APIException
	 */
	@Authorized({Constants.PRIVILEGE_MANAGE_SUMMARY})
	Integer saveInitialDate(final Location location, final Date date) throws APIException;

	/**
	 * Return the initial date for a location
	 *
	 * @param location the location
	 * @return the initial date for the location
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized({Constants.PRIVILEGE_MANAGE_SUMMARY})
	Date getInitialDate(final Location location) throws APIException;
}
