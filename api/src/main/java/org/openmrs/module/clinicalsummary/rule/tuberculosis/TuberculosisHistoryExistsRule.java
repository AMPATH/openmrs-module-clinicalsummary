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
import org.openmrs.module.clinicalsummary.rule.encounter.EncounterWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.encounter.EncounterWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;

import java.util.Arrays;
import java.util.Map;

/**
 * TODO: Write brief description about the class here.
 */
public class TuberculosisHistoryExistsRule extends EvaluableRule {

    public static final String TOKEN = "Tuberculosis:Tuberculosis History Exists";

    /**
     * @param context
     * @param patientId
     * @param parameters
     * @return
     * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
     */
    @Override
    protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) {

        String ENCOUNTER_TYPE_TUBERCULOSIS = "TUBERCULOSIS"; // typeId = 27

        parameters.put(EvaluableConstants.ENCOUNTER_FETCH_SIZE, 1);
        parameters.put(EvaluableConstants.ENCOUNTER_TYPE, Arrays.asList(ENCOUNTER_TYPE_TUBERCULOSIS));
        EncounterWithRestrictionRule encounterWithStringRestrictionRule = new EncounterWithStringRestrictionRule();
        Result tbEncounterResults = encounterWithStringRestrictionRule.eval(context, patientId, parameters);
        if (!tbEncounterResults.isEmpty()) {
            return new Result(Boolean.TRUE);
        }

        Result tbHistoryResults;
        ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();

        // 2d. Rule out history of TB treatment plan
        String TUBERCULOSIS_TREATMENT_PLAN = "TUBERCULOSIS TREATMENT PLAN";
        String CONTINUE_REGIMEN = "CONTINUE REGIMEN";
        String START_DRUGS = "START DRUGS";
        String STOP_ALL_MEDICATIONS = "STOP ALL MEDICATIONS";
        String CHANGE_REGIMEN = "CHANGE REGIMEN";
        String DOSING_CHANGE = "DOSING CHANGE";
        String DRUG_RESTART = "DRUG RESTART";
        String TUBERCULOSIS_DEFAULTER_REGIMEN_BY_USING_STREPTOMYCIN = "TUBERCULOSIS DEFAULTER REGIMEN BY USING STREPTOMYCIN";
        String MULTIDRUG_RESISTANT_TUBERCULOSIS_REGIMEN = "MULTIDRUG-RESISTANT TUBERCULOSIS REGIMEN";
        String REFILLED = "REFILLED";
        String DRUG_SUBSTITUTION = "DRUG SUBSTITUTION";

        parameters.put(EvaluableConstants.ENCOUNTER_FETCH_SIZE, 1);
        parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(TUBERCULOSIS_TREATMENT_PLAN));
        parameters.put(EvaluableConstants.OBS_VALUE_CODED,
                Arrays.asList(CONTINUE_REGIMEN, START_DRUGS, STOP_ALL_MEDICATIONS, CHANGE_REGIMEN, DOSING_CHANGE,
                        DRUG_RESTART, TUBERCULOSIS_DEFAULTER_REGIMEN_BY_USING_STREPTOMYCIN,
                        MULTIDRUG_RESISTANT_TUBERCULOSIS_REGIMEN, REFILLED, DRUG_SUBSTITUTION));
        tbHistoryResults = obsWithRestrictionRule.eval(context, patientId, parameters);
        if (!tbHistoryResults.isEmpty()) {
            return new Result(Boolean.TRUE);
        }

        // 2d. Rule out history of TB treatment plan
        String REASON_TUBERCULOSIS_TREATMENT_STOPPED = "REASON TUBERCULOSIS TREATMENT STOPPED";

        parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(REASON_TUBERCULOSIS_TREATMENT_STOPPED));
        parameters.remove(EvaluableConstants.OBS_VALUE_CODED);
        tbHistoryResults = obsWithRestrictionRule.eval(context, patientId, parameters);
        if (!tbHistoryResults.isEmpty()) {
            return new Result(Boolean.TRUE);
        }

        // 2c. Rule out patient reported history of anti-TB meds
        String PATIENT_REPORTED_CURRENT_TUBERCULOSIS_TREATMENT = "PATIENT REPORTED CURRENT TUBERCULOSIS TREATMENT";
        String PYRAZINAMIDE = "PYRAZINAMIDE";
        String STREPTOMYCIN = "STREPTOMYCIN";
        String RIFAMPICIN_ISONIAZID_PYRAZINAMIDE_AND_ETHAMBUTOL = "RIFAMPICIN ISONIAZID PYRAZINAMIDE AND ETHAMBUTOL";
        String ETHAMBUTOL = "ETHAMBUTOL";
        String RIFAMPICIN_ISONIAZID_AND_PYRAZINAMIDE = "RIFAMPICIN ISONIAZID AND PYRAZINAMIDE";
        String ISONIAZID = "ISONIAZID";
        String RIFAMPICIN_AND_ISONIAZID = "RIFAMPICIN AND ISONIAZID";
        String OTHER_NON_CODED = "OTHER NON-CODED";
        String RIFAMPICIN = "RIFAMPICIN";
        String ETHAMBUTOL_AND_ISONIZAID = "ETHAMBUTOL AND ISONIZAID";
        String COMPLETED = "COMPLETED";
        String RIFAMPICIN_ISONIAZID_AND_ETHAMBUTOL = "RIFAMPICIN ISONIAZID AND ETHAMBUTOL";
        String YES = "YES";
        String PEDIATRIC_RIFAMPICIN_ISONIAZID_AND_PYRAZINAMIDE = "PEDIATRIC RIFAMPICIN ISONIAZID AND PYRAZINAMIDE";
        String PEDIATRIC_RIFAMPICIN_AND_ISONIAZID = "PEDIATRIC RIFAMPICIN AND ISONIAZID";
        String RIFABUTIN = "RIFABUTIN";
//        String MULTIDRUG_RESISTANT_TUBERCULOSIS_REGIMEN = "MULTIDRUG-RESISTANT TUBERCULOSIS REGIMEN";

        parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(PATIENT_REPORTED_CURRENT_TUBERCULOSIS_TREATMENT));
        parameters.put(EvaluableConstants.OBS_VALUE_CODED,
                Arrays.asList(PYRAZINAMIDE, STREPTOMYCIN, RIFAMPICIN_ISONIAZID_PYRAZINAMIDE_AND_ETHAMBUTOL,
                        ETHAMBUTOL, RIFAMPICIN_ISONIAZID_AND_PYRAZINAMIDE, ISONIAZID, RIFAMPICIN_AND_ISONIAZID,
                        OTHER_NON_CODED, RIFAMPICIN, ETHAMBUTOL_AND_ISONIZAID, COMPLETED,
                        RIFAMPICIN_ISONIAZID_AND_ETHAMBUTOL, YES, PEDIATRIC_RIFAMPICIN_ISONIAZID_AND_PYRAZINAMIDE,
                        PEDIATRIC_RIFAMPICIN_AND_ISONIAZID, RIFABUTIN, MULTIDRUG_RESISTANT_TUBERCULOSIS_REGIMEN));
        tbHistoryResults = obsWithRestrictionRule.eval(context, patientId, parameters);
        if (!tbHistoryResults.isEmpty()) {
            return new Result(Boolean.TRUE);
        }

        String PROBLEM_ADDED = "PROBLEM ADDED";
        String TUBERCULOSIS_MENINGITIS = "TUBERCULOSIS MENINGITIS";
        String TUBERCULOSIS = "TUBERCULOSIS";
        String TUBERCULOSIS_MILIARY = "TUBERCULOSIS, MILIARY";
        String SKELETAL_TUBERCULOSIS = "SKELETAL TUBERCULOSIS";
        String ABDOMINAL_TUBERCULOSIS = "ABDOMINAL TUBERCULOSIS";
        String LYMPH_NODE_TUBERCULOSIS = "LYMPH NODE TUBERCULOSIS";
        String PNEUMONIA_TUBERCULOUS = "PNEUMONIA, TUBERCULOUS";

        parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(PROBLEM_ADDED));
        parameters.put(EvaluableConstants.OBS_VALUE_CODED,
                Arrays.asList(TUBERCULOSIS_MENINGITIS, TUBERCULOSIS, TUBERCULOSIS_MILIARY, SKELETAL_TUBERCULOSIS,
                        ABDOMINAL_TUBERCULOSIS, LYMPH_NODE_TUBERCULOSIS, PNEUMONIA_TUBERCULOUS));
        tbHistoryResults = obsWithRestrictionRule.eval(context, patientId, parameters);
        if (!tbHistoryResults.isEmpty()) {
            return new Result(Boolean.TRUE);
        }

        String REVIEW_OF_SYSTEMS_CARDIOPULMONARY = "REVIEW OF SYSTEMS, CARDIOPULMONARY";
//        String TUBERCULOSIS = "TUBERCULOSIS";

        parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(REVIEW_OF_SYSTEMS_CARDIOPULMONARY));
        parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList(TUBERCULOSIS));
        tbHistoryResults = obsWithRestrictionRule.eval(context, patientId, parameters);
        if (!tbHistoryResults.isEmpty()) {
            return new Result(Boolean.TRUE);
        }

        // 2b. Rule out patient-reported history of TB treatment
        String TUBERCULOSIS_TREATMENT_STATUS = "TUBERCULOSIS TREATMENT STATUS";
        String TREATMENT_COMPLETED = "TREATMENT COMPLETED";
        String CURRENTLY_ON_TREATMENT = "CURRENTLY ON TREATMENT";
        String PATIENT_DEFAULTED = "PATIENT DEFAULTED";

        parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(TUBERCULOSIS_TREATMENT_STATUS));
        parameters.put(EvaluableConstants.OBS_VALUE_CODED,
                Arrays.asList(TREATMENT_COMPLETED, CURRENTLY_ON_TREATMENT, PATIENT_DEFAULTED));
        tbHistoryResults = obsWithRestrictionRule.eval(context, patientId, parameters);
        if (!tbHistoryResults.isEmpty()) {
            return new Result(Boolean.TRUE);
        }

        String TUBERCULOSIS_TREATMENT_STARTED = "TUBERCULOSIS TREATMENT STARTED";

        parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(TUBERCULOSIS_TREATMENT_STARTED));
        parameters.remove(EvaluableConstants.OBS_VALUE_CODED);
        tbHistoryResults = obsWithRestrictionRule.eval(context, patientId, parameters);
        if (!tbHistoryResults.isEmpty()) {
            return new Result(Boolean.TRUE);
        }

        String REFERRALS_ORDERED = "REFERRALS ORDERED";
        String TUBERCULOSIS_TREATMENT_OR_DOT_PROGRAM = "TUBERCULOSIS TREATMENT OR DOT PROGRAM";
        String TB_CLINIC = "TB CLINIC";

        parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(REFERRALS_ORDERED));
        parameters.put(EvaluableConstants.OBS_VALUE_CODED,
                Arrays.asList(TUBERCULOSIS_TREATMENT_OR_DOT_PROGRAM, TB_CLINIC));
        tbHistoryResults = obsWithRestrictionRule.eval(context, patientId, parameters);
        if (!tbHistoryResults.isEmpty()) {
            return new Result(Boolean.TRUE);
        }

        String REASON_TUBERCULOSIS_TREATMENT_STARTED = "REASON TUBERCULOSIS  TREATMENT STARTED";

        parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(REASON_TUBERCULOSIS_TREATMENT_STARTED));
        parameters.remove(EvaluableConstants.OBS_VALUE_CODED);
        tbHistoryResults = obsWithRestrictionRule.eval(context, patientId, parameters);
        if (!tbHistoryResults.isEmpty()) {
            return new Result(Boolean.TRUE);
        }

        String REASON_TUBERCULOSIS_PROPHYLAXIS_STOPPED = "REASON TUBERCULOSIS PROPHYLAXIS STOPPED";
//        String TUBERCULOSIS = "TUBERCULOSIS";

        parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(REASON_TUBERCULOSIS_PROPHYLAXIS_STOPPED));
        parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList(TUBERCULOSIS));
        tbHistoryResults = obsWithRestrictionRule.eval(context, patientId, parameters);
        if (!tbHistoryResults.isEmpty()) {
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
