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
public class Element10E extends EvaluableRule {

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
        String CONTINUE_TO_PICK_DRUGS_FROM_OTHER_LOCATION = "CONTINUE TO PICK DRUGS FROM OTHER LOCATION"; // 8352

        parameters.put(EvaluableConstants.ENCOUNTER_FETCH_SIZE, 1);
        parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(TUBERCULOSIS_TREATMENT_PLAN));
        parameters.put(EvaluableConstants.OBS_VALUE_CODED,
                Arrays.asList(CONTINUE_REGIMEN, START_DRUGS, STOP_ALL_MEDICATIONS, CHANGE_REGIMEN, DOSING_CHANGE,
                        DRUG_RESTART, TUBERCULOSIS_DEFAULTER_REGIMEN_BY_USING_STREPTOMYCIN,
                        MULTIDRUG_RESISTANT_TUBERCULOSIS_REGIMEN, REFILLED, NOT_REFILLED, DRUG_SUBSTITUTION,
                        CONTINUE_TO_PICK_DRUGS_FROM_OTHER_LOCATION));
        Result obsResults = obsWithRestrictionRule.eval(context, patientId, parameters);
        if (!obsResults.isEmpty()) {
            return new Result(Boolean.TRUE);
        }

        String REASON_TUBERCULOSIS_TREATMENT_STARTED = "REASON TUBERCULOSIS TREATMENT STARTED"; // 1269
        String TUBERCULOSIS_TREATMENT_RETREATED_AFTER_RELAPSE_OR_RE_INFECTION
                = "TUBERCULOSIS TREATMENT RETREATED AFTER RELAPSE OR RE-INFECTION"; // 6980
        String NEW_TUBERCULOSIS_TREATMENT = "NEW TUBERCULOSIS TREATMENT"; // 6977
        String TUBERCULOSIS_TREATMENT_RESTART_AFTER_REGIMEN_FAILURE
                = "TUBERCULOSIS TREATMENT RESTART AFTER REGIMEN FAILURE"; // 6979
        String TUBERCULOSIS_TREATMENT_RESTART_AFTER_BEING_DEFULTED
                = "TUBERCULOSIS TREATMENT RESTART AFTER BEING DEFULTED"; // 6978
        String OTHER_NON_CODED = "OTHER NON-CODED"; // 5622
//        String MULTIDRUG_RESISTANT_TUBERCULOSIS_REGIMEN = "MULTIDRUG-RESISTANT TUBERCULOSIS REGIMEN"; // 2161

        parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(REASON_TUBERCULOSIS_TREATMENT_STARTED));
        parameters.put(EvaluableConstants.OBS_VALUE_CODED,
                Arrays.asList(TUBERCULOSIS_TREATMENT_RETREATED_AFTER_RELAPSE_OR_RE_INFECTION,
                        NEW_TUBERCULOSIS_TREATMENT, TUBERCULOSIS_TREATMENT_RESTART_AFTER_REGIMEN_FAILURE,
                        TUBERCULOSIS_TREATMENT_RESTART_AFTER_BEING_DEFULTED, OTHER_NON_CODED,
                        MULTIDRUG_RESISTANT_TUBERCULOSIS_REGIMEN));
        obsResults = obsWithRestrictionRule.eval(context, patientId, parameters);
        if (!obsResults.isEmpty()) {
            return new Result(Boolean.TRUE);
        }

        String TUBERCULOSIS_TREATMENT_STARTED = "TUBERCULOSIS TREATMENT STARTED"; // 1270
        String STREPTOMYCIN = "STREPTOMYCIN"; // 438
        String RIFAMPICIN_AND_ISONIAZID = "RIFAMPICIN AND ISONIAZID"; // 1194
        String RIFAMPICIN = "RIFAMPICIN"; // 767
//        String OTHER_NON_CODED = "OTHER NON-CODED"; // 5622
        String PEDIATRIC_RIFAMPICIN_AND_ISONIAZID = "PEDIATRIC RIFAMPICIN AND ISONIAZID"; // 6603
        String PEDIATRIC_RIFAMPICIN_ISONIAZID_AND_PYRAZINAMIDE = "PEDIATRIC RIFAMPICIN ISONIAZID AND PYRAZINAMIDE"; // 6602
        String RIFAMPICIN_ISONIAZID_AND_PYRAZINAMIDE = "RIFAMPICIN ISONIAZID AND PYRAZINAMIDE"; // 768
        String RIFABUTIN = "RIFABUTIN"; // 6983
        String ISONIAZID = "ISONIAZID"; // 656
        String PYRAZINAMIDE = "PYRAZINAMIDE"; // 5829
        String ETHAMBUTOL_AND_ISONIZAID = "ETHAMBUTOL AND ISONIZAID"; // 1108
        String ETHAMBUTOL = "ETHAMBUTOL"; // 745
        String RIFAMPICIN_ISONIAZID_AND_ETHAMBUTOL = "RIFAMPICIN ISONIAZID AND ETHAMBUTOL"; // 2231
        String RIFAMPICIN_ISONIAZID_PYRAZINAMIDE_AND_ETHAMBUTOL = "RIFAMPICIN ISONIAZID PYRAZINAMIDE AND ETHAMBUTOL"; // 1131
//        String MULTIDRUG_RESISTANT_TUBERCULOSIS_REGIMEN = "MULTIDRUG-RESISTANT TUBERCULOSIS REGIMEN"; // 2161

        parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(TUBERCULOSIS_TREATMENT_STARTED));
        parameters.put(EvaluableConstants.OBS_VALUE_CODED,
                Arrays.asList(PYRAZINAMIDE, STREPTOMYCIN, RIFAMPICIN_ISONIAZID_PYRAZINAMIDE_AND_ETHAMBUTOL,
                        ETHAMBUTOL, RIFAMPICIN_ISONIAZID_AND_PYRAZINAMIDE, ISONIAZID, RIFAMPICIN_AND_ISONIAZID,
                        OTHER_NON_CODED, RIFAMPICIN, ETHAMBUTOL_AND_ISONIZAID, RIFAMPICIN_ISONIAZID_AND_ETHAMBUTOL,
                        PEDIATRIC_RIFAMPICIN_ISONIAZID_AND_PYRAZINAMIDE, PEDIATRIC_RIFAMPICIN_AND_ISONIAZID, RIFABUTIN,
                        MULTIDRUG_RESISTANT_TUBERCULOSIS_REGIMEN));
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
