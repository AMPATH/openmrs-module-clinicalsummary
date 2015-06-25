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

package org.openmrs.module.clinicalsummary.rule.reminder.adult.pregnancy;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.result.Result;
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

/**
 */
public class SecondLineViralLoadReminderRule extends EvaluableRule {

    public static final String TOKEN = "Adult:Second Line Viral Load Reminder";

    @Override
    protected Result evaluate(LogicContext context, Integer patientId, Map<String, Object> parameters) {
        Result result = new Result();

        ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();

        parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList("HIV VIRAL LOAD, QUANTITATIVE"));
        parameters.put(EvaluableConstants.OBS_FETCH_SIZE, 2);
        Result viralLoadResults = obsWithRestrictionRule.eval(context, patientId, parameters);

        if (CollectionUtils.isNotEmpty(viralLoadResults) && CollectionUtils.size(viralLoadResults) == 2) {

            Result latestViralLoadResult = viralLoadResults.get(0);
            Result beforeLatestViralLoadResult = viralLoadResults.get(1);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(latestViralLoadResult.getResultDate());
            calendar.add(Calendar.MONTH, -3);
            Date threeMonthsAgo = calendar.getTime();

            if ((beforeLatestViralLoadResult.getResultDate().before(threeMonthsAgo)
                    || DateUtils.isSameDay(beforeLatestViralLoadResult.getResultDate(), threeMonthsAgo))
                    && latestViralLoadResult.toNumber() > 1000 && beforeLatestViralLoadResult.toNumber() > 1000) {

                parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList("HIV ANTIRETROVIRAL DRUG PLAN TREATMENT CATEGORY"));
                parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList("SECOND LINE HIV ANTIRETROVIRAL DRUG TREATMENT"));
                Result antiretroviralPlanResults = obsWithRestrictionRule.eval(context, patientId, parameters);

                if (CollectionUtils.isNotEmpty(antiretroviralPlanResults)) {
                    result.add(new Result(String.valueOf(parameters.get(ReminderParameters.DISPLAYED_REMINDER_TEXT))));
                    return result;
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
