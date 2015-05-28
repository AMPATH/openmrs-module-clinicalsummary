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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
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
import org.openmrs.module.clinicalsummary.rule.reminder.adult.BaselineReminderRule;

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

        parameters.put(EvaluableConstants.ENCOUNTER_TYPE,
                Arrays.asList(EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_INITIAL,
                        EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_RETURN));
        parameters.put(EvaluableConstants.ENCOUNTER_FETCH_ORDER, FetchOrdering.ORDER_DESCENDING);
        parameters.put(EvaluableConstants.ENCOUNTER_FETCH_SIZE, 1);
        EncounterWithRestrictionRule encounterWithRestrictionRule = new EncounterWithStringRestrictionRule();
        Result encounterResults = encounterWithRestrictionRule.eval(context, patientId, parameters);
        if (!encounterResults.isEmpty()) {
            Result encounterResult = encounterResults.get(0);
            Encounter encounter = (Encounter) encounterResult.getResultObject();

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.YEAR, -3);
            Date threeYearsAgo = calendar.getTime();

            if (!encounter.getEncounterDatetime().before(threeYearsAgo)) {
				parameters.remove(EvaluableConstants.ENCOUNTER_FETCH_ORDER);
				parameters.remove(EvaluableConstants.ENCOUNTER_FETCH_SIZE);

				ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();
				parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList("X-RAY, CHEST, RADIOLOGY FINDINGS"));
				Result cxrFindingsResults = obsWithRestrictionRule.eval(context, patientId, parameters);
				if (cxrFindingsResults.isEmpty()) {
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
