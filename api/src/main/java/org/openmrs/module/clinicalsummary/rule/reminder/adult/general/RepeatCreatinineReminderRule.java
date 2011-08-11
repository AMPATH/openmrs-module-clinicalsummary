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

package org.openmrs.module.clinicalsummary.rule.reminder.adult.general;

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

public class RepeatCreatinineReminderRule extends EvaluableRule {

	private static final Log log = LogFactory.getLog(RepeatCreatinineReminderRule.class);

	public static final String TOKEN = "Repeat Creatinine Reminder";

	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
	 */
	@Override
	protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) throws LogicException {
		Result result = new Result();

		ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();
		parameters.put(EvaluableConstants.OBS_FETCH_SIZE, 2);
		parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.SERUM_CREATININE));
		Result creatinineResults = obsWithRestrictionRule.eval(context, patientId, parameters);
		if (CollectionUtils.size(creatinineResults) == 1) {
			// get the latest test ordered
			Result latestCreatinineResult = creatinineResults.latest();

			parameters.put(EvaluableConstants.OBS_FETCH_SIZE, 1);
			parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.TESTS_ORDERED));
			parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList( EvaluableNameConstants.CHEMISTRY_LAB_TESTS, EvaluableNameConstants.SERUM_CREATININE,
					EvaluableNameConstants.SERUM_ELECTROLYTES));

			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.MONTH, -6);
			Date sixMonths = calendar.getTime();

			Result testResults = obsWithRestrictionRule.eval(context, patientId, parameters);

			// if creatinine > 200 and no test or the test was ordered more than six months ago, then show the reminder
			if (latestCreatinineResult.toNumber() > 200 && (CollectionUtils.isEmpty(testResults) || testResults.getResultDate().before(sixMonths))) {
				// prepare the search for referrals
				parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.REFERRALS_ORDERED));
				parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList(EvaluableNameConstants.REFERRAL_CONSULTANT,
						EvaluableNameConstants.REFERRAL_MEDICAL_OFFICER));
				Result referralResults = obsWithRestrictionRule.eval(context, patientId, parameters);
				if (CollectionUtils.isEmpty(referralResults))
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
