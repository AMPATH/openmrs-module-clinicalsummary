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

package org.openmrs.module.clinicalsummary.web.controller.evaluator.engine;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Cohort;
import org.openmrs.module.clinicalsummary.web.controller.evaluator.EvaluatorStatus;
import org.openmrs.util.OpenmrsUtil;

/**
 */
public class ReminderCohortEvaluatorInstance {

	private static ReminderCohortEvaluatorInstance ourInstanceCohort;

	private ReminderCohortEvaluator reminderCohortEvaluator;

	private ReminderCohortEvaluatorInstance() {
	}

	public static synchronized ReminderCohortEvaluatorInstance getInstance() {
		if (ourInstanceCohort == null)
			ourInstanceCohort = new ReminderCohortEvaluatorInstance();
		return ourInstanceCohort;
	}

	public static synchronized void removeInstance() {
		ourInstanceCohort = null;
	}

	public void evaluate(final Cohort cohort) {
		if (!isRunning()) {
			reminderCohortEvaluator = new ReminderCohortEvaluator(cohort);
			new Thread(reminderCohortEvaluator).start();
		}
	}

	public String getCurrentPatient() {
		if (isRunning())
			return String.valueOf(reminderCohortEvaluator.getCurrentPatientId());
		return StringUtils.EMPTY;
	}

	public String getProcessed() {
		if (isRunning())
			return String.valueOf(reminderCohortEvaluator.getProcessed());
		return StringUtils.EMPTY;
	}

	public String getSize() {
		if (isRunning())
			return String.valueOf(reminderCohortEvaluator.getCohortSize());
		return StringUtils.EMPTY;
	}

	public String getCurrentStatus() {
		if (isRunning())
			return reminderCohortEvaluator.getEvaluatorStatus().getValue();
		return StringUtils.EMPTY;
	}

	public Boolean isRunning() {
		if (reminderCohortEvaluator == null)
			return Boolean.FALSE;

		if (OpenmrsUtil.nullSafeEquals(EvaluatorStatus.EVALUATOR_RUNNING, reminderCohortEvaluator.getEvaluatorStatus()))
			return Boolean.TRUE;

		return Boolean.FALSE;
	}
}
