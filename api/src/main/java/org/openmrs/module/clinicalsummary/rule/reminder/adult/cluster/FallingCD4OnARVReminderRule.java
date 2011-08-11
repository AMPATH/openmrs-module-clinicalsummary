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

package org.openmrs.module.clinicalsummary.rule.reminder.adult.cluster;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableNameConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableRule;
import org.openmrs.module.clinicalsummary.rule.medication.AntiRetroViralRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.reminder.ReminderParameters;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class FallingCD4OnARVReminderRule extends EvaluableRule {

	private static final Log log = LogFactory.getLog(FallingCD4OnARVReminderRule.class);

	public static final String TOKEN = "Falling CD4 On Anti Retro Viral Reminder";

	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
	 */
	@Override
	protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) throws LogicException {
		Result result = new Result();

		ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();

		parameters.put(EvaluableConstants.OBS_FETCH_SIZE, 2);
		parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.CD4_COUNT));
		Result clusterResults = obsWithRestrictionRule.eval(context, patientId, parameters);
		// only process when we have at least two results
		if (CollectionUtils.size(clusterResults) >= 2) {
			Calendar calendar = Calendar.getInstance();
			// three months ago
			calendar.add(Calendar.MONTH, -3);
			Date threeMonthsAgo = calendar.getTime();
			// we already know there's at least two results, so we can use the magic number here to get last and second from last result
			Result currentClusterResult = clusterResults.get(0);
			Result previousClusterResult = clusterResults.get(1);
			Double percentage = (previousClusterResult.toNumber() - currentClusterResult.toNumber()) / previousClusterResult.toNumber();
			if (currentClusterResult.getResultDate().before(threeMonthsAgo) && percentage > 0.25) {

				AntiRetroViralRule antiRetroViralRule = new AntiRetroViralRule();
				// prepare the encounter types
				parameters.put(EvaluableConstants.ENCOUNTER_TYPE, Arrays.asList(EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_INITIAL,
						EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_RETURN, EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_NONCLINICALMEDICATION));
				Result arvResults = antiRetroViralRule.eval(context, patientId, parameters);
				if (CollectionUtils.isNotEmpty(arvResults)) {
					// we don't want to limit the search on the test
					parameters.remove(EvaluableConstants.OBS_FETCH_SIZE);
					// search for the test
					parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.TESTS_ORDERED));
					parameters.put(EvaluableConstants.OBS_VALUE_CODED,
							Arrays.asList(EvaluableNameConstants.CD4_PANEL, EvaluableNameConstants.HIV_DNA_POLYMERASE_CHAIN_REACTION_QUALITATIVE,
									EvaluableNameConstants.HIV_VIRAL_LOAD_QUANTITATIVE));
					Result testResults = obsWithRestrictionRule.eval(context, patientId, parameters);

					calendar.setTime(currentClusterResult.getResultDate());
					calendar.add(Calendar.MONTH, 3);
					Date threeMonthsAfterResult = calendar.getTime();

					Boolean testExists = Boolean.FALSE;
					Boolean beforeDate = Boolean.FALSE;

					Integer testCounter = 0;
					while (testCounter < testResults.size() && !testExists && !beforeDate) {
						Result testResult = testResults.get(testCounter++);
						// results are ordered by datetime
						// if current result already before 3 months, then all of them are before the cutoff point date
						if (testResult.getResultDate().before(threeMonthsAfterResult))
							beforeDate = Boolean.TRUE;
						else
							testExists = Boolean.TRUE;
					}

					if (!testExists)
						result.add(new Result(String.valueOf(parameters.get(ReminderParameters.DISPLAYED_REMINDER_TEXT))));
				}
			}
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
