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
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * TODO: Write brief description about the class here.
 */
public class PositiveCxrExistsRule extends EvaluableRule {

    public static final String TOKEN = "Tuberculosis:Positive CXR Exists";

    /**
     * @param context
     * @param patientId
     * @param parameters
     * @return
     * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
     */
    @Override
    protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) {

        String XRAY_CHEST_PRELIMINARY_FINDINGS = "X-RAY, CHEST, PRELIMINARY FINDINGS";

        String PULMONARY_EFFUSION = "PULMONARY EFFUSION";
        String MILIARY_CHANGES = "MILIARY CHANGES";
        String OTHER_NON_CODED = "OTHER NON-CODED";
        String INFILTRATE = "INFILTRATE";
        String DIFFUSE_NON_MILIARY_CHANGES = "DIFFUSE NON-MILIARY CHANGES";
        String CAVITARY_LESION = "CAVITARY LESION";
        String ABNORMAL = "ABNORMAL";

        ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();

        parameters.put(EvaluableConstants.OBS_FETCH_SIZE, 1);

        parameters.put(EvaluableConstants.OBS_VALUE_CODED,
                Arrays.asList(PULMONARY_EFFUSION, MILIARY_CHANGES, OTHER_NON_CODED, INFILTRATE,
                        DIFFUSE_NON_MILIARY_CHANGES, CAVITARY_LESION, ABNORMAL));
        parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(XRAY_CHEST_PRELIMINARY_FINDINGS));
        Result cxrResults = obsWithRestrictionRule.eval(context, patientId, parameters);
        if (!cxrResults.isEmpty()) {
            Result cxrResult = cxrResults.get(0);

            Calendar sixMonths = Calendar.getInstance();
            sixMonths.setTime(cxrResult.getResultDate());
            sixMonths.add(Calendar.MONTH, 6);

            Date today = new Date();
            if (today.before(sixMonths.getTime())) {
                return new Result(Boolean.TRUE);
            }
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
