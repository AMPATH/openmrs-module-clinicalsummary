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
package org.openmrs.module.clinicalsummary.rule.reminder.peds;

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
import org.openmrs.util.OpenmrsUtil;

/**
 *
 */
public class BabyStartARVReminderRule implements Rule {
	
	private static final Log log = LogFactory.getLog(BabyStartARVReminderRule.class);
	
	private static final String REMINDER_TEXT = "Consider starting ARV Medications. Pt with positive HIV PCR";
	
	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, org.openmrs.Patient,
	 *      java.util.Map)
	 */
	@Override
	public Result eval(LogicContext context, Patient patient, Map<String, Object> parameters) throws LogicException {
		
		Result reminder = new Result();
		
		Date birthdate = patient.getBirthdate();
		if (birthdate != null) {

			Calendar birthdateCalendar = Calendar.getInstance();
			birthdateCalendar.setTime(birthdate);
			// 18 months after birthdate
			birthdateCalendar.add(Calendar.MONTH, 18);
			Date referenceDate = birthdateCalendar.getTime();
			
			Date now = new Date();
			if (referenceDate.after(now)) {
				// 4 weeks after birthdate
				birthdateCalendar.setTime(birthdate);
				birthdateCalendar.add(Calendar.WEEK_OF_YEAR, 4);
				referenceDate = birthdateCalendar.getTime();
				
				Concept positiveConcept = ConceptRegistry.getCachedConcept(StandardConceptConstants.POSITIVE);
				
				SummaryService service = Context.getService(SummaryService.class);
				
				LogicCriteria conceptCriteria = service.parseToken(SummaryDataSource.CONCEPT).equalTo(StandardConceptConstants.DNA_PCR_NAME);
				LogicCriteria encounterCriteria = service.parseToken(SummaryDataSource.ENCOUNTER_TYPE).in(Collections.emptyList());
				LogicCriteria criteria = conceptCriteria.and(encounterCriteria);
				
				Result obsResult = context.read(patient, service.getLogicDataSource("summary"), criteria);
				
				if (log.isDebugEnabled())
					log.debug("Patient: " + patient.getPatientId() + ", elisa result: " + obsResult);
				
				// check if we have negative or positive
				boolean dnaExist = false;
				for (Result result : obsResult) {
					if (result.getResultDate().after(referenceDate))
						if (OpenmrsUtil.nullSafeEquals(positiveConcept, result.toConcept())) {
							dnaExist = true;
							break;
						}
				}
				
				if (dnaExist) {
					ARVMedicationsRule arvMedicationsRule = new ARVMedicationsRule();
					Result result = arvMedicationsRule.eval(context, patient, parameters);
					
					if (log.isDebugEnabled())
						log.debug("Patient: " + patient.getPatientId() + ", arv result: " + result);
					
					if (result.isEmpty())
						reminder = new Result(REMINDER_TEXT);
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
