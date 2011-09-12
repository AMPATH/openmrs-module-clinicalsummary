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
public class RuleCohortEvaluatorInstance {

	private static RuleCohortEvaluatorInstance ourInstanceCohort;

	private RuleCohortEvaluator ruleCohortEvaluator;

	private RuleCohortEvaluatorInstance() {
	}

	public static synchronized RuleCohortEvaluatorInstance getInstance() {
		if (ourInstanceCohort == null)
			ourInstanceCohort = new RuleCohortEvaluatorInstance();
		return ourInstanceCohort;
	}

	public static synchronized void removeInstance() {
		ourInstanceCohort = null;
	}

	public void evaluate(final Cohort cohort) {
		if (!isRunning()) {
			ruleCohortEvaluator = new RuleCohortEvaluator(cohort);
			new Thread(ruleCohortEvaluator).start();
		}
	}

	public String getCurrentPatient() {
		if (isRunning())
			return String.valueOf(ruleCohortEvaluator.getCurrentPatientId());
		return StringUtils.EMPTY;
	}

	public String getProcessed() {
		if (isRunning())
			return String.valueOf(ruleCohortEvaluator.getCounter());
		return StringUtils.EMPTY;
	}

	public String getSize() {
		if (isRunning())
			return String.valueOf(ruleCohortEvaluator.getCohortSize());
		return StringUtils.EMPTY;
	}

	public String getCurrentStatus() {
		if (isRunning())
			return ruleCohortEvaluator.getEvaluatorStatus().getValue();
		return StringUtils.EMPTY;
	}

	public Boolean isRunning() {
		if (ruleCohortEvaluator == null)
			return Boolean.FALSE;

		if (OpenmrsUtil.nullSafeEquals(EvaluatorStatus.EVALUATOR_RUNNING, ruleCohortEvaluator.getEvaluatorStatus()))
			return Boolean.TRUE;

		return Boolean.FALSE;
	}
}
