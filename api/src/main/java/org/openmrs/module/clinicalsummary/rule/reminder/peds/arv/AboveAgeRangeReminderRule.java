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

package org.openmrs.module.clinicalsummary.rule.reminder.peds.arv;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
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
import org.openmrs.module.clinicalsummary.rule.medication.AntiRetroViralRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.reminder.peds.fragment.ValidElisaRule;
import org.openmrs.module.clinicalsummary.rule.reminder.peds.fragment.ValidPolymeraseRule;
import org.openmrs.module.clinicalsummary.rule.reminder.peds.fragment.ValidRapidElisaRule;
import org.openmrs.module.clinicalsummary.rule.util.ResultUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Parameters: <ul> <li>[Optional] encounterType: list of all applicable encounter types </ul>
 */
public class AboveAgeRangeReminderRule extends EvaluableRule {

	private static final Log log = LogFactory.getLog(AboveAgeRangeReminderRule.class);

	public static final String TOKEN = "Above Age Range ARV Reminder";

	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
	 */
	@Override
	protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) throws LogicException {
		Result result = new Result();

		Patient patient = Context.getPatientService().getPatient(patientId);

		if (patient.getBirthdate() != null) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(patient.getBirthdate());

			calendar.add(Calendar.YEAR, 5);

			if (calendar.getTime().before(new Date())) {

				parameters.put(EvaluableConstants.OBS_FETCH_SIZE, 1);
				parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList(EvaluableNameConstants.POSITIVE));

				ValidPolymeraseRule validPolymeraseRule = new ValidPolymeraseRule();
				parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.HIV_DNA_POLYMERASE_CHAIN_REACTION_QUALITATIVE));
				Result validPolymeraseResults = validPolymeraseRule.eval(context, patientId, parameters);

				ValidElisaRule validElisaRule = new ValidElisaRule();
				parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.HIV_ENZYME_IMMUNOASSAY_QUALITATIVE));
				Result validElisaResults = validElisaRule.eval(context, patientId, parameters);

				ValidRapidElisaRule validRapidElisaRule = new ValidRapidElisaRule();
				parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.HIV_RAPID_TEST_QUALITATIVE));
				Result validRapidElisaResults = validRapidElisaRule.eval(context, patientId, parameters);

				if (CollectionUtils.isNotEmpty(validPolymeraseResults) || CollectionUtils.isNotEmpty(validElisaResults) || CollectionUtils.isNotEmpty(validRapidElisaResults)) {

					AntiRetroViralRule antiRetroViralRule = new AntiRetroViralRule();
					parameters.put(EvaluableConstants.ENCOUNTER_TYPE, Arrays.asList(EvaluableNameConstants.ENCOUNTER_TYPE_PEDIATRIC_INITIAL,
							EvaluableNameConstants.ENCOUNTER_TYPE_PEDIATRIC_RETURN, EvaluableNameConstants.ENCOUNTER_TYPE_PEDIATRIC_NONCLINICALMEDICATION));
					Result antiRetroViralResults = antiRetroViralRule.eval(context, patientId, parameters);

					log.info("Registered ARV: " + antiRetroViralResults);

					if (CollectionUtils.isEmpty(antiRetroViralResults)) {

						ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();

						parameters.remove(EvaluableConstants.OBS_VALUE_CODED);
						parameters.put(EvaluableConstants.OBS_FETCH_SIZE, 1);

						parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.CD4_PERCENT));
						Result percentageResults = obsWithRestrictionRule.eval(context, patientId, parameters);

						parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.PEDS_WHO_CATEGORY_QUERY));
						Result pedsStageResults = obsWithRestrictionRule.eval(context, patientId, parameters);

						if (CollectionUtils.isNotEmpty(percentageResults) || CollectionUtils.isNotEmpty(pedsStageResults)) {

							List<String> reminderFragments = new ArrayList<String>();
							if (CollectionUtils.isNotEmpty(validPolymeraseResults))
								reminderFragments.add("positive PCR");

							if (CollectionUtils.isNotEmpty(validElisaResults))
								reminderFragments.add("positive Elisa");

							if (CollectionUtils.isNotEmpty(validRapidElisaResults))
								reminderFragments.add("positive Rapid Elisa");

							if (CollectionUtils.isNotEmpty(percentageResults) && percentageResults.toNumber() < 25)
								reminderFragments.add("CD4 Percent &lt 25");

							if (CollectionUtils.isNotEmpty(pedsStageResults) && NumberUtils.toInt(ResultUtils.stripToDigit(pedsStageResults.toString())) > 2)
								reminderFragments.add(pedsStageResults.toString());

							String reminder = "Consider starting ARV Meds. Pt above 5 yrs with " + StringUtils.join(reminderFragments, ", ");
							result.add(new Result(reminder));
						}
					}
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
