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

package org.openmrs.module.clinicalsummary.rule.reminder.adult.drug;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableNameConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableRule;
import org.openmrs.module.clinicalsummary.rule.medication.AntiRetroViralRule;
import org.openmrs.module.clinicalsummary.rule.medication.TuberculosisRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.reminder.ReminderParameters;
import org.openmrs.module.clinicalsummary.rule.treatment.TuberculosisTreatmentRule;
import org.openmrs.util.OpenmrsUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RifampinNevirapineReminderRule extends EvaluableRule {

	private static final Log log = LogFactory.getLog(RifampinNevirapineReminderRule.class);

	public static final String TOKEN = "Rifampin Nevirapine Contraindication Reminder";

	@Override
	protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) {
		Result result = new Result();

		AntiRetroViralRule antiRetroViralRule = new AntiRetroViralRule();
		// prepare the encounter types
		parameters.put(EvaluableConstants.ENCOUNTER_TYPE, Arrays.asList(EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_INITIAL,
				EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_RETURN, EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_NONCLINICALMEDICATION));
		Result arvResults = antiRetroViralRule.eval(context, patientId, parameters);

		TuberculosisRule tuberculosisRule = new TuberculosisRule();
		parameters.put(EvaluableConstants.ENCOUNTER_TYPE, Arrays.asList(EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_INITIAL,
				EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_RETURN, EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_NONCLINICALMEDICATION));
		Result tbResults = tuberculosisRule.eval(context, patientId, parameters);

		TuberculosisTreatmentRule tuberculosisTreatmentRule = new TuberculosisTreatmentRule();
		parameters.put(EvaluableConstants.ENCOUNTER_TYPE, Arrays.asList(EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_INITIAL,
				EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_RETURN, EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_NONCLINICALMEDICATION));
		Result tbTreatmentResults = tuberculosisTreatmentRule.eval(context, patientId, parameters);

		List<Concept> nevirapineConcepts = Context.getConceptService().getConceptsByName(EvaluableNameConstants.NEVIRAPINE);
		List<Concept> rifampicinConcepts = Context.getConceptService().getConceptsByName(EvaluableNameConstants.RIFAMPICIN);

		Boolean rifampicinExists = Boolean.FALSE;
		Boolean nevirapineExists = Boolean.FALSE;

		Integer tbTreatmentCounter = 0;
		while (tbTreatmentCounter < tbTreatmentResults.size() && !rifampicinExists) {
			Integer conceptCounter = 0;
			Result tbTreatmentResult = tbTreatmentResults.get(tbTreatmentCounter++);
			while (conceptCounter < rifampicinConcepts.size() && !rifampicinExists) {
				Concept rifampicinConcept = rifampicinConcepts.get(conceptCounter++);
				if (OpenmrsUtil.nullSafeEquals(tbTreatmentResult.toConcept(), rifampicinConcept))
					rifampicinExists = Boolean.TRUE;
			}
		}

		Integer tbCounter = 0;
		while (tbCounter < tbResults.size() && !rifampicinExists) {
			Integer conceptCounter = 0;
			Result tbResult = tbResults.get(tbCounter++);
			while (conceptCounter < rifampicinConcepts.size() && !rifampicinExists) {
				Concept rifampicinConcept = rifampicinConcepts.get(conceptCounter++);
				if (OpenmrsUtil.nullSafeEquals(tbResult.toConcept(), rifampicinConcept))
					rifampicinExists = Boolean.TRUE;
			}
		}

		Integer arvCounter = 0;
		while (arvCounter < arvResults.size() && !nevirapineExists) {
			Integer conceptCounter = 0;
			Result arvResult = arvResults.get(arvCounter++);
			while (conceptCounter < nevirapineConcepts.size() && !nevirapineExists) {
				Concept nevirapineConcept = nevirapineConcepts.get(conceptCounter++);
				if (OpenmrsUtil.nullSafeEquals(arvResult.toConcept(), nevirapineConcept))
					nevirapineExists = Boolean.TRUE;
			}
		}

		if (rifampicinExists && nevirapineExists)
			result.add(new Result(String.valueOf(parameters.get(ReminderParameters.DISPLAYED_REMINDER_TEXT))));

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
