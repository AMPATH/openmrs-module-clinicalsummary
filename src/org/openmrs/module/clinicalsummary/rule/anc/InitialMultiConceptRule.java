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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
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
import org.openmrs.module.clinicalsummary.rule.ResultComparator;
import org.openmrs.module.clinicalsummary.rule.RuleConstants;
import org.openmrs.module.clinicalsummary.rule.ResultComparator.CompareProperty;
import org.openmrs.util.OpenmrsUtil;

/**
 * 
 */
public class InitialMultiConceptRule implements Rule {
	
	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, org.openmrs.Patient,
	 *      java.util.Map)
	 */
	@Override
	public Result eval(LogicContext context, Patient patient, Map<String, Object> parameters) throws LogicException {
		
		String conceptName = String.valueOf(parameters.get(RuleConstants.EVALUATED_CONCEPT));
		
		String included = StringUtils.defaultIfEmpty((String) parameters.get(RuleConstants.INCLUDED_EVALUATED_CONCEPT), StringUtils.EMPTY);
		String[] includedConceptNames = StringUtils.split(included, ";");
		
		Result result = new Result();
		
		SummaryService service = Context.getService(SummaryService.class);
		
		LogicCriteria conceptCriteria = service.parseToken(SummaryDataSource.CONCEPT).equalTo(conceptName);
		LogicCriteria encounterCriteria = service.parseToken(SummaryDataSource.ENCOUNTER_TYPE).equalTo(TypeConstants.ANC_INITIAL).last();
		LogicCriteria criteria = conceptCriteria.and(encounterCriteria);
		Result groupedResults = context.read(patient, service.getLogicDataSource("summary"), criteria);

		List<Result> multiObsResults = new Result();
		for (String includedConceptName : includedConceptNames) {
			LogicCriteria includedConceptCriteria = service.parseToken(SummaryDataSource.CONCEPT).equalTo(StringUtils.strip(includedConceptName));
			LogicCriteria includedcriteria = includedConceptCriteria.and(encounterCriteria);
			multiObsResults.add(context.read(patient, service.getLogicDataSource("summary"), includedcriteria));
        }
		
		int[] obsVisited = new int[multiObsResults.size()];
		
		// Structure of the result:
		// --> Parent Result
		//     --> (Encounter Result
		//		   --> Encounter Datetime
		//         --> (Obs Result | Empty Result)+
		// 		   )*
		for (Result groupedResult : groupedResults) {
			Obs groupedObs = (Obs) groupedResult.getResultObject();
			
			Result encounterResult = new Result();
			// put the encounter date as the first result
			encounterResult.add(groupedResults);
			int counter = 0;
			// iterate over all list of list of observation
			for (Result multiObsResult : multiObsResults) {
				Result obsResult = new Result();
				if (obsVisited[counter] < multiObsResult.size()) {
					Result resultObs = multiObsResult.get(obsVisited[counter]);
					Obs obs = (Obs) resultObs.getResultObject();
					// default to empty result if no obs found for this encounter
					// this will work like padding system
					// only take obs that come from the latest encounter
					if (OpenmrsUtil.nullSafeEquals(groupedObs, obs.getObsGroup())) {
						// if we see obs for this encounter, then add them to current encounter result
						obsResult = resultObs;
						obsVisited[counter]++;
					}
				}
				encounterResult.add(obsResult);
				counter++;
			}
			result.add(encounterResult);
		}
		
		Collections.sort(result, new ResultComparator(CompareProperty.DATETIME));
		
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
