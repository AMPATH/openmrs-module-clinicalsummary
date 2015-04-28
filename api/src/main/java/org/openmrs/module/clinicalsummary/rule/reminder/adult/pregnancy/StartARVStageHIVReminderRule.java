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
import org.openmrs.module.clinicalsummary.rule.medication.AntiRetroViralRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.reminder.ReminderParameters;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 */
public class StartARVStageHIVReminderRule extends EvaluableRule {

    public static final String TOKEN = "Adult:Start ARV WHO Stage HIV Positive Reminder";

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

            AntiRetroViralRule antiRetroViralRule = new AntiRetroViralRule();
            // prepare the encounter types
            parameters.put(EvaluableConstants.ENCOUNTER_TYPE, Arrays.asList(EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_INITIAL,
                    EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_RETURN, EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_NONCLINICALMEDICATION));
            Result arvResults = antiRetroViralRule.eval(context, patientId, parameters);
            if (CollectionUtils.isEmpty(arvResults)) {

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(encounterResult.getResultDate());
                calendar.add(Calendar.WEEK_OF_YEAR, 42);
                Date fortyTwoWeeksLater = calendar.getTime();

                ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();

                parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList("REASON ANTIRETROVIRALS STARTED",
                        "CURRENT WHO HIV STAGE", "PATIENT REPORTED WHO STAGE PRIOR TO ENROLLMENT AT AMPATH, ADULT"));
                parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList("WHO STAGE 3 ADULT",
                        "WHO STAGE 4 ADULT", "ADULT WHO STAGE 3 WITH CD4 COUNT LESS THAN 350"));

                Result whoStageResults = obsWithRestrictionRule.eval(context, patientId, parameters);
                if (CollectionUtils.isNotEmpty(whoStageResults)
                        && whoStageResults.latest().getResultDate().before(fortyTwoWeeksLater)) {
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
