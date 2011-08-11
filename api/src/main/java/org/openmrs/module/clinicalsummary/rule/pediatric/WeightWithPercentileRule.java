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

package org.openmrs.module.clinicalsummary.rule.pediatric;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableParameter;
import org.openmrs.module.clinicalsummary.rule.EvaluableRule;
import org.openmrs.module.clinicalsummary.rule.flowsheet.ObsFlowsheetRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.util.ZScoreUtils;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Parameters: <ul> <li>[Required] concept: the flow sheet that will be displayed</li> </ul>
 */
public class WeightWithPercentileRule extends EvaluableRule {

	private static final Log log = LogFactory.getLog(WeightWithPercentileRule.class);

	public static final String TOKEN = "Weight With Percentile";

	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
	 */
	@Override
	protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) throws LogicException {
		ObsFlowsheetRule obsFlowsheetRule = new ObsFlowsheetRule();
		Result numericResults = obsFlowsheetRule.eval(context, patientId, parameters);

		Patient patient = Context.getPatientService().getPatient(patientId);

		for (Result numericResult : numericResults) {

			String valueText = StringUtils.EMPTY;
			DecimalFormat twoDecimalFormat = new DecimalFormat("#.##");
			Double zScore = ZScoreUtils.calculateZScore(patient, numericResult.getResultDate(), numericResult.toNumber());
			if (zScore != null)
				valueText = "z=" + twoDecimalFormat.format(zScore) + " / p=" + ZScoreUtils.searchZScore(zScore) + "%";
			numericResult.setValueText(valueText);
			// set the data type to null to enable us pulling the text value and use them as the status of the flow sheet element
			numericResult.setDatatype(null);
		}

		return numericResults;
	}

	/**
	 * @see org.openmrs.logic.Rule#getDependencies()
	 */
	@Override
	public String[] getDependencies() {
		return new String[]{ObsWithStringRestrictionRule.TOKEN};
	}

	/**
	 * Get the definition of each parameter that should be passed to this rule execution
	 *
	 * @return all parameter that applicable for each rule execution
	 */
	@Override
	public Set<EvaluableParameter> getEvaluationParameters() {
		Set<EvaluableParameter> evaluableParameters = new HashSet<EvaluableParameter>();
		evaluableParameters.add(EvaluableConstants.REQUIRED_OBS_CONCEPT_PARAMETER_DEFINITION);
		return evaluableParameters;
	}

	/**
	 * Get the token name of the rule that can be used to reference the rule from LogicService
	 *
	 * @return the token name
	 */
	@Override
	protected String getEvaluableToken() {
		return TOKEN;
	}

}
