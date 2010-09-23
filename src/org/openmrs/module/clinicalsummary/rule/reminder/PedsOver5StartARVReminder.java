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
package org.openmrs.module.clinicalsummary.rule.reminder;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
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
import org.openmrs.module.clinicalsummary.rule.ARVMedicationsRule;
import org.openmrs.module.clinicalsummary.rule.peds.PedsWHOStageRule;
import org.openmrs.util.OpenmrsUtil;

/**
 *
 */
public class PedsOver5StartARVReminder implements Rule {
	
	private static final Log log = LogFactory.getLog(PedsOver5StartARVReminder.class);
	
	private static final String REMINDER_TEXT = "Consider starting ARV Meds. Pt &gt; 5 yrs with ";
	
	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, org.openmrs.Patient,
	 *      java.util.Map)
	 */
	@Override
	public Result eval(LogicContext context, Patient patient, Map<String, Object> parameters) throws LogicException {
		
		Result reminder = new Result();
		
		Date birthdate = patient.getBirthdate();
		Calendar birthdateCalendar = Calendar.getInstance();
		birthdateCalendar.setTime(birthdate);
		// 5 years after birthdate
		birthdateCalendar.setTime(birthdate);
		birthdateCalendar.add(Calendar.YEAR, 5);
		Date fiveYears = birthdateCalendar.getTime();
		
		Date now = new Date();
		// only process if the patient is at least 18 months 
		if (now.after(fiveYears)) {
			
			ARVMedicationsRule arvMedicationsRule = new ARVMedicationsRule();
			Result arvResults = arvMedicationsRule.eval(context, patient, parameters);
			
			if (arvResults.isEmpty()) {
				birthdateCalendar.setTime(birthdate);
				birthdateCalendar.add(Calendar.MONTH, 18);
				Date referenceDate = birthdateCalendar.getTime();
				
				String dnaElisa = "";
				
				Concept positiveConcept = ConceptRegistry.getCachedConcept(StandardConceptConstants.POSITIVE);
				
				SummaryService service = Context.getService(SummaryService.class);
				
				LogicCriteria conceptCriteria = service.parseToken(SummaryDataSource.CONCEPT).equalTo(StandardConceptConstants.ELISA_NAME);
				LogicCriteria encounterCriteria = service.parseToken(SummaryDataSource.ENCOUNTER_TYPE).in(Collections.emptyList());
				LogicCriteria criteria = conceptCriteria.and(encounterCriteria);
				
				Result obsResult = context.read(patient, service.getLogicDataSource("summary"), criteria);
				
				if (log.isDebugEnabled())
					log.debug("Patient: " + patient.getPatientId() + ", elisa result: " + obsResult);
				
				// check if we have negative or positive
				boolean elisaPositive = false;
				for (Result result : obsResult) {
					if (result.getResultDate().after(referenceDate))
						if (OpenmrsUtil.nullSafeEquals(positiveConcept, result.toConcept())) {
							elisaPositive = true;
							break;
						}
				}
				
				birthdateCalendar.setTime(birthdate);
				birthdateCalendar.add(Calendar.WEEK_OF_YEAR, 4);
				referenceDate = birthdateCalendar.getTime();
				
				conceptCriteria = service.parseToken(SummaryDataSource.CONCEPT).equalTo(StandardConceptConstants.DNA_PCR_NAME);
				encounterCriteria = service.parseToken(SummaryDataSource.ENCOUNTER_TYPE).in(Collections.emptyList());
				criteria = conceptCriteria.and(encounterCriteria);
				
				obsResult = context.read(patient, service.getLogicDataSource("summary"), criteria);
				
				if (log.isDebugEnabled())
					log.debug("Patient: " + patient.getPatientId() + ", dna pcr result: " + obsResult);
				
				// check if we have negative or positive
				boolean dnaPositive = false;
				for (Result result : obsResult) {
					if (result.getResultDate().after(referenceDate))
						if (OpenmrsUtil.nullSafeEquals(positiveConcept, result.toConcept())) {
							dnaPositive = true;
							break;
						}
				}
				
				if (elisaPositive || dnaPositive) {

					conceptCriteria = service.parseToken(SummaryDataSource.CONCEPT).equalTo(StandardConceptConstants.CD4_PERCENT);
					encounterCriteria = service.parseToken(SummaryDataSource.ENCOUNTER_TYPE).in(Collections.emptyList());
					criteria = conceptCriteria.and(encounterCriteria);
					
					obsResult = context.read(patient, service.getLogicDataSource("summary"), criteria);
					boolean cd4Percent = false;
					if (!obsResult.isEmpty()) {
						Result latestCD4Percent = obsResult.latest();
						if (latestCD4Percent.toNumber() < 20)
							cd4Percent = true;
					}
					
					conceptCriteria = service.parseToken(SummaryDataSource.CONCEPT).equalTo(StandardConceptConstants.CD4_NAME);
					encounterCriteria = service.parseToken(SummaryDataSource.ENCOUNTER_TYPE).in(Collections.emptyList());
					criteria = conceptCriteria.and(encounterCriteria);
					
					obsResult = context.read(patient, service.getLogicDataSource("summary"), criteria);
					boolean cd4Count = false;
					if (!obsResult.isEmpty()) {
						Result latestCD4 = obsResult.latest();
						if (latestCD4.toNumber() < 500)
							cd4Count = true;
					}
					
					PedsWHOStageRule pedsWHOStageRule = new PedsWHOStageRule();
					Result pedsWHOStage = pedsWHOStageRule.eval(context, patient, parameters);
					boolean whoStage = false;
					if (!pedsWHOStage.isEmpty())
						if (pedsWHOStage.toNumber() == 3 || pedsWHOStage.toNumber() == 4)
							whoStage = true;
					
					if (cd4Percent || cd4Count || whoStage) {
						String message = " positive";
						if (elisaPositive)
							message = message + " ELISA,";
						if (dnaPositive)
							message = message + " DNA PCR,";
						message = message.substring(0, message.length() - 1);
						message = message + " AND";
						if (cd4Percent)
							message = message + " CD4 Percent  &lt; 20,";
						if (cd4Count)
							message = message + " CD4 Count &lt; 500,";
						if (whoStage)
							message = message + " WHO Stage is 3 or 4,";
						message = message.substring(0, message.length() - 1);
						reminder = new Result(REMINDER_TEXT + message);
					}
					
				}
				
			}
		}
		
		return reminder;
	}
	
	/**
	 * @see org.openmrs.logic.Rule#getDefaultDatatype()
	 */
	@Override
	public Datatype getDefaultDatatype() {
		return Datatype.TEXT;
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
