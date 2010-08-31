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
package org.openmrs.module.clinicalsummary.rule.peds;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
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
import org.openmrs.module.clinicalsummary.concept.ConceptRegistry;
import org.openmrs.module.clinicalsummary.concept.StandardConceptConstants;
import org.openmrs.module.clinicalsummary.encounter.TypeConstants;
import org.openmrs.util.OpenmrsUtil;

/**
 * 
 */
public class PedsWHOStageRule implements Rule {
	
	private static final Log log = LogFactory.getLog(PedsWHOStageRule.class);
	
	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, org.openmrs.Patient,
	 *      java.util.Map)
	 */
	public Result eval(LogicContext context, Patient patient, Map<String, Object> parameters) throws LogicException {
		
		Result result = new Result();
		
		Concept whoStageOne = ConceptRegistry.getCachedConcept(StandardConceptConstants.WHO_STAGE_1_PEDS);
		Concept whoStageTwo = ConceptRegistry.getCachedConcept(StandardConceptConstants.WHO_STAGE_2_PEDS);
		Concept whoStageThree = ConceptRegistry.getCachedConcept(StandardConceptConstants.WHO_STAGE_3_PEDS);
		Concept whoStageFour = ConceptRegistry.getCachedConcept(StandardConceptConstants.WHO_STAGE_4_PEDS);
		
		SummaryService service = Context.getService(SummaryService.class);
		
		List<String> typeNames = Arrays.asList(TypeConstants.ADULT_INITIAL,
		    TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
		    TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN,
		    TypeConstants.PEDS_NONCLINICAL);
		LogicCriteria encounterCriteria = service.parseToken(SummaryDataSource.ENCOUNTER_TYPE).in(typeNames);
		LogicCriteria obsCriteria = service.parseToken(SummaryDataSource.CONCEPT).equalTo(StandardConceptConstants.PEDS_WHO_CATEGORY_QUERY);
		
		Result obsResults = context.read(patient, service.getLogicDataSource("summary"), obsCriteria.and(encounterCriteria));
		
		boolean[] stages = new boolean[4];
		
		for (Result obsResult : obsResults) {
			if (log.isErrorEnabled())
				log.debug("Obs Coded Value: " + (obsResult.toConcept() == null ? "NULL" : obsResult.toConcept().getBestName(Context.getLocale()).getName()));
			
			if (OpenmrsUtil.nullSafeEquals(whoStageFour, obsResult.toConcept()))
				stages[3] = true;
			else if (OpenmrsUtil.nullSafeEquals(whoStageThree, obsResult.toConcept()))
				stages[2] = true;
			else if (OpenmrsUtil.nullSafeEquals(whoStageTwo, obsResult.toConcept()))
				stages[1] = true;
			else if (OpenmrsUtil.nullSafeEquals(whoStageOne, obsResult.toConcept()))
				stages[0] = true;
			
			if (stages[3])
				// the stage is the highest, we stop searching
				break;
		}
		
		for (int i = 0; i < stages.length; i++) {
			
			if (log.isDebugEnabled())
				log.debug("Stage " + i + ": " + (stages[i] ? "FOUND" : "NOT FOUND"));
			
			if (stages[i])
				result = new Result("WHO STAGE " + (i + 1));
		}
		
		return result;
	}
	
	/**
	 * @see org.openmrs.logic.Rule#getDefaultDatatype()
	 */
	public Datatype getDefaultDatatype() {
		return Datatype.CODED;
	}
	
	/**
	 * @see org.openmrs.logic.Rule#getDependencies()
	 */
	public String[] getDependencies() {
		return null;
	}
	
	/**
	 * @see org.openmrs.logic.Rule#getParameterList()
	 */
	public Set<RuleParameterInfo> getParameterList() {
		return null;
	}
	
	/**
	 * @see org.openmrs.logic.Rule#getTTL()
	 */
	public int getTTL() {
		return 0;
	}
	
}
