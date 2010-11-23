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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
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

/**
 *  
 */
public class TestOrderedRule implements Rule {
	
	private static final Log log = LogFactory.getLog(TestOrderedRule.class);
	
	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, org.openmrs.Patient,
	 *      java.util.Map)
	 */
	public Result eval(LogicContext context, Patient patient, Map<String, Object> parameters) throws LogicException {
		
		if (parameters == null)
			return new Result();
		
		if (!parameters.containsKey(RuleConstants.INCLUDED_CODED_VALUES)
		        && !parameters.containsKey(RuleConstants.EVALUATED_CONCEPT))
			new Result();
		
		String conceptName = String.valueOf(parameters.get(RuleConstants.EVALUATED_CONCEPT));
		Concept concept = ConceptRegistry.getCachedConcept(conceptName);
		if (concept == null)
			new Result();

		String included = StringUtils.defaultIfEmpty((String) parameters.get(RuleConstants.INCLUDED_CODED_VALUES), StringUtils.EMPTY);
		String[] includedConceptNames = StringUtils.split(included, ";");
		List<Concept> includedConcept = getConcepts(includedConceptNames);
		
		SummaryService service = Context.getService(SummaryService.class);
		
		LogicCriteria encounterCriteria = service.parseToken(SummaryDataSource.ENCOUNTER_TYPE).in(Collections.emptyList());

		LogicCriteria conceptCriteria = service.parseToken(SummaryDataSource.CONCEPT).equalTo(conceptName);
		Result rawResults = context.read(patient, service.getLogicDataSource("summary"), conceptCriteria.and(encounterCriteria));
		Result resultObservations = RuleUtils.consolidate(rawResults);

		LogicCriteria testedCriteria = service.parseToken(SummaryDataSource.CONCEPT).equalTo(StandardConceptConstants.TESTS_ORDERED);
		Result testedResult = context.read(patient, service.getLogicDataSource("summary"), testedCriteria.and(encounterCriteria));
		
		Result testedObservations = new Result();
		for (Result result : testedResult) {
			Concept codedValue = result.toConcept();
			if (includedConcept.contains(codedValue))
				testedObservations.add(result);
        }
		
		Result flowsheet = new Result();
		
		// Structure:
		// Main Result
		// -- Result(Value (optional), Status (optional), Datetime)
		
		while (resultObservations.size() > 0 && testedObservations.size() > 0) {
			// always get the first element of the list
			Result testResult = resultObservations.get(0);
			Result testOrder = testedObservations.get(0);
			
			Date resultDate = testResult.getResultDate();
			Date testDate = testOrder.getResultDate();
			
			Calendar oneDayAfterTest = Calendar.getInstance();
			oneDayAfterTest.setTime(testDate);
			oneDayAfterTest.add(Calendar.DATE, 1);
			
			// The assumption is:
			// - test will always ordered before the result coming to the system
			// - test and the coresponding result will have to be 24h apart max to be considered pair
			// - if there's a test but there's no result within 24h, then a "test ordered" will be displayed
			// - if there's a result but there's no result within 24h before the result, then a "no order" will be displayed
			if (testDate.before(resultDate)) {
				if (resultDate.after(oneDayAfterTest.getTime())) {
					Result result = new Result();
					result.setValueNumeric(testResult.toNumber());
					result.setValueCoded(testResult.toConcept());
					result.setValueText("No Order");
					result.setResultDate(resultDate);
					flowsheet.add(result);
					
					// remove the test and the result
					resultObservations.remove(0);
				} else {
					// test is within 24h, they are a pair
					// show: value numeric and the date
					Result result = new Result();
					result.setValueNumeric(testResult.toNumber());
					result.setValueCoded(testResult.toConcept());
					result.setResultDate(resultDate);
					flowsheet.add(result);
					
					// remove the test and the result
					resultObservations.remove(0);
					testedObservations.remove(0);
				}
			} else if (testDate.after(resultDate)) {
				// we found a result, but there's no test before this result
				// show: numeric value -- no order
				
				// test is within 24h, they are a pair
				// show: value numeric and the date
				// there's no result within 24 after the test was ordered
				// show: test ordered
				Result result = new Result();
				result.setValueText("Test Ordered");
				result.setResultDate(testDate);
				flowsheet.add(result);
				
				// remove the test
				testedObservations.remove(0);
			} else {
				// test date == result date
				// test is within 24h, they are a pair
				// show: value numeric and the date
				Result result = new Result();
				result.setValueNumeric(testResult.toNumber());
				result.setValueCoded(testResult.toConcept());
				result.setResultDate(resultDate);
				flowsheet.add(result);
				
				// remove the test and the result
				resultObservations.remove(0);
				testedObservations.remove(0);
			}
			
			if (log.isDebugEnabled()) {
				Result addedResult = flowsheet.get(flowsheet.size() - 1);
				log.debug("Added Result:");
				log.debug("[datetime: " + Context.getDateFormat().format(addedResult.getResultDate()) + "]");
				log.debug("[value : " + addedResult.toNumber() + "]");
				log.debug("[status: " + addedResult.toString() + "]");
			}
		}
		
		// only one of the following while will be executed :)
		
		while (resultObservations.size() > 0) {
			Result testResult = resultObservations.remove(0);
			Result result = new Result();
			result.setValueNumeric(testResult.toNumber());
			result.setValueCoded(testResult.toConcept());
			result.setValueText("No Order");
			result.setResultDate(testResult.getResultDate());
			flowsheet.add(result);
		}
		
		while (testedObservations.size() > 0) {
			Result testOrder = testedObservations.remove(0);
			Result result = new Result();
			result.setValueText("Test Ordered");
			result.setResultDate(testOrder.getResultDate());
			flowsheet.add(result);
		}
		
		Result slicedResult = RuleUtils.sliceResult(flowsheet, 5);
		
		Collections.reverse(slicedResult);
		
		return slicedResult;
	}
	
	/**
	 * @see org.openmrs.logic.Rule#getDefaultDatatype()
	 */
	public Datatype getDefaultDatatype() {
		return Datatype.TEXT;
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
