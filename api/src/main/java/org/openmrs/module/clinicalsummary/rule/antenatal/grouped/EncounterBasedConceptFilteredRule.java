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

package org.openmrs.module.clinicalsummary.rule.antenatal.grouped;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.cache.CacheUtils;
import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableParameter;
import org.openmrs.module.clinicalsummary.rule.EvaluableRule;
import org.openmrs.module.clinicalsummary.rule.antenatal.AncParameters;
import org.openmrs.module.clinicalsummary.rule.encounter.EncounterWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.encounter.EncounterWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.util.RuleUtils;
import org.openmrs.util.OpenmrsUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 */
public class EncounterBasedConceptFilteredRule extends EvaluableRule {

	private static final Log log = LogFactory.getLog(EncounterBasedConceptFilteredRule.class);

	public static final String TOKEN = "Encounter Based Concept Filtered";

	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
	 */
	@Override
	protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) throws LogicException {

		Result result = new Result();

		EncounterWithRestrictionRule encounterWithRestrictionRule = new EncounterWithStringRestrictionRule();
		Result encounterResults = encounterWithRestrictionRule.eval(context, patientId, parameters);
		// take out the encounter type for the reference encounter
		Object encounterType = parameters.remove(AncParameters.AFTER_ENCOUNTER_TYPE);
		// prepare the parameters
		parameters.put(EvaluableConstants.ENCOUNTER_FETCH_SIZE, 1);
		parameters.put(EvaluableConstants.ENCOUNTER_TYPE, Arrays.asList(encounterType));
		Result afterEncounterResults = encounterWithRestrictionRule.eval(context, patientId, parameters);
		// see if we can get the reference encounter
		Object afterEncounter = null;
		if (CollectionUtils.isNotEmpty(afterEncounterResults))
			afterEncounter = afterEncounterResults.latest().getResultObject();
		// search the ordering of the observations
		Object conceptObjects = parameters.get(EvaluableConstants.OBS_CONCEPT);
		Map<Concept, Integer> conceptNamePositions = searchPositions(conceptObjects);

		ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();

		int counter = 0;
		Boolean foundAfter = Boolean.FALSE;
		while (counter < CollectionUtils.size(encounterResults) && !foundAfter) {
			Result encounterResult = encounterResults.get(counter);

			parameters.put(EvaluableConstants.OBS_ENCOUNTER, Arrays.asList(encounterResult.getResultObject()));
			Result obsResults = obsWithRestrictionRule.eval(context, patientId, parameters);

			Result groupedResult = new Result();
			groupedResult.add(new Result(encounterResult.getResultDate()));
			// initialize results array
			Result[] results = new Result[CollectionUtils.size(conceptNamePositions)];
			for (Result obsResult : obsResults) {
				Obs obs = (Obs) obsResult.getResultObject();
				Integer position = conceptNamePositions.get(obs.getConcept());
				if (position != null)
					results[position] = obsResult;
			}
			groupedResult.addAll(Arrays.asList(results));
			result.add(groupedResult);

			if (OpenmrsUtil.nullSafeEquals(afterEncounter, encounterResult.getResultObject()))
				foundAfter = Boolean.TRUE;

			counter++;
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	private Map<Concept, Integer> searchPositions(Object conceptNameObjects) {
		Map<Concept, Integer> conceptNamePositions = new HashMap<Concept, Integer>();
		if (RuleUtils.isValidCollectionObject(conceptNameObjects)) {
			List<String> conceptNames = (List<String>) conceptNameObjects;
			for (int i = 0; i < CollectionUtils.size(conceptNames); i++) {
				Concept concept = CacheUtils.getConcept(conceptNames.get(i));
				conceptNamePositions.put(concept, i);
			}
		}
		return conceptNamePositions;
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
		evaluableParameters.add(EvaluableConstants.REQUIRED_OBS_CONCEPT_PARAMETER_DEFINITION);
		evaluableParameters.add(EvaluableConstants.REQUIRED_OBS_CONCEPT_PARAMETER_DEFINITION);
		evaluableParameters.add(new EvaluableParameter(AncParameters.AFTER_ENCOUNTER_TYPE, String.class, Boolean.TRUE));
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
