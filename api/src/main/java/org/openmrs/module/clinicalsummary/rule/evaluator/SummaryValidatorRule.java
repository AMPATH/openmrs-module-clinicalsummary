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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.Mapping;
import org.openmrs.module.clinicalsummary.Summary;
import org.openmrs.module.clinicalsummary.enumeration.MappingType;
import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableRule;
import org.openmrs.module.clinicalsummary.rule.encounter.EncounterWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.encounter.EncounterWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.service.SummaryService;

/**
 */
public class SummaryValidatorRule extends EvaluableRule {

	private static final Log log = LogFactory.getLog(SummaryValidatorRule.class);

	public static final String TOKEN = "Summary Validator";

	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
	 */
	@Override
	protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) throws LogicException {
		Result result = new Result();

		Set<Summary> summarySet = new HashSet<Summary>();

		EncounterWithRestrictionRule encounterWithRestrictionRule = new EncounterWithStringRestrictionRule();

		SummaryService summaryService = Context.getService(SummaryService.class);
		List<Mapping> mappings = summaryService.getMappings(null, null, MappingType.LATEST_ENCOUNTER);

		Map<String, Summary> encounterTypeSummaryMap = new HashMap<String, Summary>();
		for (Mapping mapping : mappings) {
			EncounterType encounterType = mapping.getEncounterType();
			encounterTypeSummaryMap.put(encounterType.getName(), mapping.getSummary());
		}

		parameters.clear();
		parameters.put(EvaluableConstants.ENCOUNTER_FETCH_SIZE, 1);
		parameters.put(EvaluableConstants.ENCOUNTER_TYPE, new ArrayList<String>(encounterTypeSummaryMap.keySet()));
		Result encounterResults = encounterWithRestrictionRule.eval(context, patientId, parameters);

		for (Result encounterResult : encounterResults) {
			Encounter latestEncounter = (Encounter) encounterResult.getResultObject();
			EncounterType encounterType = latestEncounter.getEncounterType();
			Summary summary = encounterTypeSummaryMap.get(encounterType.getName());
			if (summary != null)
				summarySet.add(summary);
		}

		// clear up artifact from previous search
		encounterTypeSummaryMap.clear();
		parameters.clear();

		mappings = summaryService.getMappings(null, null, MappingType.ANY_ENCOUNTER);
		for (Mapping mapping : mappings) {
			EncounterType encounterType = mapping.getEncounterType();
			encounterTypeSummaryMap.put(encounterType.getName(), mapping.getSummary());
		}

		parameters.put(EvaluableConstants.ENCOUNTER_TYPE, new ArrayList<String>(encounterTypeSummaryMap.keySet()));
		encounterResults = encounterWithRestrictionRule.eval(context, patientId, parameters);

		for (Result encounterResult : encounterResults) {
			Encounter latestEncounter = (Encounter) encounterResult.getResultObject();
			EncounterType encounterType = latestEncounter.getEncounterType();
			Summary summary = encounterTypeSummaryMap.get(encounterType.getName());
			if (summary != null)
				summarySet.add(summary);
		}

		for (Summary summary : summarySet)
			result.add(new Result(new Date(), String.valueOf(summary.toString()), summary));

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
	 * Get the token name of the rule that can be used to reference the rule from LogicService
	 *
	 * @return the token name
	 */
	@Override
	protected String getEvaluableToken() {
		return TOKEN;
	}
}
