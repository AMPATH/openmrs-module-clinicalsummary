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
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.cache.CacheUtils;
import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableParameter;
import org.openmrs.module.clinicalsummary.rule.EvaluableRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.util.RuleUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class DatetimeBasedConceptFilteredRule extends EvaluableRule {

	private static final Log log = LogFactory.getLog(DatetimeBasedConceptFilteredRule.class);

	public static final String TOKEN = "Datetime Based Concept Filtered";

	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
	 */
	@Override
	protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) {
		Result result = new Result();

		Object conceptObjects = parameters.get(EvaluableConstants.OBS_CONCEPT);
		Map<Concept, Integer> conceptNamePositions = searchPositions(conceptObjects);

		ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();
		Result obsResults = obsWithRestrictionRule.eval(context, patientId, parameters);

		Map<Date, Result[]> obsResultDates = new HashMap<Date, Result[]>();
		for (Result obsResult : obsResults) {
			Obs obs = (Obs) obsResult.getResultObject();
			Date obsDatetime = obs.getObsDatetime();
			// see if we already have obs array for this date
			Result[] obsResultDate = obsResultDates.get(obsDatetime);
			if (obsResultDate == null) {
				obsResultDate = new Result[CollectionUtils.size(conceptNamePositions)];
				obsResultDates.put(obsDatetime, obsResultDate);
			}
			// search the concept in the concept ordering map
			Integer position = conceptNamePositions.get(obs.getConcept());
			if (position != null)
				obsResultDate[position] = obsResult;
		}

		TreeSet<Date> keys = new TreeSet<Date>(new Comparator<Date>() {

			public int compare(final Date firstDate, final Date secondDate) {
				return firstDate.equals(secondDate) ? 0 : firstDate.after(secondDate) ? -1 : 1;
			}
		});
		keys.addAll(obsResultDates.keySet());

		// TODO: need to merge the two loop into one
		Integer counter = 0;
		Iterator<Date> iterator = keys.iterator();
		while (iterator.hasNext() && counter < 5) {
			Date date = iterator.next();
			// create the grouped results
			Result groupedResult = new Result();
			groupedResult.add(new Result(date));
			groupedResult.addAll(Arrays.asList(obsResultDates.get(date)));
			// add them to the main result of the rule
			result.add(groupedResult);
			// increase the counter as we only want last 5
			counter++;
		}

		Collections.reverse(result);

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
		return evaluableParameters;
	}
}
