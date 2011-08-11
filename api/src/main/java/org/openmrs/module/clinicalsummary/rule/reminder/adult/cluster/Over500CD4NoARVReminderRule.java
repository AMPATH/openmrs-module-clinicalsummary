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

public class Over500CD4NoARVReminderRule extends EvaluableRule {

	private static final Log log = LogFactory.getLog(Over500CD4NoARVReminderRule.class);

	public static final String TOKEN = "Over 500 CD4 No Anti Retro Viral Reminder";

	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
	 */
	@Override
	protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) throws LogicException {

		Result result = new Result();

		ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();
		parameters.put(EvaluableConstants.OBS_FETCH_SIZE, 1);
		parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.CD4_COUNT));
		Result obsClusterResults = obsWithRestrictionRule.eval(context, patientId, parameters);
		if (CollectionUtils.isNotEmpty(obsClusterResults)) {
			Result latestClusterResult = obsClusterResults.latest();

			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.MONTH, -12);
			if (latestClusterResult.toNumber() > 500 && latestClusterResult.getResultDate().before(calendar.getTime())) {

				// get the latest test ordered
				parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.TESTS_ORDERED));
				parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList(EvaluableNameConstants.CD4_PANEL));

				// minus 12 and then add 6 equals to minus 6 hahaha ...
				calendar.add(Calendar.MONTH, 6);
				Date sixMonths = calendar.getTime();

				Result testResults = obsWithRestrictionRule.eval(context, patientId, parameters);

				if (CollectionUtils.isEmpty(testResults) || testResults.getResultDate().before(sixMonths)) {
					AntiRetroViralRule antiRetroViralRule = new AntiRetroViralRule();
					// prepare the encounter types
					parameters.put(EvaluableConstants.ENCOUNTER_TYPE, Arrays.asList(EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_INITIAL,
							EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_RETURN, EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_NONCLINICALMEDICATION));
					Result arvResults = antiRetroViralRule.eval(context, patientId, parameters);
					if (CollectionUtils.isEmpty(arvResults))
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
