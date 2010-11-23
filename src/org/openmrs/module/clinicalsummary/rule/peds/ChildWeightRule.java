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

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.openmrs.module.clinicalsummary.rule.RuleConstants;
import org.openmrs.module.clinicalsummary.rule.RuleUtils;
import org.openmrs.module.clinicalsummary.rule.reminder.peds.ScoreUtils;

/**
 *
 */
public class ChildWeightRule implements Rule {
	
	private static final Log log = LogFactory.getLog(ChildWeightRule.class);
	
	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, org.openmrs.Patient,
	 *      java.util.Map)
	 */
	@Override
	public Result eval(LogicContext context, Patient patient, Map<String, Object> parameters) throws LogicException {
		Result result = new Result();
		
		String conceptName = String.valueOf(parameters.get(RuleConstants.EVALUATED_CONCEPT));
		
		SummaryService service = Context.getService(SummaryService.class);
		
		LogicCriteria conceptCriteria = service.parseToken(SummaryDataSource.CONCEPT).equalTo(conceptName);
		LogicCriteria encounterCriteria = service.parseToken(SummaryDataSource.ENCOUNTER_TYPE).in(Collections.emptyList());
		
		Result obsResults = context.read(patient, service.getLogicDataSource("summary"), conceptCriteria.and(encounterCriteria));
		Result consolidatedResults = RuleUtils.consolidate(obsResults);
		
		if (log.isDebugEnabled())
			log.debug("Started arv side effect observations for patient: " + patient.getPatientId() + " is: " + obsResults);
		
		Result numericObsResults = RuleUtils.sliceResult(consolidatedResults, 5);
		
		for (Result numericObsResult : numericObsResults) {
			Result numericResult = new Result();
			numericResult.setResultDate(numericObsResult.getResultDate());
			numericResult.setValueNumeric(numericObsResult.toNumber());
			DecimalFormat twoDecimal = new DecimalFormat("#.##");
			numericResult.setValueText(twoDecimal.format(calculatePercentile(patient, numericObsResult.getResultDate(), numericObsResult.toNumber())));
			result.add(numericResult);
		}
		
		Collections.reverse(result);
		
		return result;
	}
	
	private Double calculatePercentile(Patient patient, Date asOfDate, Double weight) {
		
		SummaryService service = Context.getService(SummaryService.class);
		
		Date birthDate = patient.getBirthdate();
		
		Calendar birthCalendar = Calendar.getInstance();
		birthCalendar.setTime(birthDate);
		birthCalendar.add(Calendar.WEEK_OF_YEAR, 13);
		
		Calendar todayCalendar = Calendar.getInstance();
		todayCalendar.setTime(asOfDate);
		
		// today is after week 13, then we need to calculate the age in month
		Double percentile = 0D;
		String gender = (StringUtils.equalsIgnoreCase("male", patient.getGender()) || StringUtils.equalsIgnoreCase("M",
		    patient.getGender())) ? "Male" : "Female";
		if (todayCalendar.after(birthCalendar)) {
			birthCalendar.setTime(birthDate);
			
			int birthYear = birthCalendar.get(Calendar.YEAR);
			int todayYear = todayCalendar.get(Calendar.YEAR);
			
			int ageInYear = todayYear - birthYear;
			
			int birthMonth = birthCalendar.get(Calendar.MONTH);
			int todayMonth = todayCalendar.get(Calendar.MONTH);
			
			int ageInMonth = todayMonth - birthMonth;
			if (ageInMonth < 0) {
				ageInMonth = birthMonth + 1;
				ageInYear--;
			}
			ageInMonth = ageInMonth + (ageInYear * 12);
			
			if (log.isDebugEnabled())
				log.debug("Patient: " + patient.getPatientId() + ", age in month: " + ageInMonth);
			
			WeightAgeStandard standard = service.getWeightAgeStandard(ageInMonth, "Month", gender);
			if (standard != null) {
				Double zScore = ScoreUtils.zScore(standard.getlValue(), standard.getmValue(), standard.getsValue(), weight);
				percentile = ScoreUtils.percentile(standard.getlValue(), standard.getmValue(), standard.getsValue(), zScore);
			}
		} else {
			birthCalendar.setTime(birthDate);
			
			long diff = todayCalendar.getTimeInMillis() - birthCalendar.getTimeInMillis();
			
			long week = 60 * 60 * 24 * 7;
			int ageInWeek = (int) (diff / week);
			// if the mod if more than half of the week, then round it up
			if (diff % week > week / 2)
				ageInWeek++;
			
			if (log.isDebugEnabled())
				log.debug("Patient: " + patient.getPatientId() + ", age in week: " + ageInWeek);
			
			WeightAgeStandard standard = service.getWeightAgeStandard(ageInWeek, "Week", gender);
			if (standard != null) {
				Double zScore = ScoreUtils.zScore(standard.getlValue(), standard.getmValue(), standard.getsValue(), weight);
				percentile = ScoreUtils.percentile(standard.getlValue(), standard.getmValue(), standard.getsValue(), zScore);
			}
		}
		
		return percentile;
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
