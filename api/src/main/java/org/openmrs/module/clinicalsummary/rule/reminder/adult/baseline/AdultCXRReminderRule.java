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

package org.openmrs.module.clinicalsummary.rule.reminder.adult.baseline;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.enumeration.FetchOrdering;
import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableNameConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableRule;
import org.openmrs.module.clinicalsummary.rule.encounter.EncounterWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.encounter.EncounterWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.reminder.ReminderParameters;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class AdultCXRReminderRule extends EvaluableRule {

	private static final Log log = LogFactory.getLog(AdultCXRReminderRule.class);

	public static final String TOKEN = "Adult CXR Reminder";

	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
	 */
	@Override
	protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) throws LogicException {
		Result result = new Result();

		parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList("DATE HIV CARE PROGRAM ENROLLED"));
		parameters.remove(EvaluableConstants.OBS_VALUE_CODED);
		ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();
		Result dateEnrolledResults = obsWithRestrictionRule.eval(context, patientId, parameters);
		if (CollectionUtils.isNotEmpty(dateEnrolledResults)) {
			Result dateEnrolledResult = dateEnrolledResults.get(0);
			Obs dateEnrolledObs = (Obs) dateEnrolledResult.getResultObject();
			Date valueDatetime = dateEnrolledObs.getValueDatetime();
			if (valueDatetime != null) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(valueDatetime);
				calendar.add(Calendar.YEAR, 3);
				Date threeYearsLater = calendar.getTime();
				if (threeYearsLater.after(new Date())) {
					parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList("CXR", "X-RAY, CHEST, RADIOLOGY FINDINGS"));
					Result cxrFindingsResults = obsWithRestrictionRule.eval(context, patientId, parameters);
					if (cxrFindingsResults.isEmpty()) {
						parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList("HIV CARE PROGRAM ENROLLED"));
						Result programEnrolledResults = obsWithRestrictionRule.eval(context, patientId, parameters);
						if (CollectionUtils.isNotEmpty(programEnrolledResults)) {
							result.add(new Result(String.valueOf(parameters.get(ReminderParameters.DISPLAYED_REMINDER_TEXT))));
							return result;
						}

						parameters.put(EvaluableConstants.OBS_CONCEPT,
								Arrays.asList("HIV POSITIVE HIV CARE PROGRAM ENROLLMENT STATUS", "ENROLLED IN AMPATH"));
						parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList("YES"));
						Result enrolledResults = obsWithRestrictionRule.eval(context, patientId, parameters);
						if (CollectionUtils.isNotEmpty(enrolledResults)) {
							result.add(new Result(String.valueOf(parameters.get(ReminderParameters.DISPLAYED_REMINDER_TEXT))));
							return result;
						}
					}
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
