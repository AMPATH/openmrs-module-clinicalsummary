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
public class Exclusion2CRule extends EvaluableRule {

    public static final String TOKEN = "Tuberculosis:Exclusion 2C";

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

        // 2c. Rule out patient reported history of anti-TB meds
        String PATIENT_REPORTED_CURRENT_TUBERCULOSIS_TREATMENT = "PATIENT REPORTED CURRENT TUBERCULOSIS TREATMENT"; // 1111
        String PYRAZINAMIDE = "PYRAZINAMIDE"; // 5829
        String STREPTOMYCIN = "STREPTOMYCIN"; // 438
        String RIFAMPICIN_ISONIAZID_PYRAZINAMIDE_AND_ETHAMBUTOL = "RIFAMPICIN ISONIAZID PYRAZINAMIDE AND ETHAMBUTOL"; // 1131
        String ETHAMBUTOL = "ETHAMBUTOL"; // 745
        String RIFAMPICIN_ISONIAZID_AND_PYRAZINAMIDE = "RIFAMPICIN ISONIAZID AND PYRAZINAMIDE"; // 768
        String ISONIAZID = "ISONIAZID"; // 656
        String RIFAMPICIN_AND_ISONIAZID = "RIFAMPICIN AND ISONIAZID"; // 1194
        String OTHER_NON_CODED = "OTHER NON-CODED"; // 5622
        String RIFAMPICIN = "RIFAMPICIN"; // 767
        String ETHAMBUTOL_AND_ISONIZAID = "ETHAMBUTOL AND ISONIZAID"; // 1108
        String COMPLETED = "COMPLETED"; // 1267
        String RIFAMPICIN_ISONIAZID_AND_ETHAMBUTOL = "RIFAMPICIN ISONIAZID AND ETHAMBUTOL"; // 2231
        String YES = "YES"; // 1065
        String PEDIATRIC_RIFAMPICIN_ISONIAZID_AND_PYRAZINAMIDE = "PEDIATRIC RIFAMPICIN ISONIAZID AND PYRAZINAMIDE"; // 6602
        String PEDIATRIC_RIFAMPICIN_AND_ISONIAZID = "PEDIATRIC RIFAMPICIN AND ISONIAZID"; // 6603
        String RIFABUTIN = "RIFABUTIN"; // 6983
        String MULTIDRUG_RESISTANT_TUBERCULOSIS_REGIMEN = "MULTIDRUG-RESISTANT TUBERCULOSIS REGIMEN"; // 2161

        parameters.put(EvaluableConstants.OBS_FETCH_SIZE, 1);
        parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(PATIENT_REPORTED_CURRENT_TUBERCULOSIS_TREATMENT));
        parameters.put(EvaluableConstants.OBS_VALUE_CODED,
                Arrays.asList(PYRAZINAMIDE, STREPTOMYCIN, RIFAMPICIN_ISONIAZID_PYRAZINAMIDE_AND_ETHAMBUTOL,
                        ETHAMBUTOL, RIFAMPICIN_ISONIAZID_AND_PYRAZINAMIDE, ISONIAZID, RIFAMPICIN_AND_ISONIAZID,
                        OTHER_NON_CODED, RIFAMPICIN, ETHAMBUTOL_AND_ISONIZAID, COMPLETED,
                        RIFAMPICIN_ISONIAZID_AND_ETHAMBUTOL, YES, PEDIATRIC_RIFAMPICIN_ISONIAZID_AND_PYRAZINAMIDE,
                        PEDIATRIC_RIFAMPICIN_AND_ISONIAZID, RIFABUTIN, MULTIDRUG_RESISTANT_TUBERCULOSIS_REGIMEN));
        Result obsResults = obsWithRestrictionRule.eval(context, patientId, parameters);
        if (!obsResults.isEmpty()) {
            result = new Result(Boolean.TRUE);
        }

        String TUBERCULOSIS_DRUG_TREATMENT_START_DATE = "TUBERCULOSIS DRUG TREATMENT START DATE"; // 1113

        parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(TUBERCULOSIS_DRUG_TREATMENT_START_DATE));
        parameters.remove(EvaluableConstants.OBS_VALUE_CODED);
        Result obsDateResults = obsWithRestrictionRule.eval(context, patientId, parameters);
        if (!obsDateResults.isEmpty()) {
            result = new Result(Boolean.TRUE);
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
