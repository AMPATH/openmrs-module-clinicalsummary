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

package org.openmrs.module.clinicalsummary.rule.primary;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableParameter;
import org.openmrs.module.clinicalsummary.rule.EvaluableRule;
import org.openmrs.module.clinicalsummary.rule.encounter.EncounterWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;

/**
 */
public class GroupedObservationRule extends EvaluableRule {

    private static final Log log = LogFactory.getLog(GroupedObservationRule.class);

    public static final String TOKEN = "Grouped Observation";

    /**
     * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
     */
    @Override
    protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) throws LogicException {

        Result result = new Result();
        ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();
        Result results = obsWithRestrictionRule.eval(context, patientId, parameters);

        int counter = 0;
        Map<Concept, Result> groupedResult = new HashMap<Concept, Result>();
        while (counter < CollectionUtils.size(results)) {
            Result obsResult = results.get(counter ++);
            Obs obs = (Obs) obsResult.getResultObject();
            Concept concept = obs.getConcept();
            Result resultList = groupedResult.get(concept);
            if (resultList == null) {
                resultList = new Result();
                groupedResult.put(concept, resultList);
            }
            resultList.add(obsResult);
        }

        for (Concept concept : groupedResult.keySet()) {
            result.add(groupedResult.get(concept));
        }

        return result;
    }

    /**
     * @see org.openmrs.logic.Rule#getDependencies()
     */
    @Override
    public String[] getDependencies() {
        return new String[]{ObsWithStringRestrictionRule.TOKEN, EncounterWithStringRestrictionRule.TOKEN};
    }

    /**
     * Get the definition of each parameter that should be passed to this rule execution
     *
     * @return all parameter that applicable for each rule execution
     */
    @Override
    public Set<EvaluableParameter> getEvaluationParameters() {
        Set<EvaluableParameter> evaluableParameters = new HashSet<EvaluableParameter>();
        evaluableParameters.add(EvaluableConstants.REQUIRED_OBS_CONCEPT_PARAMETER_DEFINITION);
        return evaluableParameters;
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
