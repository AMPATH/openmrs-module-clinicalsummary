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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableNameConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;

public class EncounterBasedProblemReportedRule extends EvaluableRule {

	private static final Log log = LogFactory.getLog(EncounterBasedProblemReportedRule.class);

	public static final String TOKEN = "Encounter Based Problem Reported";

	/**
	 * @param context
	 * @param patientId
	 * @param parameters
	 * @return
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
	 */
	@Override
	protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) {
		Result result = new Result();

		Object encounters = parameters.get(EvaluableConstants.OBS_ENCOUNTER);
		if (encounters != null) {
			ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();
			// get the list of all problem resolved
			parameters.put(EvaluableConstants.OBS_ENCOUNTER, encounters);
			parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.PATIENT_REPORTED_PROBLEM_ADDED));
			Result problemResults = obsWithRestrictionRule.eval(context, patientId, parameters);

			// create a map of problems added to the system
			Integer counter = 0;
			Map<Concept, Result> addedMap = new HashMap<Concept, Result>();
			while (counter < problemResults.size()) {
				Result problemResult = problemResults.get(counter++);
				addedMap.put(problemResult.toConcept(), problemResult);
			}

			// format it to list of list of problem added
			for (Concept addedMapConcept : addedMap.keySet())
				result.add(addedMap.get(addedMapConcept));
		}

		return result;
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
}
