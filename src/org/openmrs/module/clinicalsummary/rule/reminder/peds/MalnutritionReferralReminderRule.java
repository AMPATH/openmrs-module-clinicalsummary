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
import org.openmrs.module.clinicalsummary.WeightAgeStandard;
import org.openmrs.module.clinicalsummary.cache.SummaryDataSource;
import org.openmrs.module.clinicalsummary.concept.ConceptRegistry;
import org.openmrs.module.clinicalsummary.concept.StandardConceptConstants;
import org.openmrs.util.OpenmrsUtil;

/**
 *
 */
public class MalnutritionReferralReminderRule implements Rule {
	
	private static final Log log = LogFactory.getLog(MalnutritionReferralReminderRule.class);
	
	private static final String REMINDER_TEXT = "Refer for Nutritional Support. Last Z-score in AMRS -1.5 or less.";
	
	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, org.openmrs.Patient,
	 *      java.util.Map)
	 */
	@Override
	public Result eval(LogicContext context, Patient patient, Map<String, Object> parameters) throws LogicException {
		
		Result reminder = new Result();
		
		String conceptName = StandardConceptConstants.WEIGHT;
		
		SummaryService service = Context.getService(SummaryService.class);
		
		LogicCriteria conceptCriteria = service.parseToken(SummaryDataSource.CONCEPT).equalTo(conceptName);
		LogicCriteria encounterCriteria = service.parseToken(SummaryDataSource.ENCOUNTER_TYPE).in(Collections.emptyList());
		
		Result obsResults = context.read(patient, service.getLogicDataSource("summary"), conceptCriteria.and(encounterCriteria));
		if (obsResults.isEmpty()) {
			Result latestWeightResult = obsResults.get(0);
			Double weight = latestWeightResult.toNumber();
			Double zScore = calculateZScore(patient, weight);
			if (zScore >= -3 && zScore < -1.5) {
				Result earliestResult = null;
				for (Result result : latestWeightResult) {
					weight = result.toNumber();
					zScore = calculateZScore(patient, weight);
					if (zScore >= -3 && zScore < -1.5) {
						earliestResult = result;
						continue;
					}
					if (earliestResult != null)
						break;
				}
				
				if (earliestResult != null) {
					
					Date earliestResultDate = earliestResult.getResultDate();
					
					Concept nutritionSupportConcept = ConceptRegistry.getCachedConcept(StandardConceptConstants.NUTRITIONAL_SUPPORT);
					
					LogicCriteria testedCriteria = service.parseToken(SummaryDataSource.CONCEPT).equalTo(StandardConceptConstants.REFERRALS_ORDERED);
					Result referralResults = context.read(patient, service.getLogicDataSource("summary"), testedCriteria.and(encounterCriteria));
					
					boolean referred = false;
					for (Result result : referralResults) {
						Concept codedValue = result.toConcept();
						if (earliestResultDate.before(result.getResultDate()) && OpenmrsUtil.nullSafeEquals(nutritionSupportConcept, codedValue))
							referred = true;
					}
					
					if (!referred)
						reminder = new Result(REMINDER_TEXT);
				}
			}
		}
		
		return reminder;
	}
	
	private Double calculateZScore(Patient patient, Double weight) {
		
		SummaryService service = Context.getService(SummaryService.class);
		
		Date birthDate = patient.getBirthdate();
		
		Calendar birthCalendar = Calendar.getInstance();
		birthCalendar.setTime(birthDate);
		birthCalendar.add(Calendar.WEEK_OF_YEAR, 13);
		
		Calendar todayCalendar = Calendar.getInstance();
		
		// today is after week 13, then we need to calculate the age in month
		Double zScore = 0D;
		String gender = (StringUtils.equalsIgnoreCase("male", patient.getGender()) || StringUtils.equalsIgnoreCase("M",
		    patient.getGender())) ? "Male" : "Female";
		if (todayCalendar.after(birthCalendar)) {
			birthCalendar.setTime(birthDate);
			
			int birthYear = birthCalendar.get(Calendar.YEAR);
			int todayYear = todayCalendar.get(Calendar.YEAR);
			
			int ageInYear = todayYear - birthYear;
			
			int birthMonth = birthCalendar.get(Calendar.MONTH);
			int todayMonth = todayCalendar.get(Calendar.MONTH);
			
			int age = todayMonth - birthMonth;
			if (age < 0) {
				age = birthMonth + 1;
				ageInYear--;
			}
			age = age + (ageInYear * 12);
			
			if (log.isDebugEnabled())
				log.debug("Patient: " + patient.getPatientId() + ", age in week: " + age);
			
			WeightAgeStandard standard = service.getWeightAgeStandard(age, "Month", gender);
			if (standard != null)
				zScore = ScoreUtils.zScore(standard.getlValue(), standard.getmValue(), standard.getsValue(), weight);
		} else {
			birthCalendar.setTime(birthDate);
			
			long diff = todayCalendar.getTimeInMillis() - birthCalendar.getTimeInMillis();
			
			long week = 60 * 60 * 24 * 7;
			int age = (int) (diff / week);
			// if the mod if more than half of the week, then round it up
			if (diff % week > week / 2)
				age++;
			
			if (log.isDebugEnabled())
				log.debug("Patient: " + patient.getPatientId() + ", age in week: " + age);
			
			WeightAgeStandard standard = service.getWeightAgeStandard(age, "Week", gender);
			if (standard != null)
				zScore = ScoreUtils.zScore(standard.getlValue(), standard.getmValue(), standard.getsValue(), weight);
		}
		return zScore;
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
