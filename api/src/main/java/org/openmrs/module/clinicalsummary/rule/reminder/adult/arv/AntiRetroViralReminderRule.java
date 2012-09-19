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

package org.openmrs.module.clinicalsummary.rule.reminder.adult.arv;

import java.util.Arrays;
import java.util.Map;

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
import org.openmrs.module.clinicalsummary.rule.reminder.adult.BaselineReminderRule;

/**
 * Parameters: <ul> <li>concept: the concept for the result (eg. CD4 COUNT)</li> <li>valueCoded: the values for the test
 * ordered</li> <li>encounterType: limit the anti retro viral digging to certain encounter types only</li> </ul>
 * <p/>
 * The reminder will check the baseline reminders. Only process the anti retro viral when the baseline is not triggered.
 */
public class AntiRetroViralReminderRule extends EvaluableRule {

	private static final Log log = LogFactory.getLog(AntiRetroViralReminderRule.class);

	private static final Integer MINIMUM_RESULT_VALUE = 350;

	public static final String TOKEN = "Anti Retro Viral Reminder";

	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
	 */
	@Override
	protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) throws LogicException {
		Result result = new Result();
		BaselineReminderRule baselineReminderRule = new BaselineReminderRule();
        parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.CD4_COUNT));
        parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList(EvaluableNameConstants.CD4_PANEL));
		Result baselineReminderResults = baselineReminderRule.eval(context, patientId, parameters);

		if (CollectionUtils.isNotEmpty(baselineReminderResults)) {
			ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();
			// remove the coded values because we need to pull the values of the results
			parameters.remove(EvaluableConstants.OBS_VALUE_CODED);
			parameters.put(EvaluableConstants.OBS_FETCH_SIZE, 1);
			Result resultResults = obsWithRestrictionRule.eval(context, patientId, parameters);

			// get the anti retro viral medications
			AntiRetroViralRule antiRetroViralRule = new AntiRetroViralRule();
			parameters.put(EvaluableConstants.ENCOUNTER_TYPE, Arrays.asList(EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_INITIAL,
					EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_RETURN, EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_NONCLINICALMEDICATION));
			Result arvResults = antiRetroViralRule.eval(context, patientId, parameters);
			if (CollectionUtils.isNotEmpty(resultResults) && CollectionUtils.isEmpty(arvResults)) {
				if (resultResults.toNumber() < MINIMUM_RESULT_VALUE)
					result.add(new Result(String.valueOf(parameters.get(ReminderParameters.DISPLAYED_REMINDER_TEXT))));
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
