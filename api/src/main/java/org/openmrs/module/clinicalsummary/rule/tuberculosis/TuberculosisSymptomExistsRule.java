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
import org.openmrs.module.clinicalsummary.rule.EvaluableRule;

import java.util.Map;

/**
 * TODO: Write brief description about the class here.
 */
public class TuberculosisSymptomExistsRule extends EvaluableRule {

    public static final String TOKEN = "Tuberculosis:Tuberculosis Symptom Exists";

    /**
     * @param context
     * @param patientId
     * @param parameters
     * @return
     * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
     */
    @Override
    protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) {
        PositiveFeverExistsRule feverExistsRule = new PositiveFeverExistsRule();
        Result feverExistsResult = feverExistsRule.eval(context, patientId, parameters);
        if (feverExistsResult.toBoolean()) {
            return new Result(Boolean.TRUE);
        }

        PositiveWeightLossExistsRule weightLossExistsRule = new PositiveWeightLossExistsRule();
        Result weighLostExistsResult = weightLossExistsRule.eval(context, patientId, parameters);
        if (weighLostExistsResult.toBoolean()) {
            return new Result(Boolean.TRUE);
        }

        PositiveNightSweatExistsRule nightSweatExistsRule = new PositiveNightSweatExistsRule();
        Result nightSweatExistsResult = nightSweatExistsRule.eval(context, patientId, parameters);
        if (nightSweatExistsResult.toBoolean()) {
            return new Result(Boolean.TRUE);
        }

        PositiveCoughExistsRule coughExistsRule = new PositiveCoughExistsRule();
        Result coughExistsResult = coughExistsRule.eval(context, patientId, parameters);
        if (coughExistsResult.toBoolean()) {
            return new Result(Boolean.TRUE);
        }

        return new Result(Boolean.FALSE);
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
