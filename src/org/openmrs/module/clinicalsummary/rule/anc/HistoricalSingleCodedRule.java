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
package org.openmrs.module.clinicalsummary.rule.anc;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.LogicCriteria;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.Rule;
import org.openmrs.logic.result.Result;
import org.openmrs.logic.result.Result.Datatype;
import org.openmrs.logic.rule.RuleParameterInfo;
import org.openmrs.module.clinicalsummary.SummaryService;
import org.openmrs.module.clinicalsummary.cache.SummaryDataSource;
import org.openmrs.module.clinicalsummary.encounter.TypeConstants;
import org.openmrs.module.clinicalsummary.rule.RuleConstants;
import org.openmrs.util.OpenmrsUtil;

/**
 * 
 */
public class HistoricalSingleCodedRule implements Rule {
	
	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, org.openmrs.Patient,
	 *      java.util.Map)
	 */
	@Override
	public Result eval(LogicContext context, Patient patient, Map<String, Object> parameters) throws LogicException {
		String conceptName = String.valueOf(parameters.get(RuleConstants.EVALUATED_CONCEPT));
		
		SummaryService service = Context.getService(SummaryService.class);
		
		LogicCriteria initialEncounterCriteria = service.parseToken(SummaryDataSource.ENCOUNTER_TYPE).equalTo(
		    TypeConstants.ANC_INITIAL);
		Result initialResults = context.read(patient, service.getLogicDataSource("summary"), initialEncounterCriteria);
		Result latestInitial = initialResults.latest();
		
		LogicCriteria returnEncounterCriteria = service.parseToken(SummaryDataSource.ENCOUNTER_TYPE).equalTo(
		    TypeConstants.ANC_RETURN);
		Result returnResults = context.read(patient, service.getLogicDataSource("summary"), returnEncounterCriteria);
		
		LogicCriteria conceptCriteria = service.parseToken(SummaryDataSource.CONCEPT).equalTo(conceptName);
		LogicCriteria encounterCriteria = service.parseToken(SummaryDataSource.ENCOUNTER_TYPE).in(
		    Arrays.asList(TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN));
		LogicCriteria criteria = conceptCriteria.and(encounterCriteria);
		Result obsResults = context.read(patient, service.getLogicDataSource("summary"), criteria);
		
		Result result = new Result();
		
		int counter = 0;
		int encounterCounter = 0;
		boolean shouldStop = (latestInitial == null);
		while (!shouldStop) {
			Result encounter = null;
			// if there's still encounter available, then get that one
			if (encounterCounter < returnResults.size()) {
				encounter = returnResults.get(encounterCounter);
				// but only if this encounter comes after initial
				// if the initial comes after the return, the process the initial instead
				if (latestInitial.getResultDate().after(encounter.getResultDate()))
					encounter = latestInitial;
			} else {
				// if not, then process the initial encounter
				encounter = latestInitial;
			}
			
			if (encounter != null) {
				Result observationResult = new Result();
				// only process if we still have observation left in the list
				if (counter < obsResults.size()) {
					Result obsResult = obsResults.get(counter);
					Obs observation = (Obs) obsResult.getResultObject();
					
					// only insert if we have the correct observation and move the counter forward
					if (OpenmrsUtil.nullSafeEquals(encounter.getResultObject(), observation.getEncounter())) {
						observationResult = obsResult;
						counter++;
					}
				}
				
				// we add empty result or an observation result depending on the above if
				result.add(observationResult);
				encounterCounter++;
			}
			
			// we don't need to proceed beyond the initial
			if (OpenmrsUtil.nullSafeEquals(encounter.getResultObject(), latestInitial.getResultObject()))
				shouldStop = true;
		}
		
		return result;
	}
	
	/**
	 * @see org.openmrs.logic.Rule#getDefaultDatatype()
	 */
	@Override
	public Datatype getDefaultDatatype() {
		return Datatype.CODED;
	}
	
	/**
	 * @see org.openmrs.logic.Rule#getDependencies()
	 */
	@Override
	public String[] getDependencies() {
		return null;
	}
	
	/**
	 * @see org.openmrs.logic.Rule#getParameterList()
	 */
	@Override
	public Set<RuleParameterInfo> getParameterList() {
		return null;
	}
	
	/**
	 * @see org.openmrs.logic.Rule#getTTL()
	 */
	@Override
	public int getTTL() {
		return 0;
	}
	
}
