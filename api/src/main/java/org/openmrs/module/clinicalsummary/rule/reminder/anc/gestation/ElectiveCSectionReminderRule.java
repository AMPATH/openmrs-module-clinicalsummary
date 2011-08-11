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

package org.openmrs.module.clinicalsummary.rule.reminder.anc.gestation;

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
import java.util.Date;
import java.util.Map;

public class ElectiveCSectionReminderRule extends EvaluableRule {

	private static final Log log = LogFactory.getLog(ElectiveCSectionReminderRule.class);

	public static final String TOKEN = "Elective C Section Reminder";

	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
	 */
	@Override
	protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) throws LogicException {

		Result result = new Result();

		PregnancyDateRule pregnancyDateRule = new PregnancyDateRule();
		Result pregnancyDateResults = pregnancyDateRule.eval(context, patientId, parameters);
		// you can't do anything if you can't find the pregnancy start date
		if (CollectionUtils.isNotEmpty(pregnancyDateResults)) {
			Result pregnancyDateResult = pregnancyDateResults.latest();
			Date pregnancyDate = pregnancyDateResult.toDatetime();
			// prepare the default parameters for the rule executions
			ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();
			// calculate the gestational age based on the pregnancy start date
			Long gestationalAge = GestationalReminderUtils.calculateGestationalAge(patientId, pregnancyDate);
			// get the preferred method of delivery
			parameters.put(EvaluableConstants.OBS_FETCH_SIZE, 1);
			parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.PREFERRED_MODE_OF_DELIVERY));
			Result preferredDeliveryResults = obsWithRestrictionRule.eval(context, patientId, parameters);
			if (gestationalAge >= 37 && CollectionUtils.isNotEmpty(preferredDeliveryResults)) {
				// preferred delivery is by caesarean section
				Result preferredDeliveryResult = preferredDeliveryResults.latest();
				Concept caesareanConcept = CacheUtils.getConcept(EvaluableNameConstants.ELECTIVE_CAESAREAN_SECTION);
				if (OpenmrsUtil.nullSafeEquals(preferredDeliveryResult.toConcept(), caesareanConcept)) {
					Boolean displayReminder = Boolean.TRUE;
					// check the referrals ordered if no result display reminder
					parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.REFERRALS_ORDERED));
					Result referralResults = obsWithRestrictionRule.eval(context, patientId, parameters);
					if (CollectionUtils.isNotEmpty(referralResults)) {
						Result referralResult = referralResults.latest();
						Concept obstetricConcept = CacheUtils.getConcept(EvaluableNameConstants.OBSTETRICS);
						if (OpenmrsUtil.nullSafeEquals(referralResult.toConcept(), obstetricConcept))
							displayReminder = Boolean.FALSE;
					}
					// display the reminder
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
