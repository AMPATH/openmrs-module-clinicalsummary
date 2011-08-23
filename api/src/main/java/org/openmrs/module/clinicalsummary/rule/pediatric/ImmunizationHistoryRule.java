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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.cache.CacheUtils;
import org.openmrs.module.clinicalsummary.enumeration.FetchOrdering;
import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableNameConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;
import org.openmrs.util.OpenmrsUtil;

/**
 */
public class ImmunizationHistoryRule extends EvaluableRule {

	private static final Log log = LogFactory.getLog(ImmunizationHistoryRule.class);

	public static final String TOKEN = "Immunization History";

	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
	 */
	@Override
	protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) throws LogicException {
		Result result = new Result();

		Map<Concept, Double> immunizationAdministered = new HashMap<Concept, Double>();

		ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();
		// evaluate both concept to get the entire history of patient immunization
		parameters.put(EvaluableConstants.OBS_CONCEPT,
				Arrays.asList(EvaluableNameConstants.IMMUNIZATIONS_HISTORY, EvaluableNameConstants.IMMUNIZATIONS_ORDERED_DETAILED));
		parameters.put(EvaluableConstants.OBS_FETCH_ORDER, FetchOrdering.ORDER_ASCENDING.getValue());
		Result immunizationResults = obsWithRestrictionRule.eval(context, patientId, parameters);
		// when the immunization is complete, the provider will answer with this concept
		Concept completeAdministeredConcept = Context.getConceptService().getConcept(EvaluableNameConstants.IMMUNIZATIONS_COMPLETED);
		// immunization history concept structure
		Concept previousAdministeredConcept = CacheUtils.getConcept(EvaluableNameConstants.PREVIOUS_IMMUNIZATIONS_ADMINISTERED);
		Concept dosesAdministeredConcept = CacheUtils.getConcept(EvaluableNameConstants.NUMBER_OF_DOSES_RECEIVED_BEFORE_ENROLLMENT);
		// immunization ordered concept structure
		Concept immunizationOrderedConcept = CacheUtils.getConcept(EvaluableNameConstants.IMMUNIZATIONS_ORDERED);
		Concept dosesOrderedConcept = CacheUtils.getConcept(EvaluableNameConstants.NUMBER_OF_DOSES_ORDERED);

		Concept noneConcept = CacheUtils.getConcept(EvaluableNameConstants.NONE);

		for (Result immunizationResult : immunizationResults) {
			Obs immunizationObs = (Obs) immunizationResult.getResultObject();
			if (immunizationObs.isObsGrouping()) {
				Set<Obs> immunizationObsMembers = immunizationObs.getGroupMembers();

				Double dosesAdministered = null;
				Concept administeredImmunization = null;

				for (Obs immunizationObsMember : immunizationObsMembers) {
					Concept immunizationObsMemberConcept = immunizationObsMember.getConcept();
					if (OpenmrsUtil.nullSafeEquals(immunizationObsMemberConcept, previousAdministeredConcept)) {
						administeredImmunization = immunizationObsMember.getValueCoded();
						if (OpenmrsUtil.nullSafeEquals(immunizationObsMember.getValueCoded(), completeAdministeredConcept))
							return new Result(completeAdministeredConcept);
					}
					if (OpenmrsUtil.nullSafeEquals(immunizationObsMemberConcept, dosesAdministeredConcept))
						dosesAdministered = immunizationObsMember.getValueNumeric();

					if (OpenmrsUtil.nullSafeEquals(immunizationObsMemberConcept, immunizationOrderedConcept)
							&& !OpenmrsUtil.nullSafeEquals(immunizationObsMember.getValueCoded(), noneConcept))
						administeredImmunization = immunizationObsMember.getValueCoded();
					if (OpenmrsUtil.nullSafeEquals(immunizationObsMemberConcept, dosesOrderedConcept))
						dosesAdministered = immunizationObsMember.getValueNumeric();
				}

				if (administeredImmunization != null)
					immunizationAdministered.put(administeredImmunization, dosesAdministered);
			}
		}

		for (Concept administered : immunizationAdministered.keySet()) {
			Result administeredResult = new Result(administered);
			Double doses = immunizationAdministered.get(administered);
			if (doses == null)
				administeredResult.setValueText("No Dosing");
			administeredResult.setValueNumeric(doses);
			result.add(administeredResult);
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
