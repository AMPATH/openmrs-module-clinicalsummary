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
public class Element3ARule extends EvaluableRule {

    public static final String TOKEN = "Tuberculosis:Element 3A";

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

            parameters.remove(EvaluableConstants.OBS_VALUE_CODED);
            parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(GENERAL_REVIEW_OF_SYSTEM));
            Result generalReviewResults = obsWithRestrictionRule.eval(context, patientId, parameters);

            String REVIEW_OF_CARDIOPULMONARY = "REVIEW OF SYSTEMS, CARDIOPULMONARY"; // 1071

            parameters.remove(EvaluableConstants.OBS_VALUE_CODED);
            parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(REVIEW_OF_CARDIOPULMONARY));
            Result cardioReviewResults = obsWithRestrictionRule.eval(context, patientId, parameters);
            if (!generalReviewResults.isEmpty() && !cardioReviewResults.isEmpty()) {
                return new Result(Boolean.TRUE);
            }

            String REVIEW_OF_TUBERCULOSIS_SCREENING_QUESTIONS = "REVIEW OF TUBERCULOSIS SCREENING QUESTIONS"; // 6174

            parameters.remove(EvaluableConstants.OBS_VALUE_CODED);
            parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(REVIEW_OF_TUBERCULOSIS_SCREENING_QUESTIONS));
            Result tuberculosisQuestionResults = obsWithRestrictionRule.eval(context, patientId, parameters);
            if (!tuberculosisQuestionResults.isEmpty()) {
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
