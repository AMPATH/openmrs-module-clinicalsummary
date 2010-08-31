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
package org.openmrs.module.clinicalsummary.rule;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
import org.openmrs.module.clinicalsummary.rule.ResultComparator.CompareProperty;

/**
 * 
 */
public class ProblemAddedRule implements Rule {
	
	private static final Log log = LogFactory.getLog(ProblemAddedRule.class);
	
	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, org.openmrs.Patient,
	 *      java.util.Map)
	 */
	public Result eval(LogicContext context, Patient patient, Map<String, Object> parameters) throws LogicException {
		
		SummaryService service = Context.getService(SummaryService.class);
		
		List<String> typeNames = Arrays.asList(TypeConstants.ADULT_INITIAL,
		    TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
		    TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN,
		    TypeConstants.PEDS_NONCLINICAL);
		LogicCriteria encounterCriteria = service.parseToken(SummaryDataSource.ENCOUNTER_TYPE).in(typeNames);

		LogicCriteria addedCriteria = service.parseToken(SummaryDataSource.CONCEPT).equalTo(StandardConceptConstants.PROBLEM_ADDED);
		Result addedResults = context.read(patient, service.getLogicDataSource("summary"), addedCriteria.and(encounterCriteria));
		
		LogicCriteria resolvedCriteria = service.parseToken(SummaryDataSource.CONCEPT).equalTo(StandardConceptConstants.PROBLEM_RESOLVED);
		Result resolvedResults = context.read(patient, service.getLogicDataSource("summary"), resolvedCriteria.and(encounterCriteria));
		
		Concept clinicalConcept = ConceptRegistry.getCachedConcept(StandardConceptConstants.CLINICAL_SUMMARY_PROBLEMS);
		
		if (log.isDebugEnabled())
			log.debug("Patient: " + patient.getPatientId() + ", resolved results: " + resolvedResults);
		
		// this will contains all resolved problems that are not in the un-resolvable list
		Map<Concept, Result> conceptResolveds = new HashMap<Concept, Result>();
		for (Result resolvedResult : resolvedResults) {
			Concept concept = resolvedResult.toConcept();
			if (ConceptRegistry.isCachedParentOf(clinicalConcept, concept)) {
				Result resolveds = conceptResolveds.get(concept);
				if (resolveds == null) {
					resolveds = new Result();
					conceptResolveds.put(concept, resolveds);
				}
				resolveds.add(resolvedResult);
			}
		}
		
		if (log.isDebugEnabled())
			log.debug("Patient: " + patient.getPatientId() + ", problem list: " + addedResults);
		
		Map<Concept, Result> conceptProblems = new HashMap<Concept, Result>();
		for (Result addedResult : addedResults) {
			Concept concept = addedResult.toConcept();
			
			// this problem never resolved
			if (!conceptResolveds.containsKey(concept)) {
				Result observations = conceptProblems.get(concept);
				if (observations == null) {
					observations = new Result();
					conceptProblems.put(concept, observations);
				}
				observations.add(addedResult);
			} else {
				// the problem is resolvable (there's a the same concept in the resolved map)
				// we need to check the date of the resolved.
				// if resolved come before added problem, then it's not resolving the added problem (add to the problems list)
				// if resolved come after added problem, then it's cancelling the added problem (remove the resolved and don't put the added in the list)
				Result resolveds = conceptResolveds.get(concept);
				while (!resolveds.isEmpty()) {
					Result resolved = resolveds.get(0);
					if (resolved.getResultDate().before(addedResult.getResultDate())) {
						Result problemObservations = conceptProblems.get(concept);
						if (problemObservations == null) {
							problemObservations = new Result();
							conceptProblems.put(concept, problemObservations);
						}
						problemObservations.add(addedResult);
						break;
					} else {
						resolveds.remove(0);
					}
				}
			}
		}
		
		Result problems = new Result();
		for (Concept concept : conceptProblems.keySet()) {
			Result observations = conceptProblems.get(concept);
			Result conceptResult = new Result();
			for (Result result: observations)
				conceptResult.add(result);
			problems.add(conceptResult);
		}
		
		Collections.sort(problems, new ResultComparator(CompareProperty.DATETIME));
		
		return problems;
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
