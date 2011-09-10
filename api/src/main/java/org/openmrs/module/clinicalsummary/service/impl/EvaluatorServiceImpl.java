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

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.logic.LogicCriteria;
import org.openmrs.logic.impl.LogicCriteriaImpl;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.Summary;
import org.openmrs.module.clinicalsummary.evaluator.Evaluator;
import org.openmrs.module.clinicalsummary.evaluator.velocity.VelocityEvaluator;
import org.openmrs.module.clinicalsummary.service.EvaluatorService;

/**
 */
public class EvaluatorServiceImpl extends BaseOpenmrsService implements EvaluatorService {

	private static final Log log = LogFactory.getLog(EvaluatorServiceImpl.class);

	/**
	 * @see EvaluatorService#evaluate(Patient, org.openmrs.module.clinicalsummary.Summary, Boolean)
	 */
	@Override
	public void evaluate(final Patient patient, final Summary summary, final Boolean keepArtifact) throws APIException {
		Evaluator evaluator = new VelocityEvaluator();
		evaluator.evaluate(summary, patient, keepArtifact);
	}

	/**
	 * @see EvaluatorService#evaluate(Patient, String, java.util.Map)
	 */
	@Override
	public Result evaluate(final Patient patient, final String token, final Map<String, Object> parameters) throws APIException {
		LogicCriteria criteria = new LogicCriteriaImpl(token);
		return Context.getLogicService().eval(patient.getPatientId(), criteria, parameters);
	}

	/**
	 * @see EvaluatorService#parseExpression(String)
	 */
	@Override
	public LogicCriteria parseExpression(final String expression) throws APIException {
		return Context.getLogicService().parse(expression);
	}
}
