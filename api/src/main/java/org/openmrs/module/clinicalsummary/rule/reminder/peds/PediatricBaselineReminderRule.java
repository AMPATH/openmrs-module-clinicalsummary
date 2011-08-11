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

package org.openmrs.module.clinicalsummary.rule.reminder.peds;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableNameConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableParameter;
import org.openmrs.module.clinicalsummary.rule.EvaluableRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.reminder.ReminderParameters;
import org.openmrs.module.clinicalsummary.rule.reminder.adult.BaselineReminderRule;
import org.openmrs.module.clinicalsummary.rule.reminder.peds.fragment.ValidElisaRule;
import org.openmrs.module.clinicalsummary.rule.reminder.peds.fragment.ValidPolymeraseRule;
import org.openmrs.module.clinicalsummary.rule.reminder.peds.fragment.ValidRapidElisaRule;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 */
public class PediatricBaselineReminderRule extends EvaluableRule {

	private static final Log log = LogFactory.getLog(PediatricBaselineReminderRule.class);

	public static final String TOKEN = "Pediatric Baseline Reminder";

	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
	 */
	@Override
	protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) throws LogicException {
		Result result = new Result();

		if (log.isDebugEnabled())
			log.debug("Processing baseline reminder ...");

		BaselineReminderRule baselineReminderRule = new BaselineReminderRule();
		Result baselineReminderResult = baselineReminderRule.eval(context, patientId, parameters);

		// this means we are seeing a reminder
		if (CollectionUtils.isNotEmpty(baselineReminderResult)) {

			// for pediatric, we need to check the any positive lab results before we throw the reminder
			parameters.put(EvaluableConstants.OBS_FETCH_SIZE, 1);
			parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList(EvaluableNameConstants.POSITIVE));

			ValidElisaRule validElisaRule = new ValidElisaRule();
			parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.HIV_ENZYME_IMMUNOASSAY_QUALITATIVE));
			Result positiveValidElisaResults = validElisaRule.eval(context, patientId, parameters);

			ValidPolymeraseRule validPolymeraseRule = new ValidPolymeraseRule();
			parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.HIV_DNA_POLYMERASE_CHAIN_REACTION_QUALITATIVE));
			Result positiveValidPolymeraseResults = validPolymeraseRule.eval(context, patientId, parameters);

			ValidRapidElisaRule validRapidElisaRule = new ValidRapidElisaRule();
			parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.HIV_RAPID_TEST_QUALITATIVE));
			Result positiveValidRapidElisaResults = validRapidElisaRule.eval(context, patientId, parameters);

			if (CollectionUtils.isNotEmpty(positiveValidElisaResults) || CollectionUtils.isNotEmpty
					(positiveValidPolymeraseResults) || CollectionUtils.isNotEmpty(positiveValidRapidElisaResults))
				result.add(new Result(String.valueOf(parameters.get(ReminderParameters.DISPLAYED_REMINDER_TEXT))));
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
	 * Get the definition of each parameter that should be passed to this rule execution
	 *
	 * @return all parameter that applicable for each rule execution
	 */
	@Override
	public Set<EvaluableParameter> getEvaluationParameters() {
		Set<EvaluableParameter> evaluableParameters = new HashSet<EvaluableParameter>();
		evaluableParameters.add(EvaluableConstants.REQUIRED_OBS_CONCEPT_PARAMETER_DEFINITION);
		evaluableParameters.add(EvaluableConstants.REQUIRED_OBS_VALUE_CODED_PARAMETER_DEFINITION);
		return evaluableParameters;
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
