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
public class Element10ARule extends EvaluableRule {

    public static final String TOKEN = "Tuberculosis:Element 10A";

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
        ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();

        String PROBLEM_ADDED = "PROBLEM ADDED"; // 6042
        String PROBLEM_ONGOING = "PROBLEM ONGOING"; // 2034
        String TUBERCULOSIS_MENINGITIS = "TUBERCULOSIS MENINGITIS"; // 8348
        String TUBERCULOSIS = "TUBERCULOSIS"; // 058
        String TUBERCULOSIS_MILIARY = "TUBERCULOSIS, MILIARY"; // 1460
        String SKELETAL_TUBERCULOSIS = "SKELETAL TUBERCULOSIS"; // 8284
        String ABDOMINAL_TUBERCULOSIS = "ABDOMINAL TUBERCULOSIS"; // 8283
        String LYMPH_NODE_TUBERCULOSIS = "LYMPH NODE TUBERCULOSIS"; // 2213
        String PNEUMONIA_TUBERCULOUS = "PNEUMONIA, TUBERCULOUS"; // 042

        parameters.put(EvaluableConstants.OBS_FETCH_SIZE, 1);
        parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(PROBLEM_ADDED, PROBLEM_ONGOING));
        parameters.put(EvaluableConstants.OBS_VALUE_CODED,
                Arrays.asList(TUBERCULOSIS_MENINGITIS, TUBERCULOSIS, TUBERCULOSIS_MILIARY, SKELETAL_TUBERCULOSIS,
                        ABDOMINAL_TUBERCULOSIS, LYMPH_NODE_TUBERCULOSIS, PNEUMONIA_TUBERCULOUS));
        Result obsResults = obsWithRestrictionRule.eval(context, patientId, parameters);
        if (!obsResults.isEmpty()) {
            return new Result(Boolean.TRUE);
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
