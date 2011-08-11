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

import org.openmrs.Patient;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.clinicalsummary.Constants;
import org.openmrs.module.clinicalsummary.Loggable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service contract for all operation on Loggable object
 */
@Transactional
public interface LoggableService extends OpenmrsService {

	/**
	 * Save an loggable entry to the database
	 *
	 * @param loggable
	 * 		the loggable to be saved
	 *
	 * @return saved loggable entry
	 *
	 * @throws APIException
	 * @should save the loggable object to the database
	 */
	@Authorized({Constants.PRIVILEGE_MANAGE_SUMMARY})
	Loggable saveLoggable(final Loggable loggable) throws APIException;

	/**
	 * Get an entry of Loggable from the database based on the id of the Loggable object
	 *
	 * @param id
	 * 		the id of the Loggable object
	 *
	 * @return the matching Loggable object or null when no Loggable object can be found in the system
	 *
	 * @throws APIException
	 * @should return loggable object with the matching id
	 * @should return null when no loggable object are found
	 */
	@Transactional(readOnly = true)
	@Authorized({Constants.PRIVILEGE_MANAGE_SUMMARY})
	Loggable getLoggable(final Integer id) throws APIException;

	/**
	 * Get loggable list for a patient
	 *
	 * @param patient
	 * 		the patient from which the loggable are coming from
	 *
	 * @return list of all Loggable object for the patient or empty list when no Loggable can be found for the patient
	 *
	 * @throws APIException
	 * @should return list of errors for the patient
	 * @should return empty list when no errors are found for the patient
	 */
	@Transactional(readOnly = true)
	@Authorized({Constants.PRIVILEGE_MANAGE_SUMMARY})
	List<Loggable> getLoggables(final Patient patient) throws APIException;
}
