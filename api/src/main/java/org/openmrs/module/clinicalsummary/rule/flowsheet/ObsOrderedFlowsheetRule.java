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

package org.openmrs.module.clinicalsummary.rule.flowsheet;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Obs;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableNameConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableParameter;
import org.openmrs.module.clinicalsummary.rule.EvaluableRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.util.obs.Status;

/**
 * Parameters: <ul> <li>[Required] concept: the result of the tests</li> <li>[Required] valueCoded: the answers for the tests
 * ordered</li> </ul>
 */
public class ObsOrderedFlowsheetRule extends EvaluableRule {

	private static final Log log = LogFactory.getLog(ObsOrderedFlowsheetRule.class);

	public static final String TOKEN = "Obs Ordered Flowsheet";

	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
	 */
	@Override
	protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) throws LogicException {
		Result result = new Result();
		ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();

		// remove the value coded first
		Object codedValueObjects = parameters.remove(EvaluableConstants.OBS_VALUE_CODED);
		// get the results
		Result resultResults = FlowsheetUtils.slice(obsWithRestrictionRule.eval(context, patientId, parameters));
		// get the test ordered
		parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.TESTS_ORDERED));
		parameters.put(EvaluableConstants.OBS_VALUE_CODED, codedValueObjects);
		Result testResults = FlowsheetUtils.slice(obsWithRestrictionRule.eval(context, patientId, parameters));

		Integer testCounter = 0;
		Integer resultCounter = 0;
		while (resultCounter < resultResults.size() && testCounter < testResults.size()) {
			Date testDate = testResults.get(testCounter).getResultDate();
			Date resultDate = resultResults.get(resultCounter).getResultDate();
			// set the data type to null to enable us pulling the text value and use them as the status of the flow sheet element
			// TODO: need to take the value_text "<40" into consideration
			Result flowsheetResult;
			if (testDate.after(resultDate)) {
				Result currentResult = testResults.get(testCounter++);
				// create a copy of the same result as the order result above
				flowsheetResult = duplicateResult(currentResult);
				flowsheetResult.setValueCoded(null);
				// prepare the status of the result
				StringBuilder statusBuilder = new StringBuilder();
				if (StringUtils.isNotEmpty(flowsheetResult.toString()))
					statusBuilder.append(flowsheetResult.toString()).append(" ");
				statusBuilder.append(Status.STATUS_NO_RESULT.getValue());
				// create a copy of the same result as the test result above
				flowsheetResult.setValueText(statusBuilder.toString());
			} else {
				Result currentResult = resultResults.get(resultCounter++);
				// create a copy of the same result as the order result above
				flowsheetResult = duplicateResult(currentResult);
				if (resultDate.after(DateUtils.addDays(testDate, 1))) {
					// status of the order is no-order
					StringBuilder statusBuilder = new StringBuilder();
					if (StringUtils.isNotEmpty(flowsheetResult.toString()))
						statusBuilder.append(flowsheetResult.toString()).append(" ");
					statusBuilder.append(Status.STATUS_NO_ORDER.getValue());
					flowsheetResult.setValueText(statusBuilder.toString());
				} else
					testCounter++;
			}
			result.add(flowsheetResult);
		}

		while (resultCounter < resultResults.size()) {
			Result currentResult = resultResults.get(resultCounter++);
			Result flowsheetResult = duplicateResult(currentResult);
			// prepare the status of the result
			StringBuilder statusBuilder = new StringBuilder();
			if (StringUtils.isNotEmpty(flowsheetResult.toString()))
				statusBuilder.append(flowsheetResult.toString()).append(" ");
			statusBuilder.append(Status.STATUS_NO_ORDER.getValue());
			flowsheetResult.setValueText(statusBuilder.toString());
			result.add(flowsheetResult);
		}

		while (testCounter < testResults.size()) {
			Result currentResult = testResults.get(testCounter++);
			// create a copy of the same result as the order result above
			Result flowsheetResult = duplicateResult(currentResult);
			flowsheetResult.setValueCoded(null);
			// prepare the status
			StringBuilder statusBuilder = new StringBuilder();
			if (StringUtils.isNotEmpty(flowsheetResult.toString()))
				statusBuilder.append(flowsheetResult.toString()).append(" ");
			statusBuilder.append(Status.STATUS_NO_RESULT.getValue());
			// create a copy of the same result as the test result above
			flowsheetResult.setValueText(statusBuilder.toString());
			result.add(flowsheetResult);
		}

		result = FlowsheetUtils.slice(result);
		Collections.reverse(result);
		return result;
	}

	private Result duplicateResult(final Result result) {
		Obs obs = (Obs) result.getResultObject();
		return new Result(obs.getObsDatetime(), null, obs.getValueAsBoolean(), obs.getValueCoded(),
				obs.getValueDatetime(), obs.getValueNumeric(), obs.getValueText(), obs);
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
		evaluableParameters.add(EvaluableConstants.REQUIRED_OBS_VALUE_CODED_PARAMETER_DEFINITION);
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
