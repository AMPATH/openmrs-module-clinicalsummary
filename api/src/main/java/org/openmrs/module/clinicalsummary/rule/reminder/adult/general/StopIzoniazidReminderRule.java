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

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

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
import org.openmrs.module.clinicalsummary.rule.medication.TuberculosisRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.reminder.ReminderParameters;
import org.openmrs.module.clinicalsummary.rule.treatment.TuberculosisTreatmentRule;
import org.openmrs.util.OpenmrsUtil;

public class StopIzoniazidReminderRule extends EvaluableRule {

	private static final Log log = LogFactory.getLog(StopIzoniazidReminderRule.class);

	public static final String TOKEN = "Stop Izoniazid Reminder";

	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
	 */
	@Override
	protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) throws LogicException {
		Result result = new Result();

		ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();
		parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.TUBERCULOSIS_PROPHYLAXIS_PLAN));
		parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList(EvaluableNameConstants.START_DRUGS, EvaluableNameConstants.STOP_ALL));

		Result planResults = obsWithRestrictionRule.eval(context, patientId, parameters);
		if (CollectionUtils.isNotEmpty(planResults)) {

			Concept startDrugConcept = CacheUtils.getConcept(EvaluableNameConstants.START_DRUGS);
			Concept stopDrugConcept = CacheUtils.getConcept(EvaluableNameConstants.STOP_ALL);

			Integer counter = 0;
			Date stopDrugDate = null;
			Date startDrugDate = null;
			while (counter < planResults.size() && startDrugDate == null && stopDrugDate == null) {
				Result planResult = planResults.get(counter++);
				if (OpenmrsUtil.nullSafeEquals(startDrugConcept, planResult.toConcept()))
					startDrugDate = planResult.getResultDate();
				if (OpenmrsUtil.nullSafeEquals(stopDrugConcept, planResult.toConcept()))
					stopDrugDate = planResult.getResultDate();
			}

			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.MONTH, -9);
			Date nineMonthsAgo = calendar.getTime();

			if (OpenmrsUtil.compareWithNullAsLatest(stopDrugDate, startDrugDate) == 1
					&& OpenmrsUtil.compareWithNullAsLatest(nineMonthsAgo, startDrugDate) == 1) {

                Result tbMedications = new Result();

                TuberculosisRule tuberculosisRule = new TuberculosisRule();
                parameters.put(EvaluableConstants.ENCOUNTER_TYPE, Arrays.asList(EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_INITIAL,
                        EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_RETURN, EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_NONCLINICALMEDICATION));
                Result tbResults = tuberculosisRule.eval(context, patientId, parameters);
                tbMedications.addAll(tbResults);

                TuberculosisTreatmentRule tuberculosisTreatmentRule = new TuberculosisTreatmentRule();
                parameters.put(EvaluableConstants.ENCOUNTER_TYPE, Arrays.asList(EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_INITIAL,
                        EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_RETURN, EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_NONCLINICALMEDICATION));
                Result tbTreatmentResults = tuberculosisTreatmentRule.eval(context, patientId, parameters);
                tbMedications.addAll(tbTreatmentResults);

                Concept isoniazidConcept = CacheUtils.getConcept(EvaluableNameConstants.ISONIAZID);

                if (tbMedications.size() == 1 && tbMedications.contains(isoniazidConcept))
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
