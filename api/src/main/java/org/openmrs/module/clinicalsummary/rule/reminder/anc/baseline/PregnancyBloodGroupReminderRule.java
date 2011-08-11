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

package org.openmrs.module.clinicalsummary.rule.reminder.anc.baseline;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableNameConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.reminder.ReminderParameters;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;


public class PregnancyBloodGroupReminderRule extends EvaluableRule {

	private static final Log log = LogFactory.getLog(PregnancyBloodGroupReminderRule.class);

	public static final String TOKEN = "Pregnancy Blood Group Reminder";

	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
	 */
	@Override
	protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) throws LogicException {

		Result result = new Result();

		ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();
		parameters.put(EvaluableConstants.OBS_FETCH_SIZE, 1);
		parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.BLOOD_TYPING));
		Result bloodGroupResults = obsWithRestrictionRule.eval(context, patientId, parameters);
		if (CollectionUtils.isEmpty(bloodGroupResults)) {

			parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.TESTS_ORDERED));
			parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList(EvaluableNameConstants.BLOOD_TYPING));

			Result testResults = obsWithRestrictionRule.eval(context, patientId, parameters);

			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.MONTH, -1);
			Date lastMonth = calendar.getTime();

			Boolean displayReminder = Boolean.TRUE;
			// if no test or the test was ordered more than one months ago, then show the reminder
			if (CollectionUtils.isEmpty(testResults) || testResults.getResultDate().before(lastMonth))
				result.add(new Result(String.valueOf(parameters.get(ReminderParameters.DISPLAYED_REMINDER_TEXT))));
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
