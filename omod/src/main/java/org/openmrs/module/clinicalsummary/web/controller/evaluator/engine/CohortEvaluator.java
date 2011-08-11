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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.clinicalsummary.Summary;
import org.openmrs.module.clinicalsummary.evaluator.Evaluator;
import org.openmrs.module.clinicalsummary.evaluator.velocity.VelocityEvaluator;
import org.openmrs.module.clinicalsummary.rule.ResultCacheInstance;
import org.openmrs.module.clinicalsummary.service.IndexService;
import org.openmrs.module.clinicalsummary.service.SummaryService;
import org.openmrs.module.clinicalsummary.web.controller.evaluator.EvaluatorStatus;

/**
 */
class CohortEvaluator implements Runnable {

	private static final Log log = LogFactory.getLog(CohortEvaluator.class);

	private final Cohort cohort;

	private final UserContext userContext;

	private EvaluatorStatus evaluatorStatus;

	private Integer currentPatientId;

	private Integer processed;

	/**
	 * @param cohort
	 */
	public CohortEvaluator(final Cohort cohort) {
		this.processed = 0;
		this.cohort = cohort;
		this.userContext = Context.getUserContext();
		this.evaluatorStatus = EvaluatorStatus.EVALUATOR_IDLE;
	}

	/**
	 * @return
	 */
	public Integer getCurrentPatientId() {
		return currentPatientId;
	}

	/**
	 * @param currentPatientId
	 */
	public void setCurrentPatientId(final Integer currentPatientId) {
		this.currentPatientId = currentPatientId;
	}

	/**
	 * @return
	 */
	public EvaluatorStatus getEvaluatorStatus() {
		return evaluatorStatus;
	}

	/**
	 * @param evaluatorStatus
	 */
	public void setEvaluatorStatus(final EvaluatorStatus evaluatorStatus) {
		this.evaluatorStatus = evaluatorStatus;
	}

	/**
	 * @return
	 */
	public Integer getProcessed() {
		return processed;
	}

	/**
	 * @return
	 */
	public Integer getCohortSize() {
		return cohort.size();
	}

	/**
	 * When an object implementing interface <code>Runnable</code> is used to create a thread, starting the thread causes the object's <code>run</code>
	 * method to be called in that separately executing thread.
	 * <p/>
	 * The general contract of the method <code>run</code> is that it may take any action whatsoever.
	 *
	 * @see Thread#run()
	 */
	public void run() {
		try {
			Context.openSession();
			Context.setUserContext(userContext);

			setEvaluatorStatus(EvaluatorStatus.EVALUATOR_RUNNING);

			IndexService indexService = Context.getService(IndexService.class);
			Evaluator evaluator = new VelocityEvaluator();

			ResultCacheInstance cacheInstance = ResultCacheInstance.getInstance();

			for (Integer patientId : cohort.getMemberIds()) {
				setCurrentPatientId(patientId);
				Patient patient = Context.getPatientService().getPatient(patientId);
				if (patient != null) {
					SummaryService summaryService = Context.getService(SummaryService.class);
					for (Summary summary : summaryService.getSummaries(patient)) {
						double start = System.currentTimeMillis();

						evaluator.evaluate(summary, patient, Boolean.TRUE);
						indexService.saveIndex(indexService.generateIndex(patient, summary));

						double elapsed = System.currentTimeMillis() - start;
						log.info("Velocity evaluator running for " + elapsed + "ms (" + (elapsed / 1000) + "s)");
					}

					cacheInstance.clearCache(patient);
					cleanSession();
				}
				processed++;
			}
		} catch (Exception e) {
			log.error("Generating summary sheet for cohort failed ...", e);
		} finally {
			setEvaluatorStatus(EvaluatorStatus.EVALUATOR_IDLE);
			Context.closeSession();
		}
	}

	private void cleanSession() {
		if (processed % 20 == 0) {
			Context.flushSession();
			Context.clearSession();
		}
	}
}
