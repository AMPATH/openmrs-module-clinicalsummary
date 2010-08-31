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
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
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
import org.openmrs.module.clinicalsummary.concept.ConceptRegistry;
import org.openmrs.module.clinicalsummary.concept.StandardConceptConstants;
import org.openmrs.module.clinicalsummary.encounter.TypeConstants;
import org.openmrs.util.OpenmrsUtil;

/**
 * 
 */
public class HIVRxAdherenceRule implements Rule {
	
	private static final Log log = LogFactory.getLog(HIVRxAdherenceRule.class);
	
	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, org.openmrs.Patient,
	 *      java.util.Map)
	 */
	public Result eval(LogicContext context, Patient patient, Map<String, Object> parameters) throws LogicException {
		
		Result result = new Result();
		
		Set<Date> formattedDates = new TreeSet<Date>();
		
		// time frame for the adherence is six month
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -6);
		Date sixMonthsAgo = calendar.getTime();
		
		boolean missingData = false;
		boolean imperfect = false;
		
		Concept arvAdherence = ConceptRegistry.getCachedConcept(StandardConceptConstants.ADHERENCE_ARV_DRUGS);
		Concept allAdherence = ConceptRegistry.getCachedConcept(StandardConceptConstants.ADHERENCE_ALL);
		
		SummaryService service = Context.getService(SummaryService.class);
		
		List<String> typeNames = Arrays.asList(TypeConstants.ADULT_INITIAL,
		    TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
		    TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN,
		    TypeConstants.PEDS_NONCLINICAL);
		LogicCriteria encounterCriteria = service.parseToken(SummaryDataSource.ENCOUNTER_TYPE).in(typeNames);
		
		Result encounterResults = context.read(patient, service.getLogicDataSource("summary"), encounterCriteria);
		
		LogicCriteria weekCriteria = service.parseToken(SummaryDataSource.CONCEPT).equalTo(StandardConceptConstants.ARV_WEEK_ADHERENCE);
		Result weekResults = context.read(patient, service.getLogicDataSource("summary"), weekCriteria.and(encounterCriteria));
		
		LogicCriteria monthCriteria = service.parseToken(SummaryDataSource.CONCEPT).equalTo(StandardConceptConstants.MONTH_DRUG_ADHERENCE);
		Result monthResults = context.read(patient, service.getLogicDataSource("summary"), monthCriteria.and(encounterCriteria));
		
		for (Result encounterResult : encounterResults) {
			
			Encounter encounter = (Encounter) encounterResult.getResultObject();
			
			if (encounter.getEncounterDatetime().before(sixMonthsAgo))
				break;
			
			boolean foundWeek = false;
			boolean foundMonth = false;
			
			foundWeek = true;
			for (Result weekResult : weekResults) {
				Obs weekObs = (Obs) weekResult.getResultObject();
				Concept valueCoded = weekObs.getValueCoded();
				
				if (log.isDebugEnabled())
					log.debug("Patient: " + patient.getPatientId() + ", week adherence: " + valueCoded);
				
				if (OpenmrsUtil.nullSafeEquals(encounter, weekObs.getEncounter())) {
					foundWeek = true;
					
					if (!OpenmrsUtil.nullSafeEquals(allAdherence, valueCoded))
						imperfect = true;
					
					break;
				}
			}
			
			for (Result monthResult : monthResults) {
				Obs monthObs = (Obs) monthResult.getResultObject();
				Concept valueCoded = monthObs.getValueCoded();
				
				if (log.isDebugEnabled())
					log.debug("Patient: " + patient.getPatientId() + ", week adherence: " + valueCoded);
				
				if (OpenmrsUtil.nullSafeEquals(encounter, monthObs.getEncounter())) {
					foundMonth = true;
					
					if (!OpenmrsUtil.nullSafeEquals(arvAdherence, valueCoded))
						imperfect = true;
					
					break;
				}
			}
			
			if (!foundMonth || !foundWeek) {
				missingData = true;
				formattedDates.add(encounter.getEncounterDatetime());
			}
		}
		
		if (missingData) {
			// maybe if more than two just add a "x more" words
			StringBuffer buffer = new StringBuffer();
			
			// format of the message: Missing Data - 01/22/2010 01/23/2010 (10 More)
			
			buffer.append("Missing Data - ");
			
			int counter = 0;
			Iterator<Date> iterator = formattedDates.iterator();
			while (iterator.hasNext() && counter < 2) {
				Date date = iterator.next();
				String formattedDate = Context.getDateFormat().format(date);
				buffer.append(formattedDate);
				if (counter < 1 && iterator.hasNext())
					buffer.append(", ");
				counter++;
			}
			
			if (formattedDates.size() > 2)
				buffer.append("(").append(formattedDates.size() - 2).append(" More").append(")");
			
			result.setValueText(buffer.toString());
		} else if (imperfect)
			result.setValueText("Not Perfect");
		else if (!missingData && !imperfect)
			result.setValueText("Perfect");
		
		return result;
	}
	
	/**
	 * @see org.openmrs.logic.Rule#getDefaultDatatype()
	 */
	public Datatype getDefaultDatatype() {
		return Datatype.TEXT;
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
