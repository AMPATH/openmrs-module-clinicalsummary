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

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Obs;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableParameter;
import org.openmrs.module.clinicalsummary.rule.EvaluableRule;
import org.openmrs.module.clinicalsummary.rule.encounter.EncounterWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;
import org.openmrs.util.OpenmrsUtil;

/**
 */
public class ObsMergedFlowsheetRule extends EvaluableRule {

	private static final Log log = LogFactory.getLog(ObsMergedFlowsheetRule.class);

	public static final String TOKEN = "Obs Merged Flowsheet";

	private static final String OBS_MERGED_CONCEPT = "obs.merged";

	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
	 */
	@Override
	protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) throws LogicException {
		Result result = new Result();
		ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();

		// get the results
		Result resultResults = FlowsheetUtils.slice(obsWithRestrictionRule.eval(context, patientId, parameters));
		// get the merged results
		parameters.put(EvaluableConstants.OBS_CONCEPT, parameters.get(OBS_MERGED_CONCEPT));
		Result mergedResults = FlowsheetUtils.slice(obsWithRestrictionRule.eval(context, patientId, parameters));

		Integer resultCounter = 0;
		Integer mergedCounter = 0;

		while (resultCounter < resultResults.size()) {
			Result currentResult = resultResults.get(resultCounter++);
			Result flowsheetResult = shallowCopyResult(currentResult);
			flowsheetResult.setResultObject(null);
			// get the result to be merged
			Date mergedDate = null;
			if (mergedCounter < mergedResults.size())
				mergedDate = mergedResults.get(mergedCounter).getResultDate();
			if (OpenmrsUtil.nullSafeEquals(mergedDate, flowsheetResult.getResultDate()))
				flowsheetResult.setResultObject(format(mergedResults.get(mergedCounter++)));
			result.add(flowsheetResult);
		}

		result = FlowsheetUtils.slice(result);
		Collections.reverse(result);
		return result;
	}

	private Result shallowCopyResult(final Result result) {
		Obs obs = (Obs) result.getResultObject();
		return new Result(obs.getObsDatetime(), null, obs.getValueAsBoolean(), obs.getValueCoded(),
				obs.getValueDatetime(), obs.getValueNumeric(), obs.getValueText(), obs);
	}

	/**
	 * Format the result based on the result data type
	 *
	 * @param result the result
	 * @return the String representation of the result object
	 */
	private String format(final Result result) {
		return FlowsheetUtils.format(result);
	}

	/**
	 * @see org.openmrs.logic.Rule#getDependencies()
	 */
	@Override
	public String[] getDependencies() {
		return new String[]{ObsWithStringRestrictionRule.TOKEN, EncounterWithStringRestrictionRule.TOKEN};
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
		evaluableParameters.add(new EvaluableParameter(OBS_MERGED_CONCEPT, List.class, Boolean.TRUE));
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
