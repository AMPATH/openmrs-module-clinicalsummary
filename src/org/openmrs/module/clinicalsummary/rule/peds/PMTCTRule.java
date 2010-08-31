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
import org.openmrs.module.clinicalsummary.concept.StandardConceptConstants;
import org.openmrs.module.clinicalsummary.encounter.TypeConstants;
import org.openmrs.util.OpenmrsUtil;

/**
 * 
 */
public class PMTCTRule implements Rule {
	
	private static final Log log = LogFactory.getLog(PMTCTRule.class);
	
	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, org.openmrs.Patient,
	 *      java.util.Map)
	 */
	public Result eval(LogicContext context, Patient patient, Map<String, Object> parameters) throws LogicException {
		Result pmtct = new Result();
		
		SummaryService service = Context.getService(SummaryService.class);
		
		List<String> typeNames = Arrays.asList(TypeConstants.ADULT_INITIAL,
		    TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
		    TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN,
		    TypeConstants.PEDS_NONCLINICAL);
		LogicCriteria encounterCriteria = service.parseToken(SummaryDataSource.ENCOUNTER_TYPE).in(typeNames);

		LogicCriteria maternalCriteria = service.parseToken(SummaryDataSource.CONCEPT).equalTo(StandardConceptConstants.MATERNAL_ARV_HISTORY_NAME);
		Result maternalResults = context.read(patient, service.getLogicDataSource("summary"), maternalCriteria.and(encounterCriteria));
		
		LogicCriteria partumCriteria = service.parseToken(SummaryDataSource.CONCEPT).equalTo(StandardConceptConstants.PARTUM_ARV_USED_NAME);
		Result partumResults = context.read(patient, service.getLogicDataSource("summary"), partumCriteria.and(encounterCriteria));
		
		LogicCriteria partumDosingCriteria = service.parseToken(SummaryDataSource.CONCEPT).equalTo(StandardConceptConstants.PARTUM_PERIOD_NAME);
		Result partumDosingResults = context.read(patient, service.getLogicDataSource("summary"), partumDosingCriteria.and(encounterCriteria));
		
		LogicCriteria doseQuantificationCriteria = service.parseToken(SummaryDataSource.CONCEPT).equalTo(StandardConceptConstants.ARV_DOSE_NAME);
		Result doseQuantificationResults = context.read(patient, service.getLogicDataSource("summary"), doseQuantificationCriteria.and(encounterCriteria));
		
		LogicCriteria treatmentWeeksCriteria = service.parseToken(SummaryDataSource.CONCEPT).equalTo(StandardConceptConstants.TREATMENT_WEEKS_NAME);
		Result treatmentWeeksResults = context.read(patient, service.getLogicDataSource("summary"), treatmentWeeksCriteria.and(encounterCriteria));
		
		int[] obsVisited = new int[4];
		
		// the structure is:
		// Main Result:
		// -- Per Drug Result
		//    -- Drug Name
		//    -- Drug Dose (optional)
		
		for (Result maternalResult : maternalResults) {
			Obs maternalObs = (Obs) maternalResult.getResultObject();
			Result historyResult = new Result();
			
			while (obsVisited[0] < partumResults.size()) {
				Result partumResult = partumResults.get(obsVisited[0]);
				Obs partumObs = (Obs) partumResult.getResultObject();
				if (OpenmrsUtil.nullSafeEquals(maternalObs, partumObs.getObsGroup())) {
					// only include this immunization if we get a drug (dose is optional)
					historyResult.add(partumResult);
					pmtct.add(historyResult);
				} else
					break;
				obsVisited[0]++;
			}
			
			Result doseResult = new Result();
			Result quantificationResult = new Result();
			Result treatmentResult = new Result();
			
			// i hate this repetitions :)
			
			while (obsVisited[1] < partumDosingResults.size()) {
				Result partumDosingResult = partumDosingResults.get(obsVisited[1]);
				Obs doseObs = (Obs) partumDosingResult.getResultObject();
				if (OpenmrsUtil.nullSafeEquals(maternalObs, doseObs.getObsGroup()))
					doseResult.add(partumDosingResult);
				else
					break;
				obsVisited[1]++;
			}
			
			while (obsVisited[2] < doseQuantificationResults.size()) {
				Result doseQuantificationResult = doseQuantificationResults.get(obsVisited[2]);
				Obs doseQuantificationObs = (Obs) doseQuantificationResult.getResultObject();
				if (OpenmrsUtil.nullSafeEquals(maternalObs, doseQuantificationObs.getObsGroup()))
					quantificationResult = doseQuantificationResult;
				else
					break;
				obsVisited[2]++;
			}
			
			while (obsVisited[3] < treatmentWeeksResults.size()) {
				Result treatmentWeeksResult = treatmentWeeksResults.get(obsVisited[3]);
				Obs treatmentObs = (Obs) treatmentWeeksResult.getResultObject();
				if (OpenmrsUtil.nullSafeEquals(maternalObs, treatmentObs.getObsGroup()))
					treatmentResult = treatmentWeeksResult;
				else
					break;
				obsVisited[3]++;
			}
			
			historyResult.add(doseResult);
			historyResult.add(quantificationResult);
			historyResult.add(treatmentResult);
		}
		
		if (log.isDebugEnabled())
			log.debug("Patient: " + patient.getPatientId() + ", pmtct is: " + pmtct);
		
		return pmtct;
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
