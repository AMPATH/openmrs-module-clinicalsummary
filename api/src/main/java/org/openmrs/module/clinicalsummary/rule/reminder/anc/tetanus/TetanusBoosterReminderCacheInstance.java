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

package org.openmrs.module.clinicalsummary.rule.reminder.anc.tetanus;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;

import java.util.HashMap;
import java.util.Map;

public class TetanusBoosterReminderCacheInstance {

	private static final Log log = LogFactory.getLog(TetanusBoosterReminderCacheInstance.class);

	private static final TetanusBoosterReminderCacheInstance ourInstance = new TetanusBoosterReminderCacheInstance();

	public static TetanusBoosterReminderCacheInstance getInstance() {
		return ourInstance;
	}

	private final Map<String, Boolean> reminderMap;

	private Patient patient;

	private TetanusBoosterReminderCacheInstance() {
		reminderMap = new HashMap<String, Boolean>();
	}

	/**
	 * @param token
	 * @param displayReminder
	 */
	synchronized void addReminderCache(String token, Boolean displayReminder) {
		reminderMap.put(token, displayReminder);
	}

	/**
	 *
	 */
	synchronized void clearCache() {
		reminderMap.clear();
	}

	/**
	 * @param token
	 * @return
	 */
	Boolean getReminder(String token) {
		return reminderMap.get(token);
	}

	/**
	 * @param patient
	 */
	synchronized void setPatient(Patient patient) {
		this.patient = patient;
	}

	/**
	 * @return
	 */
	Patient getPatient() {
		return patient;
	}

}
