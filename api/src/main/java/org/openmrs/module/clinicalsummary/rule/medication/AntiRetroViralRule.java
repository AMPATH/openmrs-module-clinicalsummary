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

package org.openmrs.module.clinicalsummary.rule.medication;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptSet;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.cache.CacheUtils;
import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableNameConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableParameter;
import org.openmrs.module.clinicalsummary.rule.EvaluableRule;
import org.openmrs.module.clinicalsummary.rule.encounter.EncounterWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.encounter.EncounterWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithObjectRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;
import org.openmrs.util.OpenmrsUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Parameters: <ul> <li>[Optional] encounterType: list of all applicable encounter types </ul>
 */
public class AntiRetroViralRule extends EvaluableRule {

	private static final Log log = LogFactory.getLog(AntiRetroViralRule.class);

	public static final String TOKEN = "Anti Retro Viral Medications";

	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
	 */
	@Override
	protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) throws LogicException {
		Result result = new Result();
		// check if the caller already pass encounter list object in the parameter
		Object encounters = parameters.get(EvaluableConstants.OBS_ENCOUNTER);
		if (encounters == null) {
			parameters.put(EvaluableConstants.ENCOUNTER_FETCH_SIZE, 1);
			EncounterWithRestrictionRule encounterWithRestrictionRule = new EncounterWithStringRestrictionRule();
			Result encounterResults = encounterWithRestrictionRule.eval(context, patientId, parameters);
			if (CollectionUtils.isNotEmpty(encounterResults)) {
				Result latestResult = encounterResults.latest();
				encounters = Arrays.asList(latestResult.getResultObject());
			}
		}

		// if the caller didn't pass any encounter list and we can't find any encounter in the database, then just ignore the rest of the process
		if (encounters != null) {
			parameters.put(EvaluableConstants.OBS_ENCOUNTER, encounters);
			ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();
			// get the started
			parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.ANTIRETROVIRALS_STARTED));
			Result startedResults = obsWithRestrictionRule.eval(context, patientId, parameters);
			if (CollectionUtils.isNotEmpty(startedResults)) {
				result.addAll(startedResults);
			} else {
				// process the plan when there's no started
				parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.ANTIRETROVIRAL_PLAN));
				parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList(EvaluableNameConstants.CONTINUE_REGIMEN,
						EvaluableNameConstants.REFILLED, EvaluableNameConstants.DOSING_CHANGE));

				Result planResults = obsWithRestrictionRule.eval(context, patientId, parameters);
				if (CollectionUtils.isNotEmpty(planResults)) {

					parameters.remove(EvaluableConstants.OBS_VALUE_CODED);
					parameters.put(EvaluableConstants.OBS_CONCEPT,
							Arrays.asList(EvaluableNameConstants.CURRENT_ANTIRETROVIRAL_DRUGS_USED_FOR_TRANSMISSION_PROPHYLAXIS,
									EvaluableNameConstants.ANTIRETROVIRAL_DRUGS_USED_FOR_TREATMENT,
									EvaluableNameConstants.PATIENT_REPORTED_PAST_ANTIRETROVIRAL_TREATMENT));
					Result initialResults = obsWithRestrictionRule.eval(context, patientId, parameters);
					if (CollectionUtils.isNotEmpty(initialResults))
						result.addAll(initialResults);

					parameters.remove(EvaluableConstants.OBS_VALUE_CODED);
					parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.CURRENT_ANTIRETROVIRAL_DRUGS_USED_FOR_TREATMENT));
					Result reportedResults = obsWithRestrictionRule.eval(context, patientId, parameters);

					Concept noneConcept = CacheUtils.getConcept(EvaluableNameConstants.NONE);

					Integer counter = 0;
					Boolean noneExists = Boolean.FALSE;
					Result validReportedResults = new Result();
					while (counter < reportedResults.size() && !noneExists) {
						Result reportedResult = reportedResults.get(counter++);
						// if we have none, then clear all reported
						if (!OpenmrsUtil.nullSafeEquals(reportedResult.toConcept(), noneConcept))
							validReportedResults.add(reportedResult);

						if (OpenmrsUtil.nullSafeEquals(reportedResult.toConcept(), noneConcept))
							noneExists = Boolean.TRUE;
					}

					if (!noneExists)
						result.addAll(validReportedResults);
				}
			}

			// process the medication added
			Concept addedConcept = CacheUtils.getConcept(EvaluableNameConstants.MEDICATION_ADDED);
			parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(addedConcept));

			Concept antiDrugs = CacheUtils.getConcept(EvaluableNameConstants.ANTIRETROVIRAL_DRUGS);
			Collection<Concept> concepts = new ArrayList<Concept>();
			for (ConceptSet conceptSet : antiDrugs.getConceptSets())
				concepts.add(conceptSet.getConcept());
			parameters.put(EvaluableConstants.OBS_VALUE_CODED, concepts);

			obsWithRestrictionRule = new ObsWithObjectRestrictionRule();
			Result addedResults = obsWithRestrictionRule.eval(context, patientId, parameters);

			result.addAll(addedResults);
		}

		return result;
	}

	/**
	 * @see org.openmrs.logic.Rule#getDependencies()
	 */
	@Override
	public String[] getDependencies () {
		return new String[]{ObsWithStringRestrictionRule.TOKEN, EncounterWithStringRestrictionRule.TOKEN};
	}

	/**
	 * Whether the result of the rule should be cached or not
	 *
	 * @return true if the system should put the result into the caching system
	 */
	@Override
	protected Boolean cacheable () {
		return Boolean.TRUE;
	}

	/**
	 * Get the definition of each parameter that should be passed to this rule execution
	 *
	 * @return all parameter that applicable for each rule execution
	 */
	@Override
	public Set<EvaluableParameter> getEvaluationParameters () {
		Set<EvaluableParameter> evaluableParameters = new HashSet<EvaluableParameter>();
		evaluableParameters.add(EvaluableConstants.OPTIONAL_ENCOUNTER_TYPE_PARAMETER_DEFINITION);
		evaluableParameters.add(EvaluableConstants.OPTIONAL_OBS_ENCOUNTER_PARAMETER_DEFINITION);
		return evaluableParameters;
	}

	/**
	 * Get the token name of the rule that can be used to reference the rule from LogicService
	 *
	 * @return the token name
	 */
	@Override
	protected String getEvaluableToken () {
		return TOKEN;
	}
}
