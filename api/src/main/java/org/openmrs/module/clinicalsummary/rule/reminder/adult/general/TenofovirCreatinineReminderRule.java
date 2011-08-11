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
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableNameConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableRule;
import org.openmrs.module.clinicalsummary.rule.medication.AntiRetroViralRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.reminder.ReminderParameters;
import org.openmrs.util.OpenmrsUtil;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class TenofovirCreatinineReminderRule extends EvaluableRule {

	private static final Log log = LogFactory.getLog(TenofovirCreatinineReminderRule.class);

	public static final String TOKEN = "Tenofovir Creatinine Monitoring Reminder";

	@Override
	protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) {
		Result result = new Result();

		ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();
		parameters.put(EvaluableConstants.OBS_FETCH_SIZE, 1);
		parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.ANTIRETROVIRAL_PLAN));
		parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList(EvaluableNameConstants.START_DRUGS,
				EvaluableNameConstants.CHANGE_FORMULATION, EvaluableNameConstants.DRUG_RESTART, EvaluableNameConstants.DRUG_SUBSTITUTION));

		Result planResults = obsWithRestrictionRule.eval(context, patientId, parameters);
		if (CollectionUtils.isNotEmpty(planResults)) {
			Result planResult = planResults.latest();
			Obs obs = (Obs) planResult.getResultObject();

			parameters.put(EvaluableConstants.OBS_ENCOUNTER, Arrays.asList(obs.getEncounter()));
			AntiRetroViralRule antiRetroViralRule = new AntiRetroViralRule();
			Result antiRetroViralResults = antiRetroViralRule.eval(context, patientId, parameters);
			if (CollectionUtils.isNotEmpty(antiRetroViralResults)) {
				List<Concept> tenofovirConcepts = Context.getConceptService().getConceptsByName(EvaluableNameConstants.TENOFOVIR);

				Integer resultCounter = 0;
				Result tenofovirResult = null;
				while (resultCounter < antiRetroViralResults.size() && tenofovirResult == null) {
					Result antiRetroViralResult = antiRetroViralResults.get(resultCounter++);
					Integer conceptCounter = 0;
					while (conceptCounter < tenofovirConcepts.size() && tenofovirResult == null) {
						Concept tenofovirConcept = tenofovirConcepts.get(conceptCounter++);
						if (OpenmrsUtil.nullSafeEquals(antiRetroViralResult.toConcept(), tenofovirConcept))
							tenofovirResult = antiRetroViralResult;
					}
				}

				if (tenofovirResult != null) {
					Calendar calendar = Calendar.getInstance();
					// one year ago date object
					calendar.add(Calendar.MONTH, -12);
					Date oneYearAgo = calendar.getTime();
					// create six months ago date object
					calendar.add(Calendar.MONTH, 6);
					Date sixMonths = calendar.getTime();

					calendar.setTime(tenofovirResult.getResultDate());
					// one year after tenofovir start date
					calendar.add(Calendar.MONTH, 12);
					Date oneYearAfterTenofovir = calendar.getTime();
					// six months after tenofovir start date
					calendar.add(Calendar.MONTH, -6);
					Date sixMonthsAfterTenofovir = calendar.getTime();

					// search for any creatinine result
					parameters.remove(EvaluableConstants.OBS_FETCH_SIZE);
					parameters.remove(EvaluableConstants.OBS_VALUE_CODED);
					// search for creatinine and test ordered
					parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.SERUM_CREATININE));
					// result of creatinine searching
					Result creatinineResults = obsWithRestrictionRule.eval(context, patientId, parameters);

					// search for tests ordered for creatinine
					parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.TESTS_ORDERED));
					parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList(EvaluableNameConstants.SERUM_CREATININE,
							EvaluableNameConstants.SERUM_ELECTROLYTES, EvaluableNameConstants.CHEMISTRY_LAB_TESTS));
					// result of the tests ordered for creatinine
					Result testResults = obsWithRestrictionRule.eval(context, patientId, parameters);

					Boolean afterSixMonthsExists = Boolean.FALSE;
					Boolean afterOneYearExists = Boolean.FALSE;

					Integer creatinineCounter = 0;
					while (creatinineCounter < creatinineResults.size()) {
						Result creatinineResult = creatinineResults.get(creatinineCounter++);
						if (creatinineResult.getResultDate().after(sixMonthsAfterTenofovir))
							afterSixMonthsExists = Boolean.TRUE;
						if (creatinineResult.getResultDate().after(oneYearAfterTenofovir))
							afterOneYearExists = Boolean.TRUE;
					}

					Integer testCounter = 0;
					while (testCounter < testResults.size()) {
						Result testResult = testResults.get(testCounter++);
						if (testResult.getResultDate().after(sixMonthsAfterTenofovir))
							afterSixMonthsExists = Boolean.TRUE;
						if (testResult.getResultDate().after(oneYearAfterTenofovir))
							afterOneYearExists = Boolean.TRUE;
					}

					Boolean displayReminder = Boolean.FALSE;
					Date tenofovirDate = tenofovirResult.getResultDate();
					if ((tenofovirDate.before(sixMonths) && tenofovirDate.after(oneYearAgo) && !afterSixMonthsExists)
							|| (tenofovirDate.before(oneYearAgo) && !afterOneYearExists))
						displayReminder = Boolean.TRUE;

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
