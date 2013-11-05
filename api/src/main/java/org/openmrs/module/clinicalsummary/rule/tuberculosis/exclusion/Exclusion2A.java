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
package org.openmrs.module.clinicalsummary.rule.tuberculosis.exclusion;

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
public class Exclusion2A extends EvaluableRule {

    public static final String TOKEN = "Tuberculosis:Exclusion 2A";

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

        String WHO_STAGE_3_ADULT = "WHO STAGE 3 ADULT";  // 1207
        String WHO_STAGE_4_ADULT = "WHO STAGE 4 ADULT"; // 1206

        String PRESUMPTIVE_HIV_STAGING_EXTRAPULMONARY_TUBERCULOSIS =
                "PRESUMPTIVE HIV STAGING - EXTRAPULMONARY TUBERCULOSIS"; // 2122
        String CONFIRMED_HIV_STAGING_EXTRAPULMONARY_TUBERCULOSIS =
                "CONFIRMED HIV STAGING - EXTRAPULMONARY TUBERCULOSIS"; // 2123
        String PRESUMPTIVE_HIV_STAGING_TUBERCULOSIS_PULMONARY =
                "PRESUMPTIVE HIV STAGING - TUBERCULOSIS, PULMONARY"; // 2104
        String CONFIRMED_HIV_STAGING_TUBERCULOSIS_PULMONARY =
                "CONFIRMED HIV STAGING - TUBERCULOSIS, PULMONARY"; // 2105
        String MYCOBACTERIUM_TUBERCULOSIS_EXTRAPULMONARY =
                "MYCOBACTERIUM TUBERCULOSIS, EXTRAPULMONARY"; // 5042

        parameters.put(EvaluableConstants.OBS_FETCH_SIZE, 1);
        parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(WHO_STAGE_3_ADULT, WHO_STAGE_4_ADULT));
        parameters.put(EvaluableConstants.OBS_VALUE_CODED,
                Arrays.asList(PRESUMPTIVE_HIV_STAGING_EXTRAPULMONARY_TUBERCULOSIS,
                        CONFIRMED_HIV_STAGING_EXTRAPULMONARY_TUBERCULOSIS, PRESUMPTIVE_HIV_STAGING_TUBERCULOSIS_PULMONARY,
                        CONFIRMED_HIV_STAGING_TUBERCULOSIS_PULMONARY, MYCOBACTERIUM_TUBERCULOSIS_EXTRAPULMONARY));
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
