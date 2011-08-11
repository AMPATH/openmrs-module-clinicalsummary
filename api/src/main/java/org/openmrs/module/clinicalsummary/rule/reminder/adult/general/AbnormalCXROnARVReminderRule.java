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

package org.openmrs.module.clinicalsummary.rule.reminder.adult.general;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableNameConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableRule;
import org.openmrs.module.clinicalsummary.rule.encounter.EncounterWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.encounter.EncounterWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.reminder.ReminderParameters;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;

public class AbnormalCXROnARVReminderRule extends EvaluableRule {

	private static final Log log = LogFactory.getLog(AbnormalCXROnARVReminderRule.class);

	public static final String TOKEN = "Abnormal CXR On Anti Retro Viral Reminder";

	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
	 */
	@Override
	protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) throws LogicException {
		Result result = new Result();

		ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();
		parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList(EvaluableNameConstants.CXR_PULMONARY_EFFUSION, EvaluableNameConstants.CXR_MILIARY_CHANGES,
				EvaluableNameConstants.CXR_INFILTRATE, EvaluableNameConstants.CXR_DIFFUSE_NON_MILIARY_CHANGES, EvaluableNameConstants.CXR_CAVITARY_LESION));
		parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.CXR));
		parameters.put(EvaluableConstants.OBS_FETCH_SIZE, 1);

		Result cxrResults = obsWithRestrictionRule.eval(context, patientId, parameters);
		if (CollectionUtils.isNotEmpty(cxrResults)) {
			Result cxrResult = cxrResults.latest();

			parameters.put(EvaluableConstants.ENCOUNTER_FETCH_SIZE, 1);
			parameters.put(EvaluableConstants.ENCOUNTER_TYPE, Arrays.asList(EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_INITIAL,
					EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_RETURN, EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_NONCLINICALMEDICATION));
			EncounterWithRestrictionRule encounterWithRestrictionRule = new EncounterWithStringRestrictionRule();
			Result encounterResults = encounterWithRestrictionRule.eval(context, patientId, parameters);
			if (CollectionUtils.isNotEmpty(encounterResults)) {
				Result latestResult = encounterResults.latest();
				parameters.put(EvaluableConstants.OBS_ENCOUNTER, Arrays.asList(latestResult.getResultObject()));

				parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.ANTIRETROVIRAL_PLAN));
				parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList(EvaluableNameConstants.START_DRUGS));

				Result planResults = obsWithRestrictionRule.eval(context, patientId, parameters);
				if (CollectionUtils.isNotEmpty(planResults)) {
					Result planResult = planResults.latest();

					Boolean displayReminder = Boolean.TRUE;

					parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.TESTS_ORDERED));
					parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList(EvaluableNameConstants.CXR));
					Result testOrderedResults = obsWithRestrictionRule.eval(context, patientId, parameters);
					if (CollectionUtils.isNotEmpty(testOrderedResults)) {
						Result testOrderedResult = testOrderedResults.latest();
						// check the test ordered date
						Calendar calendar = Calendar.getInstance();
						calendar.add(Calendar.MONTH, -6);
						// test ordered after 6 months ago and comes after the latest cxr result and comes after the plan
						if (testOrderedResult.getResultDate().after(calendar.getTime())
								&& testOrderedResult.getResultDate().after(cxrResult.getResultDate())
								&& testOrderedResult.getResultDate().after(planResult.getResultDate()))
							displayReminder = Boolean.FALSE;
					}

					if (displayReminder)
						result.add(new Result(String.valueOf(parameters.get(ReminderParameters.DISPLAYED_REMINDER_TEXT))));
				}
			}
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
	 * Get the token name of the rule that can be used to reference the rule from LogicService
	 *
	 * @return the token name
	 */
	@Override
	protected String getEvaluableToken() {
		return TOKEN;
	}
}
