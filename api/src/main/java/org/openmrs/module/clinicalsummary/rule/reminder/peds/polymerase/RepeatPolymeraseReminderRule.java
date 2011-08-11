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

package org.openmrs.module.clinicalsummary.rule.reminder.peds.polymerase;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.time.DateUtils;
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
import org.openmrs.module.clinicalsummary.rule.reminder.peds.fragment.ValidPolymeraseRule;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 */
public class RepeatPolymeraseReminderRule extends EvaluableRule {

	private static final Log log = LogFactory.getLog(RepeatPolymeraseReminderRule.class);

	public static final String TOKEN = "Repeat Polymerase Reminder";

	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
	 */
	@Override
	protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) throws LogicException {
		Result result = new Result();

		Patient patient = Context.getPatientService().getPatient(patientId);

		if (patient.getBirthdate() != null) {

			Calendar calendar = Calendar.getInstance();

			// 18 months after birth date
			calendar.setTime(patient.getBirthdate());
			calendar.add(Calendar.MONTH, 18);
			Date eighteenMonths = calendar.getTime();

			// 6 weeks after birth date
			calendar.setTime(patient.getBirthdate());
			calendar.add(Calendar.WEEK_OF_YEAR, 6);
			Date sixWeeks = calendar.getTime();

			if (sixWeeks.before(new Date()) && eighteenMonths.after(new Date())) {
				ValidPolymeraseRule validPolymeraseRule = new ValidPolymeraseRule();
				// only pull the positive and negative results
				parameters.put(EvaluableConstants.OBS_FETCH_SIZE, 2);
				parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList(EvaluableNameConstants.POSITIVE, EvaluableNameConstants.NEGATIVE));
				parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.HIV_DNA_POLYMERASE_CHAIN_REACTION_QUALITATIVE));
				Result validPolymeraseResults = validPolymeraseRule.eval(context, patientId, parameters);

				if (CollectionUtils.isNotEmpty(validPolymeraseResults) && CollectionUtils.size(validPolymeraseResults) < 2) {

					Result validPolymeraseResult = validPolymeraseResults.latest();

					// get the latest test ordered
					parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.TESTS_ORDERED));
					parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList(EvaluableNameConstants
							.HIV_DNA_POLYMERASE_CHAIN_REACTION_QUALITATIVE));

					ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();
					Result testResults = obsWithRestrictionRule.eval(context, patientId, parameters);

					calendar.setTime(patient.getBirthdate());
					calendar.add(Calendar.WEEK_OF_YEAR, 4);
					Date fourWeeks = calendar.getTime();

					calendar.setTime(new Date());
					calendar.add(Calendar.MONTH, -6);
					Date sixMonthsAgo = calendar.getTime();

					int pendingCount = 0;
					for (Result testResult : testResults) {
						// test is valid if it's performed after four weeks and after six months ago and not the same with the date of the result
						if (testResult.getResultDate().after(sixMonthsAgo) && testResult.getResultDate().after(fourWeeks)
								&& !DateUtils.isSameDay(testResult.getResultDate(), validPolymeraseResult.getResultDate()))
							pendingCount++;
					}

					if (pendingCount + validPolymeraseResults.size() < 2)
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
