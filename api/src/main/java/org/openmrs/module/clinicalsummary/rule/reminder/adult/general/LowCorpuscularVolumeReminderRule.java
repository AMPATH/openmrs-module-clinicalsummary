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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableNameConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.reminder.ReminderParameters;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

public class LowCorpuscularVolumeReminderRule extends EvaluableRule {

	private static final Log log = LogFactory.getLog(LowCorpuscularVolumeReminderRule.class);

	public static final String TOKEN = "Low Corpuscular Volume Reminder";

	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
	 */
	@Override
	protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) throws LogicException {

		Result result = new Result();

		ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();
		parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.MEAN_CORPUSCULAR_VOLUME));
		parameters.put(EvaluableConstants.OBS_FETCH_SIZE, 1);
		Result corpuscularResults = obsWithRestrictionRule.eval(context, patientId, parameters);
		if (CollectionUtils.isNotEmpty(corpuscularResults)) {
			Result latestCorpuscularResult = corpuscularResults.latest();
			if (latestCorpuscularResult.toNumber() < 75) {
				parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.HEMOGLOBIN));
				Result hbResults = obsWithRestrictionRule.eval(context, patientId, parameters);
				if (CollectionUtils.isNotEmpty(hbResults)) {
					Result latestHbResult = hbResults.latest();
					if (latestHbResult.toNumber() < 10) {
						parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.MEDICATION_ADDED));
						parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList(EvaluableNameConstants.IRON_SUPPLEMENT));
						Result medicationResults = obsWithRestrictionRule.eval(context, patientId, parameters);
						if (CollectionUtils.isEmpty(medicationResults)) {
							Boolean displayReminder = Boolean.TRUE;
							// see if any of the medication is after the low corpuscular volume
							Integer counter = 0;
							while (counter < CollectionUtils.size(medicationResults) && displayReminder) {
								Result medicationResult = medicationResults.get(counter++);
								if (medicationResult.getResultDate().after(latestCorpuscularResult.getResultDate()))
									displayReminder = Boolean.FALSE;
							}

							StringBuilder buffer = new StringBuilder();
							buffer.append(" Last Hgb low and last MCV: ").append(latestCorpuscularResult.toNumber());

							String resultDatetime = format(latestCorpuscularResult.getResultDate());
							if (StringUtils.isNotEmpty(resultDatetime))
								buffer.append(" on ").append(resultDatetime);

							// no iron supplement found
							if (displayReminder)
								result.add(new Result(String.valueOf(parameters.get(ReminderParameters.DISPLAYED_REMINDER_TEXT)) + buffer.toString()));
						}
					}
				}
			}
		}

		return result;
	}

	/**
	 * Format date to a string according to the date format
	 *
	 * @param date the date
	 * @return string representation of the date
	 */
	private String format(final Date date) {
		if (date == null)
			return StringUtils.EMPTY;
		String format = "dd-MMM-yyyy";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
		return simpleDateFormat.format(date);
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
