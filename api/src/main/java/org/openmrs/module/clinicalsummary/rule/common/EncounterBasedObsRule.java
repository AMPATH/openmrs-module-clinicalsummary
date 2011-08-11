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

package org.openmrs.module.clinicalsummary.rule.common;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 */
public class EncounterBasedObsRule extends EvaluableRule {

	private static final Log log = LogFactory.getLog(EncounterBasedObsRule.class);

	public static final String TOKEN = "Encounter Based Obs";

	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
	 */
	@Override
	protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) throws LogicException {

		Result result = new Result();

		EncounterWithRestrictionRule encounterWithRestrictionRule = new EncounterWithStringRestrictionRule();
		Result encounterResults = encounterWithRestrictionRule.eval(context, patientId, parameters);

		if (CollectionUtils.isNotEmpty(encounterResults)) {

			ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();

			List<Object> encounters = new ArrayList<Object>();
			for (Result encounterResult : encounterResults)
				encounters.add(encounterResult.getResultObject());

			parameters.put(EvaluableConstants.OBS_ENCOUNTER, encounters);
			Result obsResults = obsWithRestrictionRule.eval(context, patientId, parameters);

			if (CollectionUtils.isNotEmpty(obsResults))
				result.addAll(obsResults);
		}

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
		evaluableParameters.add(EvaluableConstants.REQUIRED_OBS_CONCEPT_PARAMETER_DEFINITION);
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
