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

package org.openmrs.module.clinicalsummary.rule.flowsheet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNumeric;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableNameConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableParameter;
import org.openmrs.module.clinicalsummary.rule.EvaluableRule;
import org.openmrs.module.clinicalsummary.rule.encounter.EncounterWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.util.RuleUtils;
import org.openmrs.module.clinicalsummary.util.obs.Status;
import org.openmrs.util.OpenmrsUtil;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 */
public class ObsOrderedMergedFlowsheetRule extends EvaluableRule {

	private static final Log log = LogFactory.getLog(ObsOrderedMergedFlowsheetRule.class);

	public static final String TOKEN = "Obs Ordered Merged Flowsheet";

	private static final String OBS_MERGED_CONCEPT = "obs.merged";

	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
	 */
	@Override
	protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) throws LogicException {
		Result result = new Result();

		Object codedValueObjects = parameters.remove(EvaluableConstants.OBS_VALUE_CODED);

		// get the results
		Result resultResults = searchResults(context, patientId, parameters);

		// get the merged results
		parameters.put(EvaluableConstants.OBS_CONCEPT, parameters.get(OBS_MERGED_CONCEPT));
		Result mergedResults = searchResults(context, patientId, parameters);

		// get the test ordered
		parameters.put(EvaluableConstants.OBS_VALUE_CODED, codedValueObjects);
		parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.TESTS_ORDERED));
		Result testResults = searchResults(context, patientId, parameters);

		Integer resultCounter = 0;
		Integer testCounter = 0;
		Integer mergedCounter = 0;
		while (resultCounter < resultResults.size() && testCounter < testResults.size()) {
			Date resultDate = resultResults.get(resultCounter).getResultDate();
			Date testDate = testResults.get(testCounter).getResultDate();

			Result flowsheetResult;
			if (testDate.after(resultDate)) {
				Result currentResult = testResults.get(testCounter++);
				// status of the test is no-result
				// create a copy of the same result as the order result above
				flowsheetResult = duplicateResult(currentResult);
				flowsheetResult.setValueCoded(null);
				flowsheetResult.setResultObject(null);
				// prepare the status of the result
				StringBuilder statusBuilder = new StringBuilder();
				if (StringUtils.isNotEmpty(flowsheetResult.toString()))
					statusBuilder.append(flowsheetResult.toString()).append(" ");
				statusBuilder.append(Status.STATUS_NO_RESULT.getValue());
				// create a copy of the same result as the test result above
				flowsheetResult.setValueText(statusBuilder.toString());
			} else {
				Result currentResult = resultResults.get(resultCounter++);
				// create a copy of the same result as the order result above
				flowsheetResult = duplicateResult(currentResult);
				flowsheetResult.setResultObject(null);
				// search whether we have result for the same date that can be merged together
				Date mergedDate = null;
				if (mergedCounter < mergedResults.size())
					mergedDate = mergedResults.get(mergedCounter).getResultDate();
				if (OpenmrsUtil.nullSafeEquals(mergedDate, flowsheetResult.getResultDate()))
					flowsheetResult.setResultObject(format(mergedResults.get(mergedCounter++)));
				// if this result more than one day after the test was ordered then it's no-order
				if (resultDate.after(DateUtils.addDays(testDate, 1))) {
					// status of the order is no-order
					StringBuilder statusBuilder = new StringBuilder();
					if (StringUtils.isNotEmpty(flowsheetResult.toString()))
						statusBuilder.append(flowsheetResult.toString()).append(" ");
					statusBuilder.append(Status.STATUS_NO_ORDER.getValue());
					flowsheetResult.setValueText(statusBuilder.toString());
				} else
					testCounter++;
			}
			result.add(flowsheetResult);
		}

		while (resultCounter < resultResults.size()) {
			Result currentResult = resultResults.get(resultCounter++);
			Result flowsheetResult = duplicateResult(currentResult);
			flowsheetResult.setResultObject(null);
			// prepare the status of the result
			StringBuilder statusBuilder = new StringBuilder();
			if (StringUtils.isNotEmpty(flowsheetResult.toString()))
				statusBuilder.append(flowsheetResult.toString()).append(" ");
			statusBuilder.append(Status.STATUS_NO_ORDER.getValue());
			flowsheetResult.setValueText(statusBuilder.toString());
			// get the result to be merged
			Date mergedDate = null;
			if (mergedCounter < mergedResults.size())
				mergedDate = mergedResults.get(mergedCounter).getResultDate();
			if (OpenmrsUtil.nullSafeEquals(mergedDate, flowsheetResult.getResultDate()))
				flowsheetResult.setResultObject(format(mergedResults.get(mergedCounter++)));
			result.add(flowsheetResult);
		}

		while (testCounter < testResults.size()) {
			Result currentResult = testResults.get(testCounter++);
			// create a copy of the same result as the order result above
			Result flowsheetResult = duplicateResult(currentResult);
			flowsheetResult.setValueCoded(null);
			flowsheetResult.setResultObject(null);
			// prepare the status of the result
			StringBuilder statusBuilder = new StringBuilder();
			if (StringUtils.isNotEmpty(flowsheetResult.toString()))
				statusBuilder.append(flowsheetResult.toString()).append(" ");
			statusBuilder.append(Status.STATUS_NO_RESULT.getValue());
			// create a copy of the same result as the test result above
			flowsheetResult.setValueText(statusBuilder.toString());
			// remove the value coded because we don't want to display the name of the test ordered
			result.add(flowsheetResult);
		}

		result = FlowsheetUtils.slice(result);
		Collections.reverse(result);
		return result;
	}

	private Result duplicateResult(final Result result) {
		Obs obs = (Obs) result.getResultObject();
		return new Result(obs.getObsDatetime(), null, obs.getValueAsBoolean(), obs.getValueCoded(),
				obs.getValueDatetime(), obs.getValueNumeric(), obs.getValueText(), obs);
	}

	private Result searchResults(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) {
		ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();
		Result results = obsWithRestrictionRule.eval(context, patientId, parameters);
		return FlowsheetUtils.slice(results);
	}

	// TODO: delegate the following code to a static methods and merge them with the one in the VelocityUtils

	/**
	 * Format the result based on the result data type
	 *
	 * @param result the result
	 * @return the String representation of the result object
	 */
	private String format(final Result result) {
		if (result == null)
			return StringUtils.EMPTY;

		if (OpenmrsUtil.nullSafeEquals(result.getDatatype(), Result.Datatype.CODED))
			return format(result.toConcept());
		else if (OpenmrsUtil.nullSafeEquals(result.getDatatype(), Result.Datatype.DATETIME))
			return format(result.toDatetime());
		else if (OpenmrsUtil.nullSafeEquals(result.getDatatype(), Result.Datatype.NUMERIC)) {
			Object object = result.getResultObject();
			if (RuleUtils.isValidObsObject(object)) {
				Concept concept = ((Obs) object).getConcept();
				if (concept.isNumeric()) {
					ConceptNumeric conceptNumeric = Context.getConceptService().getConceptNumeric(concept.getConceptId());
					if (!conceptNumeric.isPrecise())
						return format(result.toNumber());
				}
			}

			DecimalFormat decimalFormat = new DecimalFormat("#.00");
			return decimalFormat.format(result.toNumber());
		}

		// special case hack
		if (result.getDatatype() == null && result.toDatetime() != null)
			return format(result.toDatetime());

		return result.toString();
	}

	/**
	 * Format double object and remove the decimal section of the value
	 *
	 * @param value the decimal to be formatted
	 * @return String representation of the decimal values
	 */
	private String format(final Double value) {
		if (value == null)
			return StringUtils.EMPTY;
		DecimalFormat decimalFormat = new DecimalFormat("#");
		return decimalFormat.format(value);
	}

	/**
	 * Format date to a string according to the date format
	 *
	 * @param date the date
	 * @return string representation of the date
	 */
	private String format(final Date date) {
		if (date == null)
			return StringUtils.EMPTY;
		String format = "dd-MMM-yyyy";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
		return simpleDateFormat.format(date);
	}

	/**
	 * Format the name of a concept with the best name for the locale or a shorter name for the concept
	 *
	 * @param concept the concept
	 * @return concept name with a relatively shorter name
	 */
	private String format(final Concept concept) {
		if (concept == null)
			return StringUtils.EMPTY;

		// use the best name as the default name
		String name = concept.getName(Context.getLocale()).getName();
		// when the name is too long, then use the concept's short name for display
		if (StringUtils.length(name) > 10) {
			ConceptName conceptName = concept.getShortNameInLocale(Context.getLocale());
			if (conceptName != null)
				name = conceptName.getName();
		}

		return name;
	}

	/**
	 * @see org.openmrs.logic.Rule#getDependencies()
	 */
	@Override
	public String[] getDependencies() {
		return new String[]{ObsWithStringRestrictionRule.TOKEN, EncounterWithStringRestrictionRule.TOKEN};
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
		evaluableParameters.add(new EvaluableParameter(OBS_MERGED_CONCEPT, List.class, Boolean.TRUE));
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
