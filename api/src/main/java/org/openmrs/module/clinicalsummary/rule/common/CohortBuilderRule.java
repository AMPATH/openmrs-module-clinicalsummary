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

package org.openmrs.module.clinicalsummary.rule.common;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableNameConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableRule;
import org.openmrs.module.clinicalsummary.rule.encounter.EncounterWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.encounter.EncounterWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.util.RuleUtils;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class CohortBuilderRule extends EvaluableRule {

	private static final Log log = LogFactory.getLog(CohortBuilderRule.class);

	public static final String TOKEN = "Cohort Builder";

	public static final String ENCOUNTER_LOCATION_MTRH_MODULE_4 = "MTRH Module 4";

	public static final String ENCOUNTER_LOCATION_TURBO = "Turbo";

	public static final String ENCOUNTER_LOCATION_KITALE = "Kitale";

	public static final String ENCOUNTER_LOCATION_WEBUYE = "Webuye";

	public static final String DATE_CUTOFF_POINT = "01/01/2010";

	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
	 */
	@Override
	protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) {
		Result result = new Result();

		String dateString = DATE_CUTOFF_POINT;
		Date initialDate = RuleUtils.parse(dateString, null);
		if (initialDate != null) {

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(initialDate);
			calendar.add(Calendar.MONTH, 5);
			Date cutoffDate = calendar.getTime();

			EncounterWithRestrictionRule encounterWithRestrictionRule = new EncounterWithStringRestrictionRule();

			parameters.put(EvaluableConstants.ENCOUNTER_FETCH_SIZE, 1);
			parameters.put(EvaluableConstants.ENCOUNTER_LOCATION, Arrays.asList(ENCOUNTER_LOCATION_MTRH_MODULE_4,
					ENCOUNTER_LOCATION_TURBO, ENCOUNTER_LOCATION_KITALE, ENCOUNTER_LOCATION_WEBUYE));
			Result encounterResults = encounterWithRestrictionRule.eval(context, patientId, parameters);

			if (CollectionUtils.isNotEmpty(encounterResults)
					&& encounterResults.getResultDate().after(cutoffDate)) {

				Result encounterResult = encounterResults.latest();
				Encounter encounter = (Encounter) encounterResult.getResultObject();
				Location encounterLocation = encounter.getLocation();

				Patient patient = Context.getPatientService().getPatient(patientId);

				Integer age = patient.getAge();
				if (age != null && age >= 6 && age <= 14) {

					Result obsResult = null;

					ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();

					parameters.put(EvaluableConstants.OBS_FETCH_SIZE, 1);

					parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.PROBLEM_ADDED));
					parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList(EvaluableNameConstants.HIV_INFECTED));
					Result problemAddedResults = obsWithRestrictionRule.eval(context, patientId, parameters);
					if (CollectionUtils.isNotEmpty(problemAddedResults))
						obsResult = problemAddedResults.latest();

					parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.HIV_TEST_RESULT_THIS_VISIT));
					parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList(EvaluableNameConstants.POSITIVE));
					Result hivTestResults = obsWithRestrictionRule.eval(context, patientId, parameters);
					if (CollectionUtils.isNotEmpty(hivTestResults))
						obsResult = hivTestResults.latest();

					parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.HIV_ENZYME_IMMUNOASSAY_QUALITATIVE));
					parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList(EvaluableNameConstants.POSITIVE));
					Result elisaResults = obsWithRestrictionRule.eval(context, patientId, parameters);
					if (CollectionUtils.isNotEmpty(elisaResults))
						obsResult = elisaResults.latest();

					parameters.put(EvaluableConstants.OBS_FETCH_SIZE, 2);

					parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.HIV_DNA_POLYMERASE_CHAIN_REACTION_QUALITATIVE));
					parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList(EvaluableNameConstants.POSITIVE));
					Result polymeraseResults = obsWithRestrictionRule.eval(context, patientId, parameters);
					if (CollectionUtils.size(polymeraseResults) >= 2)
						obsResult = polymeraseResults.latest();

					if (obsResult != null) {
						Obs obs = (Obs) obsResult.getResultObject();
						result.add(new Result(encounterResult.getResultDate(), null, null, obs.getConcept(), obs.getObsDatetime(),
								obs.getObsId().doubleValue(), encounter.getLocation().getName(), obs.getValueCoded()));
					}
				}
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
