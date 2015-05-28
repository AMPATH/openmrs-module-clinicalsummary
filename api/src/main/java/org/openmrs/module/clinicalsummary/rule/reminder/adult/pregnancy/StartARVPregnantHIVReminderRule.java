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
import org.openmrs.Obs;
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
public class StartARVPregnantHIVReminderRule extends EvaluableRule {

    public static final String TOKEN = "Adult:Start ARV Pregnant HIV Positive Reminder";

    private static final String PREGNANCY_STATUS = "PREGNANCY STATUS";

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

                parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(PREGNANCY_STATUS));
                parameters.put(EvaluableConstants.OBS_FETCH_SIZE, 1);

                Result pregnancyStatus = obsWithRestrictionRule.eval(context, patientId, parameters);
                if (CollectionUtils.isNotEmpty(pregnancyStatus)) {
                    Result pregnancyStatusResult = pregnancyStatus.latest();
                    if (pregnancyStatusResult.toBoolean() && !pregnancyStatusResult.getResultDate().after(fortyTwoWeeksLater)) {
                        result.add(new Result(String.valueOf(parameters.get(ReminderParameters.DISPLAYED_REMINDER_TEXT))));
                        return result;
                    }
                }

                parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList("URINE PREGNANCY TEST"));
                parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList(EvaluableNameConstants.POSITIVE));

                Result urinePregnancy = obsWithRestrictionRule.eval(context, patientId, parameters);
                if (CollectionUtils.isNotEmpty(urinePregnancy)
                        && !urinePregnancy.latest().getResultDate().after(fortyTwoWeeksLater)) {
                    result.add(new Result(String.valueOf(parameters.get(ReminderParameters.DISPLAYED_REMINDER_TEXT))));
                    return result;
                }

                parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.PROBLEM_ADDED));
                parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList("PREGNANCY"));

                Result problemAdded = obsWithRestrictionRule.eval(context, patientId, parameters);
                if (CollectionUtils.isNotEmpty(problemAdded)
                        && !problemAdded.latest().getResultDate().after(fortyTwoWeeksLater)) {
                    result.add(new Result(String.valueOf(parameters.get(ReminderParameters.DISPLAYED_REMINDER_TEXT))));
                    return result;
                }

                parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList("PREGNANCY STATUS, CODED"));
                parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList(EvaluableNameConstants.YES));

                Result pregnancyStatusCoded = obsWithRestrictionRule.eval(context, patientId, parameters);
                if (CollectionUtils.isNotEmpty(pregnancyStatusCoded)
                        && !pregnancyStatusCoded.latest().getResultDate().after(fortyTwoWeeksLater)) {
                    result.add(new Result(String.valueOf(parameters.get(ReminderParameters.DISPLAYED_REMINDER_TEXT))));
                    return result;
                }

                parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.NUMBER_OF_WEEKS_PREGNANT));
                Result numberOfWeeksPregnant = obsWithRestrictionRule.eval(context, patientId, parameters);
                if (CollectionUtils.isNotEmpty(numberOfWeeksPregnant)) {
                    Result numberOfWeeksPregnantResult = numberOfWeeksPregnant.latest();
                    if (numberOfWeeksPregnant.toNumber() > 0 && !numberOfWeeksPregnantResult.getResultDate().after(fortyTwoWeeksLater)) {
                        result.add(new Result(String.valueOf(parameters.get(ReminderParameters.DISPLAYED_REMINDER_TEXT))));
                        return result;
                    }
                }

                parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList("ANTENATAL CARE ENROLLED"));
                parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList(EvaluableNameConstants.YES));

                Result ancEnrolled = obsWithRestrictionRule.eval(context, patientId, parameters);
                if (CollectionUtils.isNotEmpty(ancEnrolled)) {
                    Result ancEnrolledResult = ancEnrolled.latest();

                    parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.PROBLEM_ADDED));
                    parameters.put(EvaluableConstants.OBS_CONCEPT,
                            Arrays.asList("PREGNANCY, MISCARRIAGE", "PREGNANCY, TERMINATION"));

                    Result pregnancyProblemAdded = obsWithRestrictionRule.eval(context, patientId, parameters);
                    if ((CollectionUtils.isEmpty(pregnancyProblemAdded) || pregnancyProblemAdded.latest().getResultDate().before(encounterResult.getResultDate()))
                            && !ancEnrolledResult.getResultDate().after(fortyTwoWeeksLater)) {
                        result.add(new Result(String.valueOf(parameters.get(ReminderParameters.DISPLAYED_REMINDER_TEXT))));
                        return result;
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
