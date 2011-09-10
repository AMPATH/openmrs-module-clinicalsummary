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

import org.openmrs.OpenmrsObject;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.clinicalsummary.Reminder;
import org.openmrs.module.clinicalsummary.Summary;
import org.springframework.transaction.annotation.Transactional;

/**
 */
public interface ReminderService extends OpenmrsService {

	/**
	 * Evaluate a summary template but only on the reminder rules
	 *
	 * @param summary the summary sheet template definition
	 * @param patient the patient id
	 * @throws org.openmrs.api.APIException
	 * @should evaluate the template on a patient
	 */
	@Transactional(readOnly = true)
	void evaluateReminder(final Patient patient, final Summary summary) throws APIException;

	/**
	 * Save a reminder object to the database
	 *
	 * @param reminder the reminder object
	 * @return saved reminder object
	 * @throws APIException
	 */
	@Transactional
	Reminder saveReminder(final Reminder reminder) throws APIException;

	/**
	 * Get a reminder object from the database
	 *
	 * @param id the id
	 * @return the reminder object with the matching id or null when no object is found with matching id
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	Reminder getReminder(final Integer id) throws APIException;

	/**
	 * Search for reminder records for a patient
	 *
	 * @param patient the patient
	 * @return all reminder records for the patient
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	List<Reminder> getReminders(final Patient patient) throws APIException;

	/**
	 * Search for latest reminder records for a patient
	 *
	 * @param patient the patient
	 * @return all reminder records for the patient
	 * @throws APIException
	 */
	List<Reminder> getLatestReminders(Patient patient) throws APIException;

	/**
	 * Search reminder records with certain restrictions on providers, locations and reminder datetime
	 *
	 * @param restrictions  map of the property name and the list of accepted objects
	 * @param reminderStart the reminder generated start time
	 * @param reminderEnd   the reminder generated end time
	 * @return all reminders with matching criteria
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	List<Reminder> getReminders(final Map<String, Collection<OpenmrsObject>> restrictions,
	                            final Date reminderStart, final Date reminderEnd) throws APIException;


	/**
	 * Search reminder records with certain restrictions on providers, locations and reminder datetime
	 *
	 * @param restrictions       map of the property name and the list of accepted objects
	 * @param groupingProperties
	 * @param reminderStart      the reminder generated start time
	 * @param reminderEnd        the reminder generated end time
	 * @return all reminders with matching criteria
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	List<Object[]> aggregateReminders(final Map<String, Collection<OpenmrsObject>> restrictions, final Collection<String> groupingProperties,
	                                  final Date reminderStart, final Date reminderEnd) throws APIException;
}
