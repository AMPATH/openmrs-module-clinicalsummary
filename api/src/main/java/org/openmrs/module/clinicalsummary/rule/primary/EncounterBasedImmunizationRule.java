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

package org.openmrs.module.clinicalsummary.rule.primary;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.cache.CacheUtils;
import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableNameConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;
import org.openmrs.util.OpenmrsUtil;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 */
public class EncounterBasedImmunizationRule extends EvaluableRule {

	private static final Log log = LogFactory.getLog(EncounterBasedImmunizationRule.class);

	public static final String TOKEN = "Encounter Based Immunization";

	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
	 */
	@Override
	protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) throws LogicException {
		Result result = new Result();

		// check if the caller already pass encounter list object in the parameter
		Object encounters = parameters.get(EvaluableConstants.OBS_ENCOUNTER);
		if (encounters != null) {

			ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();
			// evaluate both concept to get the entire history of patient immunization
			parameters.put(EvaluableConstants.OBS_ENCOUNTER, encounters);
			parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.IMMUNIZATIONS_COMPLETE));
			parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList(EvaluableNameConstants.YES));
			Result completeImmunizationResults = obsWithRestrictionRule.eval(context, patientId, parameters);
			if (CollectionUtils.isNotEmpty(completeImmunizationResults))
				return new Result(Context.getConceptService().getConcept(EvaluableNameConstants.IMMUNIZATIONS_COMPLETE));

			// immunization is not complete
			parameters.remove(EvaluableConstants.OBS_VALUE_CODED);
			parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.IMMUNIZATIONS_ORDERED_DETAILED));
			Result immunizationResults = obsWithRestrictionRule.eval(context, patientId, parameters);
			// immunization ordered concept structure
			Concept immunizationOrderedConcept = CacheUtils.getConcept(EvaluableNameConstants.IMMUNIZATIONS_ORDERED);
			Concept dosesOrderedConcept = CacheUtils.getConcept(EvaluableNameConstants.NUMBER_OF_DOSES_ORDERED);

			Concept noneConcept = CacheUtils.getConcept(EvaluableNameConstants.NONE);

			for (Result immunizationResult : immunizationResults) {
				Obs immunizationObs = (Obs) immunizationResult.getResultObject();
				if (immunizationObs.isObsGrouping()) {
					Set<Obs> immunizationObsMembers = immunizationObs.getGroupMembers();
					for (Obs immunizationObsMember : immunizationObsMembers) {
						Result administeredResult = new Result();
						Concept immunizationObsMemberConcept = immunizationObsMember.getConcept();
						if (OpenmrsUtil.nullSafeEquals(immunizationObsMemberConcept, immunizationOrderedConcept)
								&& !OpenmrsUtil.nullSafeEquals(immunizationObsMember.getValueCoded(), noneConcept))
							administeredResult = new Result(immunizationObsMember.getValueCoded());
						if (OpenmrsUtil.nullSafeEquals(immunizationObsMemberConcept, dosesOrderedConcept))
							administeredResult.setValueNumeric(immunizationObsMember.getValueNumeric());
						result.add(administeredResult);
					}
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
