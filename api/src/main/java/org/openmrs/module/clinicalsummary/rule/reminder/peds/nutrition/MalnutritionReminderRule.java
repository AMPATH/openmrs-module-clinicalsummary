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

package org.openmrs.module.clinicalsummary.rule.reminder.peds.nutrition;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableNameConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.reminder.ReminderParameters;
import org.openmrs.module.clinicalsummary.rule.util.ZScoreUtils;

import java.util.Arrays;
import java.util.Map;

/**
 */
public abstract class MalnutritionReminderRule extends EvaluableRule {

	private static final Log log = LogFactory.getLog(MalnutritionReminderRule.class);

	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
	 */
	@Override
	protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) throws LogicException {
		Result result = new Result();

		parameters.put(EvaluableConstants.OBS_FETCH_SIZE, 1);
		parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.WEIGHT_KG));

		ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();
		Result weightResults = obsWithRestrictionRule.eval(context, patientId, parameters);

		Patient patient = Context.getPatientService().getPatient(patientId);

		if (CollectionUtils.isNotEmpty(weightResults)) {
			Result weightResult = weightResults.latest();
			Double zScore = ZScoreUtils.calculateZScore(patient, weightResult.getResultDate(), weightResult.toNumber());
			if (exceedMinimumScore(zScore)) {

				Integer counter = 0;
				parameters.remove(EvaluableConstants.OBS_FETCH_SIZE);
				weightResults = obsWithRestrictionRule.eval(context, patientId, parameters);
				while (counter < weightResults.size() && exceedMinimumScore(zScore)) {
					weightResult = weightResults.get(counter++);
					zScore = ZScoreUtils.calculateZScore(patient, weightResult.getResultDate(), weightResult.toNumber());
				}

				if (exceedMinimumScore(zScore) && acceptedScore(zScore)) {
					// get the latest nutrition referral
					parameters.put(EvaluableConstants.OBS_FETCH_SIZE, 1);
					parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.REFERRALS_ORDERED));
					parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList(EvaluableNameConstants.NUTRITIONAL_SUPPORT));

					Result referralResults = obsWithRestrictionRule.eval(context, patientId, parameters);
					if (CollectionUtils.isEmpty(referralResults)
							|| referralResults.getResultDate().before(weightResult.getResultDate()))
						result.add(new Result(String.valueOf(parameters.get(ReminderParameters.DISPLAYED_REMINDER_TEXT))));
				}
			}
		}

		return result;
	}

	/**
	 * Method to check whether the z-score returned already exceeded the minimum acceptable z-score value. The minimum are set to
	 * 1.5
	 *
	 * @param zScore the z-score value
	 * @return true when the z-score value are less than the minimum value
	 */
	private Boolean exceedMinimumScore(final Double zScore) {
		return (zScore != null && zScore < -1.5);
	}

	/**
	 * Method that will determine whether the z-score value should throw reminder or not
	 *
	 * @param zScore the z-score value
	 * @return true when the z-score will are within the reminded values
	 */
	protected abstract Boolean acceptedScore(final Double zScore);

	/**
	 * @see org.openmrs.logic.Rule#getDependencies()
	 */
	@Override
	public String[] getDependencies() {
		return new String[]{ObsWithStringRestrictionRule.TOKEN};
	}
}
