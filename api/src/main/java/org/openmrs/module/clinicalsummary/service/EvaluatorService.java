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
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.logic.LogicCriteria;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.Summary;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 */
@Transactional
public interface EvaluatorService extends OpenmrsService {

	/**
	 * Evaluate a summary template and generate the summary sheet file
	 *
	 * @param summary      the summary sheet template definition
	 * @param patient      the patient
	 * @param keepArtifact
	 * @throws APIException
	 * @should evaluate the template on a patient
	 */
	@Transactional(readOnly = true)
	void evaluate(final Patient patient, final Summary summary, final Boolean keepArtifact) throws APIException;

	/**
	 * Evaluates a rule for a given patient, given a token and parameters for the rule.
	 *
	 * @param patient    patient for whom the rule is to be calculated
	 * @param token      token to be evaluated
	 * @param parameters parameters to be passed to the rule
	 * @return patient-specific result from given rule
	 * @throws org.openmrs.logic.LogicException
	 *
	 * @see {@link org.openmrs.logic.LogicService#parse(String)}
	 */
	@Transactional(readOnly = true)
	Result evaluate(final Patient patient, final String token, final Map<String, Object> parameters) throws APIException;

	/**
	 * Parse a criteria String to create a new LogicCriteria. <br /> <br /> Example: <br /> <code>logicService.parseString("LAST 'CD4 COUNT' <
	 * 200");</code>
	 *
	 * @param expression LogicCriteria expression in a plain String object.
	 * @return LogicCriteria using all possible operand and operator from the String input
	 */
	@Transactional(readOnly = true)
	LogicCriteria parseExpression(final String expression) throws APIException;
}
