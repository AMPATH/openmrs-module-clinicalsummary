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

package org.openmrs.module.clinicalsummary.rule.pediatric;

import org.apache.commons.collections.CollectionUtils;
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
import org.openmrs.module.clinicalsummary.rule.EvaluableRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.util.FetchOrdering;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 */
public class MotherToChildPreventionRule extends EvaluableRule {

	private static final Log log = LogFactory.getLog(MotherToChildPreventionRule.class);

	public static final String TOKEN = "Mother to Child Prevention";

	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
	 */
	@Override
	protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) throws LogicException {
		Result result = new Result();

		if (log.isDebugEnabled())
			log.debug("Processing mother to child prevention rule ...");

		ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();

		parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.MATERNAL_PARTUM_ANTIRETROVIRAL_HISTORY));
		parameters.put(EvaluableConstants.OBS_FETCH_ORDER, FetchOrdering.ORDER_ASCENDING.getValue());

		Result antiRetroViralHistoryResults = obsWithRestrictionRule.eval(context, patientId, parameters);

		Concept antiRetroViralConcept = CacheUtils.getConcept(EvaluableNameConstants.PARTUM_ANTIRETROVIRAL_USE);
		Concept dosingPeriodConcept = CacheUtils.getConcept(EvaluableNameConstants.PARTUM_DOSING_PERIOD);
		Concept dosingQuantificationConcept = CacheUtils.getConcept(EvaluableNameConstants.ANTIRETROVIRAL_DOSE_QUANTIFICATION);
		Concept weeksOnTreatmentConcept = CacheUtils.getConcept(EvaluableNameConstants.NUMBER_OF_WEEKS_ON_TREATMENT);

		for (Result antiRetroViralHistoryResult : antiRetroViralHistoryResults) {
			Obs antiRetroViralHistoryObsGroup = (Obs) antiRetroViralHistoryResult.getResultObject();
			if (antiRetroViralHistoryObsGroup.isObsGrouping()) {
				Set<Obs> antiRetroViralHistoryObsMembers = antiRetroViralHistoryObsGroup.getGroupMembers();

				Map<Concept, Result> obsMap = new HashMap<Concept, Result>();
				for (Obs antiRetroViralHistoryObs : antiRetroViralHistoryObsMembers) {
					Concept antiRetroViralHistoryConcept = antiRetroViralHistoryObs.getConcept();
					Result obsMapEntry = obsMap.get(antiRetroViralHistoryConcept);
					if (obsMapEntry == null) {
						obsMapEntry = new Result();
						obsMap.put(antiRetroViralHistoryConcept, obsMapEntry);
					}
					obsMapEntry.add(new Result(antiRetroViralHistoryObs));
				}

				Result[] results = new Result[]{new Result(), new Result(), new Result(), new Result()};
				Result antiRetroViralResult = obsMap.get(antiRetroViralConcept);
				if (CollectionUtils.isNotEmpty(antiRetroViralResult)) {
					results[0] = antiRetroViralResult;

					Result dosingResult = obsMap.get(dosingPeriodConcept);
					if (CollectionUtils.isNotEmpty(dosingResult))
						results[1] = dosingResult;

					Result dosingQuantificationResult = obsMap.get(dosingQuantificationConcept);
					if (CollectionUtils.isNotEmpty(dosingQuantificationResult))
						results[2] = dosingQuantificationResult;

					Result weeksOnTreatmentResult = obsMap.get(weeksOnTreatmentConcept);
					if (CollectionUtils.isNotEmpty(weeksOnTreatmentResult))
						results[3] = weeksOnTreatmentResult;

					Result resultEntry = new Result();
					resultEntry.addAll(Arrays.asList(results));
					result.add(resultEntry);
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
