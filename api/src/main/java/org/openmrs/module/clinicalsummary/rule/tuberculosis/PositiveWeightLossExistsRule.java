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
import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;

import java.util.Arrays;
import java.util.Map;

/**
 * TODO: Write brief description about the class here.
 */
public class PositiveWeightLossExistsRule extends EvaluableRule {

    public static final String TOKEN = "Tuberculosis:Positive Weight Loss Exists";

    /**
     * @param context
     * @param patientId
     * @param parameters
     * @return
     * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
     */
    @Override
    protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) {

        ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();
        parameters.put(EvaluableConstants.OBS_FETCH_SIZE, 1);

        Result tbScreeningResults;

        String PROBLEM_ADDED = "PROBLEM ADDED";
        String WEIGHT_LOSS = "WEIGHT LOSS";

        parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList(WEIGHT_LOSS));
        parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(PROBLEM_ADDED));
        tbScreeningResults = obsWithRestrictionRule.eval(context, patientId, parameters);
        if (!tbScreeningResults.isEmpty()) {
            return new Result(Boolean.TRUE);
        }

        String GENERAL_REVIEW_OF_SYSTEM = "REVIEW OF SYSTEMS, GENERAL";

        parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList(WEIGHT_LOSS));
        parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(GENERAL_REVIEW_OF_SYSTEM));
        tbScreeningResults = obsWithRestrictionRule.eval(context, patientId, parameters);
        if (!tbScreeningResults.isEmpty()) {
            return new Result(Boolean.TRUE);
        }

        String REVIEW_OF_TUBERCULOSIS_SCREENING_QUESTIONS = "REVIEW OF TUBERCULOSIS SCREENING QUESTIONS";

        parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList(WEIGHT_LOSS));
        parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(REVIEW_OF_TUBERCULOSIS_SCREENING_QUESTIONS));
        tbScreeningResults = obsWithRestrictionRule.eval(context, patientId, parameters);
        if (!tbScreeningResults.isEmpty()) {
            return new Result(Boolean.TRUE);
        }

        return new Result(Boolean.FALSE);
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
