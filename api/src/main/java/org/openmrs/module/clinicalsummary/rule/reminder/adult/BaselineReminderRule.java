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

package org.openmrs.module.clinicalsummary.rule.reminder.adult;

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
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.reminder.ReminderParameters;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Parameters <ul> <li>[Required] concept: the result of the tests</li> <li>[Required] valueCoded: the answers for the tests
 * ordered</li> <li>[Required] reminder: the text that will be displayed if the reminder return true</li> </ul>
 */
public class BaselineReminderRule extends EvaluableRule {

	private static final Log log = LogFactory.getLog(BaselineReminderRule.class);

	public static final String TOKEN = "Baseline Reminder";

	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
	 */
	@Override
	protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) throws LogicException {
		Result result = new Result();
		// remove the coded value from the parameters first
		Object codedValueParams = parameters.remove(EvaluableConstants.OBS_VALUE_CODED);
		// create the low level rule object that will get us the data
		ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();
		// just fetch one obs
		parameters.put(EvaluableConstants.OBS_FETCH_SIZE, 1);
		// pull all results from the database
		Result resultResults = obsWithRestrictionRule.eval(context, patientId, parameters);
		if (CollectionUtils.isEmpty(resultResults)) {
			// get the latest test ordered
			parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.TESTS_ORDERED));
			parameters.put(EvaluableConstants.OBS_VALUE_CODED, codedValueParams);

			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.MONTH, -6);
			Date sixMonths = calendar.getTime();

			Result testResults = obsWithRestrictionRule.eval(context, patientId, parameters);
			// if no test or the test was ordered more than six months ago, then show the reminder
			if (CollectionUtils.isEmpty(testResults) || testResults.getResultDate().before(sixMonths)) {
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
