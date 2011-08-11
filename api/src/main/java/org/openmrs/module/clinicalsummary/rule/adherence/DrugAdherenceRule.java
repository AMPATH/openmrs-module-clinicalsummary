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

package org.openmrs.module.clinicalsummary.rule.adherence;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.cache.CacheUtils;
import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableNameConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableParameter;
import org.openmrs.module.clinicalsummary.rule.EvaluableRule;
import org.openmrs.module.clinicalsummary.rule.encounter.EncounterWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.encounter.EncounterWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;
import org.openmrs.util.OpenmrsUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 */
public class DrugAdherenceRule extends EvaluableRule {

	private static final Log log = LogFactory.getLog(DrugAdherenceRule.class);

	public static final String TOKEN = "Drug Adherence";

	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
	 */
	@Override
	protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) throws LogicException {

		Result result = new Result();
		// list of all dates with missing data
		List<Date> dates = new ArrayList<Date>();
		// time frame for the adherence is six month
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -6);
		Date sixMonthsAgo = calendar.getTime();

		EncounterWithRestrictionRule encounterWithRestrictionRule = new EncounterWithStringRestrictionRule();
		Result encounterResults = encounterWithRestrictionRule.eval(context, patientId, parameters);

		Boolean perfect = Boolean.FALSE;

		Concept noConcept = CacheUtils.getConcept(EvaluableNameConstants.NO);
		Concept allConcept = CacheUtils.getConcept(EvaluableNameConstants.ALL);

		for (Result encounterResult : encounterResults) {

			if (encounterResult.getResultDate().after(sixMonthsAgo)) {
				ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();

				parameters.put(EvaluableConstants.OBS_ENCOUNTER, Arrays.asList(encounterResult.getResultObject()));
				parameters.put(EvaluableConstants.OBS_CONCEPT,
						Arrays.asList(EvaluableNameConstants.ANTIRETROVIRAL_ADHERENCE_IN_PAST_WEEK,
								EvaluableNameConstants.OVERALL_DRUG_ADHERENCE_IN_LAST_MONTH));
				Result adherenceResults = obsWithRestrictionRule.eval(context, patientId, parameters);

				Concept arvAdherenceConcept = CacheUtils.getConcept(EvaluableNameConstants.ANTIRETROVIRAL_ADHERENCE_IN_PAST_WEEK);
				Concept overallAdherenceConcept = CacheUtils.getConcept(EvaluableNameConstants.OVERALL_DRUG_ADHERENCE_IN_LAST_MONTH);

				Boolean monthAdherence = Boolean.TRUE;
				Boolean weekAdherence = Boolean.TRUE;

				for (Result adherenceResult : adherenceResults) {
					Obs obs = (Obs) adherenceResult.getResultObject();
					Concept adherenceConcept = adherenceResult.toConcept();
					if (OpenmrsUtil.nullSafeEquals(arvAdherenceConcept, obs.getConcept())
							&& !OpenmrsUtil.nullSafeEquals(adherenceConcept, allConcept))
						weekAdherence = Boolean.FALSE;
					if (OpenmrsUtil.nullSafeEquals(overallAdherenceConcept, obs.getConcept())
							&& !OpenmrsUtil.nullSafeEquals(adherenceConcept, noConcept))
						monthAdherence = Boolean.FALSE;
				}

				if (CollectionUtils.isEmpty(adherenceResults) || CollectionUtils.size(adherenceResults) < 2)
					dates.add(encounterResult.getResultDate());
				else {
					perfect = monthAdherence && weekAdherence;
					if (!perfect)
						return new Result("Imperfect Adherence");
				}
			}
		}

		if (CollectionUtils.isNotEmpty(dates))
			result.setValueText(formatDate(dates));
		else if (perfect)
			result.setValueText("Perfect Adherence");
		return result;
	}

	private String formatDate(final List<Date> dates) {

		String format = "dd-MMM-yyyy";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);

		if (dates.size() > 2) {
			String[] formattedDates = new String[2];
			for (int i = 0; i < formattedDates.length; i++)
				formattedDates[i] = simpleDateFormat.format(dates.get(i));
			return "Missing Data - " + StringUtils.join(formattedDates, ", ") + "(" + (dates.size() - 2) + " More)";
		} else {
			String[] formattedDates = new String[dates.size()];
			for (int i = 0; i < formattedDates.length; i++)
				formattedDates[i] = simpleDateFormat.format(dates.get(i));
			return "Missing Data - " + StringUtils.join(formattedDates, ", ");
		}
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
		evaluableParameters.add(EvaluableConstants.REQUIRED_ENCOUNTER_TYPE_PARAMETER_DEFINITION);
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
