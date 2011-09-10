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

package org.openmrs.module.clinicalsummary.service.impl;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.OpenmrsObject;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.clinicalsummary.Reminder;
import org.openmrs.module.clinicalsummary.Summary;
import org.openmrs.module.clinicalsummary.db.ReminderDAO;
import org.openmrs.module.clinicalsummary.evaluator.Evaluator;
import org.openmrs.module.clinicalsummary.evaluator.reminder.ReminderEvaluator;
import org.openmrs.module.clinicalsummary.service.ReminderService;

/**
 */
public class ReminderServiceImpl extends BaseOpenmrsService implements ReminderService {

	private static final Log log = LogFactory.getLog(ReminderServiceImpl.class);

	private ReminderDAO reminderDAO;

	/**
	 * Setter for the DAO interface reference that will be called by Spring to inject the actual implementation of the DAO layer
	 *
	 * @param reminderDAO
	 * 		the utilDAO to be injected
	 */
	public void setReminderDAO(final ReminderDAO reminderDAO) {
		if (log.isDebugEnabled())
			log.debug("Wiring up ReminderDAO with ReminderService ...");

		this.reminderDAO = reminderDAO;
	}

	/**
	 * @see ReminderService#evaluateReminder(Patient, org.openmrs.module.clinicalsummary.Summary)
	 */
	@Override
	public void evaluateReminder(final Patient patient, final Summary summary) throws APIException {
		Evaluator evaluator = new ReminderEvaluator();
		evaluator.evaluate(summary, patient, Boolean.FALSE);
	}

	/**
	 * @see ReminderService#saveReminder(org.openmrs.module.clinicalsummary.Reminder)
	 */
	@Override
	public Reminder saveReminder(final Reminder reminder) throws APIException {
		return reminderDAO.saveReminder(reminder);
	}

	/**
	 * @see ReminderService#getReminder(Integer)
	 */
	@Override
	public Reminder getReminder(final Integer id) throws APIException {
		return reminderDAO.getReminder(id);
	}

	/**
	 * @see ReminderService#getReminders(org.openmrs.Patient)
	 */
	@Override
	public List<Reminder> getReminders(final Patient patient) throws APIException {
		return reminderDAO.getReminders(patient);
	}

	/**
	 * @see ReminderService#getLatestReminders(org.openmrs.Patient)
	 */
	@Override
	public List<Reminder> getLatestReminders(final Patient patient) throws APIException {
		return reminderDAO.getLatestReminders(patient);
	}

	/**
	 * @see ReminderService#getReminders(java.util.Map, java.util.Date, java.util.Date)
	 */
	@Override
	public List<Reminder> getReminders(final Map<String, Collection<OpenmrsObject>> restrictions,
	                                   final Date reminderStart, final Date reminderEnd) throws APIException {
		return reminderDAO.getReminders(restrictions, reminderStart, reminderEnd);
	}

	/**
	 * @see ReminderService#aggregateReminders(java.util.Map, java.util.Collection, java.util.Date, java.util.Date)
	 */
	@Override
	public List<Object[]> aggregateReminders(final Map<String, Collection<OpenmrsObject>> restrictions, final Collection<String> groupingProperties,
	                                         final Date reminderStart, final Date reminderEnd) throws APIException {
		return reminderDAO.aggregateReminders(restrictions, groupingProperties, reminderStart, reminderEnd);
	}
}
