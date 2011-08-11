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

package org.openmrs.module.clinicalsummary.rule.reminder.peds.pneumocystis;

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
import org.openmrs.module.clinicalsummary.rule.medication.PneumocystisCariniiRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.reminder.ReminderParameters;
import org.openmrs.module.clinicalsummary.rule.reminder.peds.fragment.ValidElisaRule;
import org.openmrs.module.clinicalsummary.rule.reminder.peds.fragment.ValidRapidElisaRule;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * Parameters: <ul> <li>[Optional] encounterType: list of all applicable encounter types </ul>
 */
public class ElisaBasedReminderRule extends EvaluableRule {

	private static final Log log = LogFactory.getLog(ElisaBasedReminderRule.class);

	public static final String TOKEN = "Elisa Based PCP Reminder";

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

			calendar.add(Calendar.MONTH, 18);

			if (calendar.getTime().before(new Date())) {

				parameters.put(EvaluableConstants.OBS_FETCH_SIZE, 1);
				parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList(EvaluableNameConstants.POSITIVE));

				ValidElisaRule validElisaRule = new ValidElisaRule();
				parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.HIV_ENZYME_IMMUNOASSAY_QUALITATIVE));
				Result validElisaResults = validElisaRule.eval(context, patientId, parameters);

				ValidRapidElisaRule validRapidElisaRule = new ValidRapidElisaRule();
				parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.HIV_RAPID_TEST_QUALITATIVE));
				Result validRapidElisaResults = validRapidElisaRule.eval(context, patientId, parameters);

				if (CollectionUtils.isNotEmpty(validElisaResults) || CollectionUtils.isNotEmpty(validRapidElisaResults)) {
					PneumocystisCariniiRule pneumocystisCariniiRule = new PneumocystisCariniiRule();
					parameters.put(EvaluableConstants.ENCOUNTER_TYPE, Arrays.asList(EvaluableNameConstants.ENCOUNTER_TYPE_PEDIATRIC_INITIAL,
							EvaluableNameConstants.ENCOUNTER_TYPE_PEDIATRIC_RETURN, EvaluableNameConstants.ENCOUNTER_TYPE_PEDIATRIC_NONCLINICALMEDICATION));
					Result pneumocystisCariniiResults = pneumocystisCariniiRule.eval(context, patientId, parameters);

					if (CollectionUtils.isEmpty(pneumocystisCariniiResults))
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
