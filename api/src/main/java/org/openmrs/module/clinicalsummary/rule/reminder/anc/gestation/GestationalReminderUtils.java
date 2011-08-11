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

package org.openmrs.module.clinicalsummary.rule.reminder.anc.gestation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.impl.LogicContextImpl;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableNameConstants;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.util.ZScoreUtils;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GestationalReminderUtils {

	private static final Log log = LogFactory.getLog(GestationalReminderUtils.class);

	private static final Integer ONE_WEEK = ZScoreUtils.ONE_WEEK;

	/**
	 * Calculate patients gestational age based on the pregnancy start date passed to this method.
	 *
	 *
	 * @param patientId
	 * @param pregnancyDate
	 * @return
	 */
	public static Long calculateGestationalAge(Integer patientId, Date pregnancyDate) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(EvaluableConstants.OBS_FETCH_SIZE, 1);
		parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.NUMBER_OF_WEEKS_PREGNANT));

		LogicContext context = new LogicContextImpl(patientId);
		ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();
		Result gestationalResults = obsWithRestrictionRule.eval(context, patientId, parameters);
		// if we can get any gestational week observation, then use that value
		if (CollectionUtils.isNotEmpty(gestationalResults)) {
			log.info("Gestational based on obs with value " + gestationalResults.toNumber() + " week(s)");
			return Math.round(gestationalResults.latest().toNumber());
		}

		// if we can't find the obs, then calculate the gestational age based on the estimated start of pregnancy date
		double gestationalAge = 0;
		if (pregnancyDate != null) {
			long timeDifference = (Calendar.getInstance().getTimeInMillis() - pregnancyDate.getTime()) / 1000;
			gestationalAge = (double) timeDifference / GestationalReminderUtils.ONE_WEEK;
			log.info("Gestational based on pregnancy date with value " + gestationalResults.toNumber() + " week(s)");
		}
		return Math.round(gestationalAge);
	}
}
