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

package org.openmrs.module.clinicalsummary.rule.primary;

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
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.util.RuleUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 */
public class ConceptSetBasedObsRule extends EvaluableRule {

	private static final Log log = LogFactory.getLog(ConceptSetBasedObsRule.class);

	public static final String TOKEN = "Concept Set Based Obs";

	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) throws LogicException {

		Result result = new Result();

		Object conceptSetObjects = parameters.remove(PrimaryCareParameters.OBS_CONCEPT_SET_MEMBERS);
		Map<Concept, Integer> conceptSetPositions = searchPositions(conceptSetObjects);

		ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();
		Result parentObsResults = obsWithRestrictionRule.eval(context, patientId, parameters);

		for (Result parentObsResult : parentObsResults) {
			Obs parentObs = (Obs) parentObsResult.getResultObject();
			if (parentObs.isObsGrouping()) {
				Set<Obs> childObservations = parentObs.getGroupMembers();

				Result groupedResult = new Result();
				groupedResult.add(new Result(parentObsResult.getResultDate()));
				Result[] results = new Result[CollectionUtils.size(conceptSetPositions)];
				for (Obs childObservation : childObservations) {
					Integer position = conceptSetPositions.get(childObservation.getConcept());
					if (position != null)
						results[position] = new Result(childObservation);
				}
				groupedResult.addAll(Arrays.asList(results));
				result.add(groupedResult);
			}
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	private Map<Concept, Integer> searchPositions(Object conceptSetObjects) {
		Map<Concept, Integer> conceptSetPositions = new HashMap<Concept, Integer>();
		if (RuleUtils.isValidCollectionObject(conceptSetObjects)) {
			List<String> conceptSetNames = (List<String>) conceptSetObjects;
			for (int counter = 0; counter < CollectionUtils.size(conceptSetNames); counter++) {
				Concept concept = CacheUtils.getConcept(conceptSetNames.get(counter));
				conceptSetPositions.put(concept, counter);
			}
		}
		return conceptSetPositions;
	}

	/**
	 * @see org.openmrs.logic.Rule#getDependencies()
	 */
	@Override
	public String[] getDependencies() {
		return new String[]{ObsWithStringRestrictionRule.TOKEN};
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

	/**
	 * Get the definition of each parameter that should be passed to this rule execution
	 *
	 * @return all parameter that applicable for each rule execution
	 */
	@Override
	public Set<EvaluableParameter> getEvaluationParameters() {
		Set<EvaluableParameter> evaluableParameters = new HashSet<EvaluableParameter>();
		evaluableParameters.add(EvaluableConstants.REQUIRED_OBS_CONCEPT_PARAMETER_DEFINITION);
		evaluableParameters.add(new EvaluableParameter(PrimaryCareParameters.OBS_CONCEPT_SET_MEMBERS, List.class, Boolean.TRUE));
		return evaluableParameters;
	}
}
