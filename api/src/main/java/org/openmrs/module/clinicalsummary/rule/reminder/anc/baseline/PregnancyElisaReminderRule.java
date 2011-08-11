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

package org.openmrs.module.clinicalsummary.rule.reminder.anc.baseline;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.cache.CacheUtils;
import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableNameConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.reminder.ReminderParameters;
import org.openmrs.module.clinicalsummary.rule.reminder.anc.common.PregnancyDateRule;
import org.openmrs.util.OpenmrsUtil;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class PregnancyElisaReminderRule extends EvaluableRule {

	private static final Log log = LogFactory.getLog(PregnancyElisaReminderRule.class);

	public static final String TOKEN = "Pregnancy Elisa Reminder";

	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
	 */
	@Override
	protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) throws LogicException {

		Result result = new Result();

		PregnancyDateRule pregnancyDateRule = new PregnancyDateRule();
		Result pregnancyDateResults = pregnancyDateRule.eval(context, patientId, parameters);
		if (CollectionUtils.isNotEmpty(pregnancyDateResults)) {
			Result pregnancyDateResult = pregnancyDateResults.latest();
			Date pregnancyDate = pregnancyDateResult.toDatetime();

			ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();
			parameters.put(EvaluableConstants.OBS_FETCH_SIZE, 1);
			List<String> conceptNames = Arrays.asList(EvaluableNameConstants.HIV_ENZYME_IMMUNOASSAY_QUALITATIVE,
					EvaluableNameConstants.HIV_RAPID_TEST_QUALITATIVE);

			// fetch the latest elisa or rapid elisa
			parameters.put(EvaluableConstants.OBS_CONCEPT, conceptNames);

			Boolean elisaExists = Boolean.FALSE;
			Result elisaResults = obsWithRestrictionRule.eval(context, patientId, parameters);
			if (CollectionUtils.isNotEmpty(elisaResults)) {
				Result elisaResult = elisaResults.latest();
				Concept positiveConcept = CacheUtils.getConcept(EvaluableNameConstants.POSITIVE);
				if (OpenmrsUtil.nullSafeEquals(elisaResult.toConcept(), positiveConcept))
					elisaExists = Boolean.TRUE;
				if (elisaResult.getResultDate().after(pregnancyDate))
					elisaExists = Boolean.TRUE;
			}

			if (!elisaExists) {
				parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.TESTS_ORDERED));
				parameters.put(EvaluableConstants.OBS_VALUE_CODED, conceptNames);

				Result testResults = obsWithRestrictionRule.eval(context, patientId, parameters);

				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.MONTH, -1);
				Date lastMonth = calendar.getTime();

				Boolean displayReminder = Boolean.TRUE;

				// if no test or the test was ordered more than one months ago, then show the reminder
				if (CollectionUtils.isEmpty(testResults) || testResults.getResultDate().before(lastMonth))
					result.add(new Result(String.valueOf(parameters.get(ReminderParameters.DISPLAYED_REMINDER_TEXT))));
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
