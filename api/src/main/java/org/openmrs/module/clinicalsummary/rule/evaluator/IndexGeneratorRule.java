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

package org.openmrs.module.clinicalsummary.rule.evaluator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableParameter;
import org.openmrs.module.clinicalsummary.rule.EvaluableRule;
import org.openmrs.module.clinicalsummary.rule.encounter.EncounterWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.encounter.EncounterWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 */
public class IndexGeneratorRule extends EvaluableRule {

	public static final String TOKEN = "Index Generator";

	private static final String RETURN_VISIT_DATE = "RETURN VISIT DATE";

	private static final Log log = LogFactory.getLog(IndexGeneratorRule.class);

	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
	 */
	@Override
	protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) throws LogicException {

		Result result = new Result();

		EncounterWithRestrictionRule encounterWithRestrictionRule = new EncounterWithStringRestrictionRule();
		parameters.put(EvaluableConstants.ENCOUNTER_FETCH_SIZE, 1);
		Result encounterResults = encounterWithRestrictionRule.eval(context, patientId, parameters);

		Result indexResult = null;
		for (Result encounterResult : encounterResults) {
			indexResult = new Result();

			Encounter encounter = (Encounter) encounterResult.getResultObject();
			indexResult.setResultDate(encounter.getEncounterDatetime());
			indexResult.setResultObject(encounter.getLocation());

			ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();

			parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(RETURN_VISIT_DATE));
			parameters.put(EvaluableConstants.OBS_ENCOUNTER, Arrays.asList(encounter));
			parameters.put(EvaluableConstants.OBS_FETCH_SIZE, 1);
			Result obsResults = obsWithRestrictionRule.eval(context, patientId, parameters);

			if (CollectionUtils.isNotEmpty(obsResults))
				indexResult.setValueDatetime(obsResults.toDatetime());
		}

		if (indexResult != null)
			result.add(indexResult);

		return result;
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
		evaluableParameters.add(EvaluableConstants.REQUIRED_ENCOUNTER_TYPE_PARAMETER_DEFINITION);
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
