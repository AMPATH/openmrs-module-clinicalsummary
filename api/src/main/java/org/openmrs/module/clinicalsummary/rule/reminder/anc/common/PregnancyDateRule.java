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

package org.openmrs.module.clinicalsummary.rule.reminder.anc.common;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Obs;
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
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;
import org.openmrs.util.OpenmrsUtil;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PregnancyDateRule extends EvaluableRule {

	private static final Log log = LogFactory.getLog(PregnancyDateRule.class);

	public static final String TOKEN = "Pregnancy Date";

	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
	 */
	@Override
	protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) throws LogicException {
		Result result = new Result();

		Date earliestDate = null;

		EncounterWithRestrictionRule encounterWithRestrictionRule = new EncounterWithStringRestrictionRule();
		Result encounterResults = encounterWithRestrictionRule.eval(context, patientId, parameters);

		if (CollectionUtils.isNotEmpty(encounterResults)) {

			EncounterType encounterType = CacheUtils.getEncounterType(EvaluableNameConstants.ENCOUNTER_TYPE_ANCINITIAL);

			Integer counter = 0;
			Object referenceEncounter = null;
			while (counter < encounterResults.size() && referenceEncounter == null) {
				Encounter encounter = (Encounter) encounterResults.get(counter++).getResultObject();
				if (OpenmrsUtil.nullSafeEquals(encounter.getEncounterType(), encounterType))
					referenceEncounter = encounter;
			}

			if (log.isDebugEnabled())
				log.debug("Can't find any reference encounter. Using earliest for the calculation");

			if (referenceEncounter == null)
				referenceEncounter = encounterResults.earliest().getResultObject();

			Concept confinementConcept = CacheUtils.getConcept(EvaluableNameConstants.ESTIMATED_DATE_OF_CONFINEMENT);
			Concept menstrualConcept = CacheUtils.getConcept(EvaluableNameConstants.LAST_MENSTRUAL_PERIOD_DATE);

			Date confinementDate = null;
			Date menstrualDate = null;

			ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();

			parameters.put(EvaluableConstants.OBS_ENCOUNTER, Arrays.asList(referenceEncounter));
			parameters.put(EvaluableConstants.OBS_CONCEPT,
					Arrays.asList(EvaluableNameConstants.ESTIMATED_DATE_OF_CONFINEMENT, EvaluableNameConstants.LAST_MENSTRUAL_PERIOD_DATE));
			Result pregnancyResults = obsWithRestrictionRule.eval(context, patientId, parameters);

			for (Result pregnancyResult : pregnancyResults) {
				Obs obs = (Obs) pregnancyResult.getResultObject();
				if (OpenmrsUtil.nullSafeEquals(confinementConcept, obs.getConcept())) {
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(obs.getValueDatetime());
					calendar.add(Calendar.DATE, -270);
					confinementDate = calendar.getTime();
				} else if (OpenmrsUtil.nullSafeEquals(menstrualConcept, obs.getConcept())) {
					menstrualDate = obs.getValueDatetime();
				}
			}

			if (OpenmrsUtil.compareWithNullAsGreatest(confinementDate, menstrualDate) < 0)
				result.add(new Result(confinementDate));
			else if (OpenmrsUtil.compareWithNullAsGreatest(confinementDate, menstrualDate) > 0)
				result.add(new Result(menstrualDate));
		}
		return result;
	}

	/**
	 * @see org.openmrs.logic.Rule#getDependencies()
	 */
	@Override
	public String[] getDependencies() {
		return new String[]{ObsWithStringRestrictionRule.TOKEN};
	}

	/**
	 * Whether the result of the rule should be cached or not
	 *
	 * @return true if the system should put the result into the caching system
	 */
	@Override
	protected Boolean cacheable() {
		return Boolean.TRUE;
	}

	/**
	 * Get the definition of each parameter that should be passed to this rule execution
	 *
	 * @return all parameter that applicable for each rule execution
	 */
	@Override
	public Set<EvaluableParameter> getEvaluationParameters() {
		Set<EvaluableParameter> evaluableParameters = new HashSet<EvaluableParameter>();
		evaluableParameters.add(EvaluableConstants.REQUIRED_ENCOUNTER_TYPE_PARAMETER_DEFINITION);
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
