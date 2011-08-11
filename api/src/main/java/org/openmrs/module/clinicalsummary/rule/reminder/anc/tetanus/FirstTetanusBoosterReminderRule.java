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

package org.openmrs.module.clinicalsummary.rule.reminder.anc.tetanus;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.logic.result.Result;

public class FirstTetanusBoosterReminderRule extends AbstractTetanusBoosterReminderRule {

	private static final Log log = LogFactory.getLog(FirstTetanusBoosterReminderRule.class);

	private static final String TOKEN = AbstractTetanusBoosterReminderRule.FIRST_TETANUS_BOOSTER_REMINDER;

	@Override
	protected Result processCachedReminder(final String displayedReminderText) {
		Result result = new Result();
		TetanusBoosterReminderCacheInstance cacheInstance = TetanusBoosterReminderCacheInstance.getInstance();
		if (BooleanUtils.isTrue(cacheInstance.getReminder(TOKEN)))
			result.add(new Result(displayedReminderText));
		return result;
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
