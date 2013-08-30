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

import org.openmrs.Encounter;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.enumeration.FetchOrdering;
import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableNameConstants;
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
public class ProphylaxisHistoryExistsRule extends EvaluableRule {

    public static final String TOKEN = "Tuberculosis:Prophylaxis History Exists";

    /**
     * @param context
     * @param patientId
     * @param parameters
     * @return
     * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
     */
    @Override
    protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) {
        Result prophylaxisHistory = new Result();

        ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();
        parameters.put(EvaluableConstants.OBS_FETCH_SIZE, 1);

        EncounterWithRestrictionRule encounterWithRestrictionRule = new EncounterWithStringRestrictionRule();
        parameters.put(EvaluableConstants.ENCOUNTER_TYPE,
                Arrays.asList(EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_INITIAL,
                        EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_RETURN));
        parameters.put(EvaluableConstants.ENCOUNTER_FETCH_SIZE, 1);
        parameters.put(EvaluableConstants.ENCOUNTER_FETCH_ORDER, FetchOrdering.ORDER_DESCENDING.getValue());
        Result hivEncounters = encounterWithRestrictionRule.eval(context, patientId, parameters);
        if (!hivEncounters.isEmpty()) {
            Result hivEncounter = hivEncounters.get(0);
            Encounter encounter = (Encounter) hivEncounter.getResultObject();

            String CURRENT_MEDICATIONS = "CURRENT MEDICATIONS";
            String ISONIAZID = "ISONIAZID";
            String RIFAMPICIN_ISONIAZID_AND_PYRAZINAMIDE = "RIFAMPICIN ISONIAZID AND PYRAZINAMIDE";
            String RIFAMPICIN_ISONIAZID_PYRAZINAMIDE_AND_ETHAMBUTOL = "RIFAMPICIN ISONIAZID PYRAZINAMIDE AND ETHAMBUTOL";
            String RIFAMPICIN_AND_ISONIAZID = "RIFAMPICIN AND ISONIAZID";
            String RIFAMPICIN_ISONIAZID_AND_ETHAMBUTOL = "RIFAMPICIN ISONIAZID AND ETHAMBUTOL";
            String ETHAMBUTOL_AND_ISONIZAID = "ETHAMBUTOL AND ISONIZAID";

            parameters.put(EvaluableConstants.OBS_ENCOUNTER, Arrays.asList(encounter));
            parameters.put(EvaluableConstants.OBS_VALUE_CODED,
                    Arrays.asList(ISONIAZID, RIFAMPICIN_ISONIAZID_AND_PYRAZINAMIDE,
                            RIFAMPICIN_ISONIAZID_PYRAZINAMIDE_AND_ETHAMBUTOL, RIFAMPICIN_AND_ISONIAZID,
                            RIFAMPICIN_ISONIAZID_AND_ETHAMBUTOL, ETHAMBUTOL_AND_ISONIZAID));
            parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(CURRENT_MEDICATIONS));
            prophylaxisHistory = obsWithRestrictionRule.eval(context, patientId, parameters);
            if (!prophylaxisHistory.isEmpty()) {
                return new Result(Boolean.TRUE);
            }
        }

        String PATIENT_REPORTED_CURRENT_TUBERCULOSIS_PROPHYLAXIS = "PATIENT REPORTED CURRENT TUBERCULOSIS PROPHYLAXIS";
        String ISONIAZID = "ISONIAZID";

        parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList(ISONIAZID));
        parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(PATIENT_REPORTED_CURRENT_TUBERCULOSIS_PROPHYLAXIS));
        prophylaxisHistory = obsWithRestrictionRule.eval(context, patientId, parameters);
        if (!prophylaxisHistory.isEmpty()) {
            return new Result(Boolean.TRUE);
        }

        String TUBERCULOSIS_PROPHYLAXIS_START_DATE = "TUBERCULOSIS PROPHYLAXIS START DATE";

        parameters.remove(EvaluableConstants.OBS_VALUE_CODED);
        parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(TUBERCULOSIS_PROPHYLAXIS_START_DATE));
        prophylaxisHistory = obsWithRestrictionRule.eval(context, patientId, parameters);
        if (!prophylaxisHistory.isEmpty()) {
            return new Result(Boolean.TRUE);
        }

        String TUBERCULOSIS_PROPHYLAXIS_PLAN = "TUBERCULOSIS PROPHYLAXIS PLAN";

        parameters.remove(EvaluableConstants.OBS_VALUE_CODED);
        parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(TUBERCULOSIS_PROPHYLAXIS_PLAN));
        prophylaxisHistory = obsWithRestrictionRule.eval(context, patientId, parameters);
        if (!prophylaxisHistory.isEmpty()) {
            return new Result(Boolean.TRUE);
        }

        String REASON_TUBERCULOSIS_PROPHYLAXIS_STOPPED = "REASON TUBERCULOSIS PROPHYLAXIS STOPPED";

        parameters.remove(EvaluableConstants.OBS_VALUE_CODED);
        parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(REASON_TUBERCULOSIS_PROPHYLAXIS_STOPPED));
        prophylaxisHistory = obsWithRestrictionRule.eval(context, patientId, parameters);
        if (!prophylaxisHistory.isEmpty()) {
            return new Result(Boolean.TRUE);
        }

        String TUBERCULOSIS_PROPHYLAXIS_ADHERENCE_IN_PAST_WEEK = "TUBERCULOSIS PROPHYLAXIS ADHERENCE IN PAST WEEK";

        parameters.remove(EvaluableConstants.OBS_VALUE_CODED);
        parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(TUBERCULOSIS_PROPHYLAXIS_ADHERENCE_IN_PAST_WEEK));
        prophylaxisHistory = obsWithRestrictionRule.eval(context, patientId, parameters);
        if (!prophylaxisHistory.isEmpty()) {
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
