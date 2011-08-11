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

package org.openmrs.module.clinicalsummary.rule.reminder.peds.nutrition;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
public class NutritionistReferralReminderRule extends MalnutritionReminderRule {

	private static final Log log = LogFactory.getLog(NutritionistReferralReminderRule.class);

	public static final String TOKEN = "Nutritionist Referral Reminder";

	/**
	 * Method that will determine whether the z-score value should throw reminder or not
	 *
	 * @param zScore
	 * 		the z-score value
	 *
	 * @return true when the z-score will are within the reminded values
	 */
	@Override
	protected Boolean acceptedScore(final Double zScore) {
		return (zScore != null && zScore > -3 && zScore < 1.5);
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
