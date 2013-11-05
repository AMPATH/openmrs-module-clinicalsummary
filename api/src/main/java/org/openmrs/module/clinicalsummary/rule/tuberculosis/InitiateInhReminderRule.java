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
package org.openmrs.module.clinicalsummary.rule.tuberculosis;

import org.openmrs.logic.LogicContext;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.enumeration.FetchOrdering;
import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableNameConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableRule;
import org.openmrs.module.clinicalsummary.rule.encounter.EncounterWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.encounter.EncounterWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.reminder.ReminderParameters;

import java.util.Arrays;
import java.util.Map;

/**
 * TODO: Write brief description about the class here.
 */
public class InitiateInhReminderRule extends EvaluableRule {

    public static final String TOKEN = "Tuberculosis:Initiate INH Reminder";

    /**
     * @param context
     * @param patientId
     * @param parameters
     * @return
     * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
     */
    @Override
    protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) {
        Result result = new Result();

        parameters.put(EvaluableConstants.ENCOUNTER_TYPE,
                Arrays.asList(EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_INITIAL,
                        EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_RETURN));
        parameters.put(EvaluableConstants.ENCOUNTER_FETCH_SIZE, 1);
        parameters.put(EvaluableConstants.ENCOUNTER_FETCH_ORDER, FetchOrdering.ORDER_DESCENDING.getValue());
        EncounterWithRestrictionRule encounterWithRestrictionRule = new EncounterWithStringRestrictionRule();
        Result hivEncounters = encounterWithRestrictionRule.eval(context, patientId, parameters);
        if (hivEncounters.isEmpty()) {
            return result;
        }

        TuberculosisHistoryExistsRule tuberculosisHistoryExistsRule = new TuberculosisHistoryExistsRule();
        Result tbHistoryExistsResult = tuberculosisHistoryExistsRule.eval(context, patientId, parameters);
        if (tbHistoryExistsResult.toBoolean()) {
            return result;
        }

        InhScreeningQuestionExistsRule inhScreeningQuestionExistsRule = new InhScreeningQuestionExistsRule();
        Result inhScreeningQuestionExistsResult = inhScreeningQuestionExistsRule.eval(context, patientId, parameters);
        if (!inhScreeningQuestionExistsResult.toBoolean()) {
            return result;
        }

        TuberculosisSymptomExistsRule tuberculosisSymptomExistsRule = new TuberculosisSymptomExistsRule();
        Result tuberculosisSymptomExistsResult = tuberculosisSymptomExistsRule.eval(context, patientId, parameters);
        if (tuberculosisSymptomExistsResult.toBoolean()) {
            return result;
        }

        PositiveCxrExistsRule positiveCxrExistsRule = new PositiveCxrExistsRule();
        Result positiveCxrExistsResult = positiveCxrExistsRule.eval(context, patientId, parameters);
        if (positiveCxrExistsResult.toBoolean()) {
            return result;
        }

        NormalCXRExistsRule normalCxrExistsRule = new NormalCXRExistsRule();
        Result normalCxrExistsResult = normalCxrExistsRule.eval(context, patientId, parameters);
        if (!normalCxrExistsResult.toBoolean()) {
            return result;
        }

        ProphylaxisHistoryExistsRule prophylaxisHistoryExistsRule = new ProphylaxisHistoryExistsRule();
        Result prophylaxisHistoryExistsResult = prophylaxisHistoryExistsRule.eval(context, patientId, parameters);
        if (prophylaxisHistoryExistsResult.toBoolean()) {
            return result;
        }

        result.add(new Result(String.valueOf(parameters.get(ReminderParameters.DISPLAYED_REMINDER_TEXT))));
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
