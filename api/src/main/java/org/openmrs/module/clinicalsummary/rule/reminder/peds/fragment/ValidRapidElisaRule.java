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

package org.openmrs.module.clinicalsummary.rule.reminder.peds.fragment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableNameConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableParameter;
import org.openmrs.module.clinicalsummary.rule.EvaluableRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Valid rapid elisa results are results that comes after the patient is 18 months old.
 * <p/>
 * Parameters: <ul> <li>[Optional] valueCoded: the possible answers for the rapid elisa</li> <li>[Optional] size: the size that
 * should be returned by the rule</li> <li>[Optional] order: the ordering of the list that should be returned by the rule</li>
 * </ul>
 */
public class ValidRapidElisaRule extends EvaluableRule {

	private static final Log log = LogFactory.getLog(ValidRapidElisaRule.class);

	public static final String TOKEN = "Valid Rapid Elisa";

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
			Date validReferenceDate = calendar.getTime();

			// user of the rule can override this value if they want to
			if (!parameters.containsKey(EvaluableConstants.OBS_CONCEPT))
				parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.HIV_RAPID_TEST_QUALITATIVE));

			ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();
			Result validRapidResults = obsWithRestrictionRule.eval(context, patientId, parameters);

			for (Result validRapidResult : validRapidResults) {
				if (validRapidResult.getResultDate().after(validReferenceDate))
					result.add(validRapidResult);
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

	/**
	 * Whether the result of the rule should be cached or not
	 *
	 * @return true if the system should put the result into the caching system
	 */
	@Override
	protected Boolean cacheable() {
		return Boolean.TRUE;
	}

	/**
	 * Get the definition of each parameter that should be passed to this rule execution
	 *
	 * @return all parameter that applicable for each rule execution
	 */
	@Override
	public Set<EvaluableParameter> getEvaluationParameters() {
		Set<EvaluableParameter> evaluableParameters = new HashSet<EvaluableParameter>();
		evaluableParameters.add(EvaluableConstants.OPTIONAL_OBS_CONCEPT_PARAMETER_DEFINITION);
		evaluableParameters.add(EvaluableConstants.OPTIONAL_OBS_VALUE_CODED_PARAMETER_DEFINITION);
		evaluableParameters.add(EvaluableConstants.OPTIONAL_OBS_FETCH_ORDER_PARAMETER_DEFINITION);
		evaluableParameters.add(EvaluableConstants.OPTIONAL_OBS_FETCH_SIZE_PARAMETER_DEFINITION);
		return evaluableParameters;
	}
}
