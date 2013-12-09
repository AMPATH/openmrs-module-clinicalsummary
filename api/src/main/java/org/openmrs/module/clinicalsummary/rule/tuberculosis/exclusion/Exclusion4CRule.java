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

import org.openmrs.Obs;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;

/**
 * TODO: Write brief description about the class here.
 */
public class Exclusion4CRule extends EvaluableRule {

    public static final String TOKEN = "Tuberculosis:Exclusion 4C";

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

        String TB_PROPHYLAXIS_ADHERENCE = "TB PROPHYLAXIS ADHERENCE, PAST WEEK"; // 1165
        String FEW = "FEW"; // 1160
        String HALF = "HALF"; // 1161
        String MOST = "MOST"; // 1162
        String ALL = "ALL"; // 1163

        parameters.put(EvaluableConstants.OBS_FETCH_SIZE, 1);
        parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(TB_PROPHYLAXIS_ADHERENCE));
        parameters.put(EvaluableConstants.OBS_VALUE_CODED,Arrays.asList(FEW, HALF, MOST, ALL));
        Result obsResults = obsWithRestrictionRule.eval(context, patientId, parameters);
        if (!obsResults.isEmpty()) {
            Result cxrResult = obsResults.get(0);
            Obs obs = (Obs) cxrResult.getResultObject();

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, 2013);
            calendar.set(Calendar.MONTH, Calendar.SEPTEMBER);
            calendar.set(Calendar.DATE, 1);

            if (obs.getObsDatetime().before(calendar.getTime())) {
                result = new Result(Boolean.TRUE);
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
