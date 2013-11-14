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
public class Exclusion2DRule extends EvaluableRule {

    public static final String TOKEN = "Tuberculosis:Exclusion 2D";

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

        // 2d. Rule out history of TB treatment plan
        String TUBERCULOSIS_TREATMENT_PLAN = "TUBERCULOSIS TREATMENT PLAN"; // 1268
        String CONTINUE_REGIMEN = "CONTINUE REGIMEN"; // 1257
        String START_DRUGS = "START DRUGS"; // 1256
        String STOP_ALL_MEDICATIONS = "STOP ALL MEDICATIONS"; // 1260
        String CHANGE_REGIMEN = "CHANGE REGIMEN"; // 1259
        String DOSING_CHANGE = "DOSING CHANGE"; // 981
        String DRUG_RESTART = "DRUG RESTART"; // 1850
        String TUBERCULOSIS_DEFAULTER_REGIMEN_BY_USING_STREPTOMYCIN =
                "TUBERCULOSIS DEFAULTER REGIMEN BY USING STREPTOMYCIN"; // 2160
        String MULTIDRUG_RESISTANT_TUBERCULOSIS_REGIMEN = "MULTIDRUG-RESISTANT TUBERCULOSIS REGIMEN"; // 2161
        String REFILLED = "REFILLED"; // 1406
        String NOT_REFILLED = "NOT REFILLED"; // 1407
        String DRUG_SUBSTITUTION = "DRUG SUBSTITUTION"; // 1849

        parameters.put(EvaluableConstants.ENCOUNTER_FETCH_SIZE, 1);
        parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(TUBERCULOSIS_TREATMENT_PLAN));
        parameters.put(EvaluableConstants.OBS_VALUE_CODED,
                Arrays.asList(CONTINUE_REGIMEN, START_DRUGS, STOP_ALL_MEDICATIONS, CHANGE_REGIMEN, DOSING_CHANGE,
                        DRUG_RESTART, TUBERCULOSIS_DEFAULTER_REGIMEN_BY_USING_STREPTOMYCIN,
                        MULTIDRUG_RESISTANT_TUBERCULOSIS_REGIMEN, REFILLED, NOT_REFILLED, DRUG_SUBSTITUTION));
        Result obsResults = obsWithRestrictionRule.eval(context, patientId, parameters);
        if (!obsResults.isEmpty()) {
            return new Result(Boolean.TRUE);
        }

        // 2d. Rule out history of TB treatment plan
        String REASON_TUBERCULOSIS_TREATMENT_STOPPED = "REASON TUBERCULOSIS TREATMENT STOPPED"; // 1269
        String WEIGHT_CHANGE = "WEIGHT CHANGE"; // 983
        String COMPLETED = "COMPLETED"; // 1267
        String TOXICITY_DRUG = "TOXICITY, DRUG"; // 102
        String OTHER_NON_CODED = "OTHER NON-CODED"; // 5622
        String TUBERCULOSIS_INDUCTION_TREATMENT_PHASE_COMPLETION = "TUBERCULOSIS INDUCTION TREATMENT PHASE COMPLETION"; // 7904
        String TUBERCULOSIS_TREATMENT_COMPLETION = "TUBERCULOSIS TREATMENT COMPLETION"; // 7905

        parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(REASON_TUBERCULOSIS_TREATMENT_STOPPED));
        parameters.put(EvaluableConstants.OBS_VALUE_CODED,
                Arrays.asList(WEIGHT_CHANGE, COMPLETED, TOXICITY_DRUG, OTHER_NON_CODED,
                        TUBERCULOSIS_INDUCTION_TREATMENT_PHASE_COMPLETION, TUBERCULOSIS_TREATMENT_COMPLETION));
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
