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
public class SecondViralLoadReminderRule extends EvaluableRule {

    public static final String TOKEN = "Adult:Second Viral Load Reminder";

    @Override
    protected Result evaluate(LogicContext context, Integer patientId, Map<String, Object> parameters) {
        Result result = new Result();

        EncounterWithRestrictionRule encounterWithRestrictionRule = new EncounterWithStringRestrictionRule();
        parameters.put(EvaluableConstants.ENCOUNTER_TYPE,
                Arrays.asList(EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_INITIAL,
                        EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_RETURN));
        parameters.put(EvaluableConstants.ENCOUNTER_FETCH_SIZE, 1);
        Result encounterResults = encounterWithRestrictionRule.eval(context, patientId, parameters);
        if (CollectionUtils.isNotEmpty(encounterResults)) {
            Result encounterResult = encounterResults.latest();

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, -12);
            Date twelveMonthsAgo = calendar.getTime();

            calendar.setTime(encounterResult.getResultDate());
            calendar.add(Calendar.MONTH, 12);
            Date twelveMonthsLater = calendar.getTime();

            ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();

            parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList("TEST RESULT"));
            parameters.put(EvaluableConstants.OBS_VALUE_CODED,
                    Arrays.asList("HIV VIRAL LOAD, QUALITATIVE", "HIV VIRAL LOAD, QUANTITATIVE"));
            Result viralLoadResults = obsWithRestrictionRule.eval(context, patientId, parameters);

            parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList("TESTS ORDERED",
                    "LAB TESTS ORDERED FOR NEXT VISIT"));
            parameters.put(EvaluableConstants.OBS_VALUE_CODED,
                    Arrays.asList("HIV VIRAL LOAD, QUALITATIVE", "HIV VIRAL LOAD, QUANTITATIVE"));
            Result viralLoadOrderedResults = obsWithRestrictionRule.eval(context, patientId, parameters);

            if (CollectionUtils.isNotEmpty(viralLoadResults) && viralLoadResults.getResultDate().after(encounterResult.getResultDate())
                    && (CollectionUtils.isEmpty(viralLoadOrderedResults) || viralLoadOrderedResults.latest().getResultDate().before(encounterResult.getResultDate()))) {

                parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList("ANTIRETROVIRAL PLAN"));
                parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList("START DRUGS", "DRUG RESTART"));
                Result antiretroviralPlanResults = obsWithRestrictionRule.eval(context, patientId, parameters);
                if (CollectionUtils.isNotEmpty(antiretroviralPlanResults)
                        && antiretroviralPlanResults.latest().getResultDate().after(twelveMonthsLater)) {
                    result.add(new Result(String.valueOf(parameters.get(ReminderParameters.DISPLAYED_REMINDER_TEXT))));
                    return result;
                }

                parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList("REASON ANTIRETROVIRALS STARTED"));
                parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList("NONE"));
                Result reasonStartedResults = obsWithRestrictionRule.eval(context, patientId, parameters);
                if (CollectionUtils.isNotEmpty(reasonStartedResults)
                        || (reasonStartedResults.latest().getResultDate().before(encounterResult.getResultDate())
                            && reasonStartedResults.latest().getResultDate().before(twelveMonthsAgo))) {
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
