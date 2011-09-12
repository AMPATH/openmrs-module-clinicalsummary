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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.rule.common.CohortBuilderRule;
import org.openmrs.module.clinicalsummary.service.EvaluatorService;
import org.openmrs.module.clinicalsummary.web.controller.WebUtils;
import org.openmrs.module.clinicalsummary.web.controller.evaluator.EvaluatorStatus;

/**
 */
class RuleCohortEvaluator implements Runnable {

	private static final Log log = LogFactory.getLog(RuleCohortEvaluator.class);

	private static final String SEPARATOR_STRING = "\t";

	private final Cohort cohort;

	private final UserContext userContext;

	private EvaluatorStatus evaluatorStatus;

	private Integer currentPatientId;

	private Integer counter;

	/**
	 * @param cohort
	 */
	public RuleCohortEvaluator(final Cohort cohort) {
		this.counter = 0;
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
	public Integer getCounter() {
		return counter;
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

			EvaluatorService evaluatorService = Context.getService(EvaluatorService.class);

			File attachmentFile = new File(System.getProperty("java.io.tmpdir"), WebUtils.prepareFilename(null, null));
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(attachmentFile));

			for (Integer patientId : cohort.getMemberIds()) {

				setCurrentPatientId(patientId);

				Map<String, Object> parameters = new HashMap<String, Object>();
				Patient patient = Context.getPatientService().getPatient(patientId);
				Result results = evaluatorService.evaluate(patient, CohortBuilderRule.TOKEN, parameters);

				if (CollectionUtils.isNotEmpty(results)) {
					Result result = results.latest();
					bufferedWriter.write(format(patient));
					bufferedWriter.write(format(result));
					bufferedWriter.newLine();
				}

				counter++;
			}

			bufferedWriter.close();
		} catch (Exception e) {
			log.error("Generating summary sheet for cohort failed ...", e);
		} finally {
			setEvaluatorStatus(EvaluatorStatus.EVALUATOR_IDLE);
			Context.closeSession();
		}
	}

	private String format(Patient patient) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(patient.getPersonName().getFullName()).append(SEPARATOR_STRING);
		buffer.append(patient.getPatientIdentifier().getIdentifier()).append(SEPARATOR_STRING);
		return buffer.toString();
	}

	private String format(Result result) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(Context.getDateFormat().format(result.getResultDate())).append(SEPARATOR_STRING);
		buffer.append(result.toString()).append(SEPARATOR_STRING);
		buffer.append(result.toNumber()).append(SEPARATOR_STRING);
		buffer.append(Context.getDateFormat().format(result.toDatetime())).append(SEPARATOR_STRING);
		buffer.append(result.toConcept().getName(Context.getLocale()).getName()).append(SEPARATOR_STRING);

		Concept concept = (Concept) result.getResultObject();
		buffer.append(concept.getName(Context.getLocale()).getName()).append(SEPARATOR_STRING);
		return buffer.toString();
	}
}
