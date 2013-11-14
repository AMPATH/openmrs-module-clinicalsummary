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
public class Element10CRule extends EvaluableRule {

    public static final String TOKEN = "Tuberculosis:Element 10C";

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

        String LYMPH_NODE_EXAM_FINDINGS = "LYMPH NODE EXAM FINDINGS"; // 1121
        String SUBMANDIBULAR = "SUBMANDIBULAR"; // 504
        String SUPRACLAVICULAR = "SUPRACLAVICULAR"; // 505
        String INGUINAL = "INGUINAL"; // 506
        String CERVICAL = "CERVICAL"; // 643
        String ABNORMAL = "ABNORMAL"; // 1116
        String AXILLARY = "AXILLARY"; // 5112
        String LYMPHADENOPATHY = "LYMPHADENOPATHY"; // 161
        String OTHER_NON_CODED = "OTHER NON-CODED"; // 5622

        parameters.put(EvaluableConstants.OBS_FETCH_SIZE, 1);
        parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(LYMPH_NODE_EXAM_FINDINGS));
        parameters.put(EvaluableConstants.OBS_VALUE_CODED,
                Arrays.asList(SUBMANDIBULAR, SUPRACLAVICULAR, INGUINAL, CERVICAL, ABNORMAL, AXILLARY,
                        LYMPHADENOPATHY, OTHER_NON_CODED));
        Result obsResults = obsWithRestrictionRule.eval(context, patientId, parameters);
        if (!obsResults.isEmpty()) {
            return new Result(Boolean.TRUE);
        }

        String CHEST_EXAM_FINDINGS = "CHEST EXAM FINDINGS"; // 1121
        // String ABNORMAL = "ABNORMAL"; // 1116
        String DIMINISHED_BREATH_SOUNDS = "DIMINISHED BREATH SOUNDS"; // 5115
        String BRONCHIAL_BREATH_SOUNDS = "BRONCHIAL BREATH SOUNDS"; // 5116
        String CREPITATIONS = "CREPITATIONS"; // 5127
        String DULLNESS_TO_PERCUSSION = "DULLNESS TO PERCUSSION"; // 5138
        String RHONCHI = "RHONCHI"; // 5181
        String WHEEZE = "WHEEZE"; // 5209

        parameters.put(EvaluableConstants.OBS_FETCH_SIZE, 1);
        parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(CHEST_EXAM_FINDINGS));
        parameters.put(EvaluableConstants.OBS_VALUE_CODED,
                Arrays.asList(ABNORMAL, DIMINISHED_BREATH_SOUNDS, BRONCHIAL_BREATH_SOUNDS, CREPITATIONS,
                        DULLNESS_TO_PERCUSSION, RHONCHI, WHEEZE));
        obsResults = obsWithRestrictionRule.eval(context, patientId, parameters);
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
