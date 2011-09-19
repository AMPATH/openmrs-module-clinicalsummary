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

package org.openmrs.module.clinicalsummary.rule.pediatric;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.rule.EvaluableRule;

/**
 * No parameter needed
 */
public class AgeWithUnitRule extends EvaluableRule {

	private static final Log log = LogFactory.getLog(AgeWithUnitRule.class);

	public static final String TOKEN = "Age With Unit";

	public static final String REFERENCE_DATE = "reference.date";

	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
	 */
	@Override
	protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) throws LogicException {

		// just make sure that this is the patient from the database
		Patient patient = Context.getPatientService().getPatient(patientId);
		Date birthdate = patient.getBirthdate();

		if (log.isDebugEnabled())
			log.debug("Patient: " + patient.getPatientId() + ", birthdate: " + birthdate);

		Result ageResult = new Result();

		if (birthdate != null) {

			Calendar todayCalendar = Calendar.getInstance();
			Date referenceDate = (Date) parameters.get(REFERENCE_DATE);
			if (referenceDate != null)
				todayCalendar.setTime(referenceDate);

			Calendar birthCalendar = Calendar.getInstance();
			birthCalendar.setTime(birthdate);

			int birthYear = birthCalendar.get(Calendar.YEAR);
			int todayYear = todayCalendar.get(Calendar.YEAR);

			int ageInYear = todayYear - birthYear;

			int birthMonth = birthCalendar.get(Calendar.MONTH);
			int todayMonth = todayCalendar.get(Calendar.MONTH);

			int ageInMonth = todayMonth - birthMonth;
			if (ageInMonth < 0) {
				// birth month is bigger, the decrease the year
				ageInYear--;
				ageInMonth = 12 - birthMonth + todayMonth;
			}

			int birthDay = birthCalendar.get(Calendar.DATE);
			int todayDay = todayCalendar.get(Calendar.DATE);

			int ageInDay = todayDay - birthDay;
			if (ageInDay < 0) {
				ageInMonth--;
				birthCalendar.add(Calendar.MONTH, -1);
				ageInDay = birthCalendar.getActualMaximum(Calendar.DATE) - birthDay + todayDay;

				if (ageInDay > birthCalendar.getActualMaximum(Calendar.DATE) / 2)
					ageInMonth++;
			}

			if (ageInYear != 0)
				ageResult.add(new Result(ageInYear + " Years"));
			if (ageInMonth != 0)
				ageResult.add(new Result(ageInMonth + " Months"));

		}

		return ageResult;
	}

	/**
	 * @see org.openmrs.logic.Rule#getDependencies()
	 */
	@Override
	public String[] getDependencies() {
		return new String[]{};
	}

	/**
	 * Get the token name of the rule that can be used to reference the rule from LogicService
	 *
	 * @return the token name
	 */
	@Override
	protected String getEvaluableToken() {
		return TOKEN;
	}
}
