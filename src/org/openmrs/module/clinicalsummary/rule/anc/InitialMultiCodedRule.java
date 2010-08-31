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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
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
import org.openmrs.module.clinicalsummary.encounter.TypeConstants;
import org.openmrs.module.clinicalsummary.rule.RuleConstants;

/**
 * 
 */
public class InitialMultiCodedRule implements Rule {
	
	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, org.openmrs.Patient,
	 *      java.util.Map)
	 */
	@Override
	public Result eval(LogicContext context, Patient patient, Map<String, Object> parameters) throws LogicException {
		
		String conceptName = String.valueOf(parameters.get(RuleConstants.EVALUATED_CONCEPT));
		
		// we use cast here instead of toString because it might be null
		// and we don't use String.valueOf because it will return "null" when the argument is null
		String included = StringUtils.defaultIfEmpty((String) parameters.get(RuleConstants.INCLUDED_CODED_VALUES), StringUtils.EMPTY);
		String[] includedConceptNames = StringUtils.split(included, ";");
		List<Concept> includedConcept = getConcepts(includedConceptNames);
		
		Result result = new Result();
		
		SummaryService service = Context.getService(SummaryService.class);
		
		LogicCriteria conceptCriteria = service.parseToken(SummaryDataSource.CONCEPT).equalTo(conceptName);
		LogicCriteria encounterCriteria = service.parseToken(SummaryDataSource.ENCOUNTER_TYPE).equalTo(
		    TypeConstants.ANC_INITIAL).last();
		LogicCriteria criteria = conceptCriteria.and(encounterCriteria);
		Result obsResults = context.read(patient, service.getLogicDataSource("summary"), criteria);
		
		for (Result obsResult : obsResults) {
			// if nothing in the include and exclude, then that means get all the value coded
			if (includedConcept.contains(obsResult.toConcept()))
				result.add(obsResult);
		}
		return result;
	}
	
	/**
	 * @param conceptNames
	 * @return
	 */
	private List<Concept> getConcepts(String[] conceptNames) {
		List<Concept> concepts = new ArrayList<Concept>();
		for (String conceptName : conceptNames)
			concepts.add(ConceptRegistry.getCachedConcept(StringUtils.strip(conceptName)));
		return concepts;
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
