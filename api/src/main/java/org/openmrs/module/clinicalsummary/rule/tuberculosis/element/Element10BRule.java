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
package org.openmrs.module.clinicalsummary.rule.tuberculosis.element;

import org.openmrs.Encounter;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableNameConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableRule;
import org.openmrs.module.clinicalsummary.rule.encounter.EncounterWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.encounter.EncounterWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;

import java.util.Arrays;
import java.util.Map;

/**
 * TODO: Write brief description about the class here.
 */
public class Element10BRule extends EvaluableRule {

    public static final String TOKEN = "Tuberculosis:Element 10B";

    /**
     * @param context
     * @param patientId
     * @param parameters
     * @return
     * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
     */
    @Override
    protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) {
        Result result = new Result(Boolean.FALSE);

        parameters.put(EvaluableConstants.ENCOUNTER_TYPE,
                Arrays.asList(EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_INITIAL,
                        EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_RETURN));
        parameters.put(EvaluableConstants.ENCOUNTER_FETCH_SIZE, 1);
        EncounterWithRestrictionRule encounterWithRestrictionRule = new EncounterWithStringRestrictionRule();
        Result encounterResults = encounterWithRestrictionRule.eval(context, patientId, parameters);
        if (!encounterResults.isEmpty()) {
            Result encounterResult = encounterResults.get(0);
            Encounter encounter = (Encounter) encounterResult.getResultObject();

            ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();
            parameters.put(EvaluableConstants.OBS_FETCH_SIZE, 1);
            parameters.put(EvaluableConstants.OBS_ENCOUNTER, Arrays.asList(encounter));

            String GENERAL_REVIEW_OF_SYSTEM = "REVIEW OF SYSTEMS, GENERAL"; // 1069
            String FEVER = "FEVER"; // 5945
            String NIGHT_SWEATS = "NIGHT SWEATS"; // 6029
            String WEIGHT_LOSS = "WEIGHT LOSS"; // 832

            parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(GENERAL_REVIEW_OF_SYSTEM));
            parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList(FEVER, NIGHT_SWEATS, WEIGHT_LOSS));
            Result generalReviewResults = obsWithRestrictionRule.eval(context, patientId, parameters);

            String REVIEW_OF_CARDIOPULMONARY = "REVIEW OF SYSTEMS, CARDIOPULMONARY"; // 1071
            String COUGH = "COUGH"; // 107
            String PRODUCTIVE_COUGH = "PRODUCTIVE COUGH"; // 5957
            String TUBERCULOSIS = "TUBERCULOSIS"; // 58

            parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(REVIEW_OF_CARDIOPULMONARY));
            parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList(COUGH, PRODUCTIVE_COUGH, TUBERCULOSIS));
            Result cardioReviewResults = obsWithRestrictionRule.eval(context, patientId, parameters);

            String REVIEW_OF_TUBERCULOSIS_SCREENING_QUESTIONS = "REVIEW OF TUBERCULOSIS SCREENING QUESTIONS"; // 6174
            String COUGH_FOR_MORE_THAN_TWO_WEEKS = "COUGH FOR MORE THAN TWO WEEKS"; // 6171
            String FEVER_MORE_THAN_TWO_WEEKS = "FEVER MORE THAN TWO WEEKS"; // 8065
            // String WEIGHT_LOSS = "WEIGHT LOSS"; // 832
            String NIGHT_SWEATS_MORE_THAN_TWO_WEEKS = "NIGHT SWEATS MORE THAN TWO WEEKS"; // 8061
            String HOUSEHOLD_MEMBER_DIAGNOSED_WITH_TUBERCULOSIS = " HOUSEHOLD MEMBER DIAGNOSED WITH TUBERCULOSIS"; // 2020

            parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(REVIEW_OF_TUBERCULOSIS_SCREENING_QUESTIONS));
            parameters.put(EvaluableConstants.OBS_VALUE_CODED,
                    Arrays.asList(COUGH_FOR_MORE_THAN_TWO_WEEKS, FEVER_MORE_THAN_TWO_WEEKS, WEIGHT_LOSS,
                            NIGHT_SWEATS_MORE_THAN_TWO_WEEKS, HOUSEHOLD_MEMBER_DIAGNOSED_WITH_TUBERCULOSIS));
            Result screeningResults = obsWithRestrictionRule.eval(context, patientId, parameters);

            if (!generalReviewResults.isEmpty() || !cardioReviewResults.isEmpty() || !screeningResults.isEmpty()) {
                return new Result(Boolean.TRUE);
            }
        }

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
