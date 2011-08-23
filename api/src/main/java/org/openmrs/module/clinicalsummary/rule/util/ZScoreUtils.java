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

package org.openmrs.module.clinicalsummary.rule.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.enumeration.AgeUnit;
import org.openmrs.module.clinicalsummary.enumeration.Gender;
import org.openmrs.module.clinicalsummary.service.UtilService;
import org.openmrs.module.clinicalsummary.util.weight.WeightStandard;

/**
 */
public class ZScoreUtils {

	private static final Log log = LogFactory.getLog(ZScoreUtils.class);

	public static final Integer ONE_WEEK = 60 * 60 * 24 * 7;

	// this is the approximation table of z-score to percentile
	// the method will search the closest double value and return the position of that double value that will correspond to the z-score percentile
	private static final Double[] zScorePercentiles = {-2.326, -2.054, -1.881, -1.751, -1.645, -1.555, -1.476, -1.405, -1.341,
			-1.282, -1.227, -1.175, -1.126, -1.08, -1.036, -0.994, -0.954, -0.915, -0.878, -0.842, -0.806, -0.772, -0.739,
			-0.706, -0.674, -0.643, -0.613, -0.583, -0.553, -0.524, -0.496, -0.468, -0.44, -0.412, -0.385, -0.358, -0.332,
			-0.305, -0.279, -0.253, -0.228, -0.202, -0.176, -0.151, -0.126, -0.1, -0.075, -0.05, -0.025, 0.0, 0.025, 0.05,
			0.075, 0.1, 0.126, 0.151, 0.176, 0.202, 0.228, 0.253, 0.279, 0.305, 0.332, 0.358, 0.385, 0.412, 0.44, 0.468,
			0.496, 0.524, 0.553, 0.583, 0.613, 0.643, 0.674, 0.706, 0.739, 0.772, 0.806, 0.842, 0.878, 0.915, 0.954, 0.994,
			1.036, 1.08, 1.126, 1.175, 1.227, 1.282, 1.341, 1.405, 1.476, 1.555, 1.645, 1.751, 1.881, 2.054, 2.326};

	private static final String MALE_LONG_STRING = "Male";

	private static final String MALE_SHORT_STRING = "M";

	private static final String FEMALE_LONG_STRING = "Female";

	private static final String FEMALE_SHORT_STRING = "F";

	/**
	 * Search for the closest percentile value based on the z-score value.
	 *
	 * Check the for the edge cases. If greater than the last value in the array, then return the last. If smaller than the first value in the array,
	 * then return the first. Otherwise search for the closest value in the array using b-tree search.
	 *
	 * @param zScore the z-score value
	 * @return closest percentile based on the z-score value
	 */
	public static int searchZScore(final Double zScore) {
		if (zScore > zScorePercentiles[zScorePercentiles.length - 1])
			return zScorePercentiles.length;
		if (zScore < zScorePercentiles[0])
			return 0;

		return search(zScore, 0, zScorePercentiles.length - 1);
	}

	/**
	 * Search for a z-score value in the z-score to percentile table. The returned percentile value is the closest percentile value for the z-score.
	 *
	 * @param zScore
	 * @param startIndex
	 * @param endIndex
	 * @return
	 */
	private static int search(final double zScore, final int startIndex, final int endIndex) {
		int middleIndex = (endIndex - startIndex) / 2;
		int searchIndex = startIndex + middleIndex;

		if (zScorePercentiles[searchIndex] > zScore) {
			// conditional checking to make sure we can find a location for the z-score
			if (between(zScore, searchIndex, searchIndex - 1))
				return closestZScore(zScore, searchIndex, searchIndex - 1);
			return search(zScore, startIndex, searchIndex);
		} else if (zScorePercentiles[searchIndex] < zScore) {
			// conditional checking to make sure we can find a location for the z-score
			if (between(zScore, searchIndex, searchIndex + 1))
				return closestZScore(zScore, searchIndex, searchIndex + 1);
			return search(zScore, searchIndex, endIndex);
		} else {
			return searchIndex + 1;
		}
	}

	/**
	 * Check if zScore is between is between zScorePercentiles[firstIndex] and zScorePercentiles[secondIndex].
	 *
	 * @param zScore
	 * @param firstIndex
	 * @param secondIndex
	 * @return
	 */
	private static boolean between(final double zScore, final int firstIndex, final int secondIndex) {
		return ((zScore > zScorePercentiles[firstIndex] && zScore <= zScorePercentiles[secondIndex]) || (zScore < zScorePercentiles[firstIndex] && zScore >= zScorePercentiles[secondIndex]));
	}

	/**
	 * This method will calculate if the zScore is closest to zScorePercentiles[firstIndex] or zScorePercentiles[secondIndex]. The requirement is the
	 * zScore is between zScorePercentiles[firstIndex] and zScorePercentiles[secondIndex].
	 *
	 * @param zScore      the z-score value
	 * @param firstIndex  the first index in the z-score to percentile table
	 * @param secondIndex the last index in the z-score to percentile table
	 * @return
	 */
	private static int closestZScore(final double zScore, final int firstIndex, final int secondIndex) {
		double diffWithFirstIndex = Math.abs(zScore - zScorePercentiles[firstIndex]);
		double diffWithSecondIndex = Math.abs(zScore - zScorePercentiles[secondIndex]);
		if (zScore > 0)
			return (diffWithFirstIndex > diffWithSecondIndex ? firstIndex + 1 : secondIndex + 1);
		else
			return (diffWithFirstIndex > diffWithSecondIndex) ? secondIndex + 1 : firstIndex + 1;
	}

	/**
	 * Calculate the z-score for a certain weight value based on the WHO table
	 *
	 * @param l the L from the WHO table
	 * @param m the M from the WHO table
	 * @param s the S from the WHO table
	 * @param y the measurement value
	 * @return
	 */
	private static double zScore(final double l, final double m, final double s, final double y) {
		double zIndex = (Math.pow((y / m), l) - 1) / (s * l);
		if (zIndex > 3) {
			return (3 + ((y - standardDeviation(l, m, s, 3)) / (standardDeviation(l, m, s, 3) - standardDeviation(l, m, s, 2))));
		} else if (zIndex < -3) {
			return (-3 + ((y - standardDeviation(l, m, s, -3)) / (standardDeviation(l, m, s, -2) - standardDeviation(l, m,
					s, -3))));
		} else
			return zIndex;
	}

	/**
	 * Calculate standard deviation value for a weight based on the WHO Weight-For-Age standard
	 *
	 * @param l      value of L from the WHO table
	 * @param m      value of M from the WHO table
	 * @param s      value of S from the WHO table
	 * @param cutOff cut-off SD (possible value are -3, 3, 2, -2)
	 * @return
	 */
	private static double standardDeviation(final double l, final double m, final double s, final double cutOff) {
		return m * Math.pow((1 + (l * s * cutOff)), 1 / l);
	}

	/**
	 * Calculate age based on the birth date and the reference date. The returned age is in month. The remaining days will be rounded to the closest one
	 * month value.
	 *
	 * @param birthDate the birth date
	 * @param asOfDate  the reference date to calculate the age
	 * @return the age in month
	 */
	private static int calculateAgeInMonth(final Date birthDate, final Date asOfDate) {

		Calendar birthCalendar = Calendar.getInstance();
		birthCalendar.setTime(birthDate);

		Calendar todayCalendar = Calendar.getInstance();
		todayCalendar.setTime(asOfDate);

		int birthYear = birthCalendar.get(Calendar.YEAR);
		int todayYear = todayCalendar.get(Calendar.YEAR);

		int ageInYear = todayYear - birthYear;

		int birthMonth = birthCalendar.get(Calendar.MONTH);
		int todayMonth = todayCalendar.get(Calendar.MONTH);

		int ageInMonth = todayMonth - birthMonth;
		if (ageInMonth < 0) {
			ageInYear--;
			ageInMonth = 12 - birthMonth + todayMonth;
		}

		int birthDay = birthCalendar.get(Calendar.DATE);
		int todayDay = todayCalendar.get(Calendar.DATE);

		int ageInDay = todayDay - birthDay;
		if (ageInDay < 0) {
			ageInMonth--;

			// we need to calculate the age in day using previous month
			Calendar calendar = GregorianCalendar.getInstance();
			calendar.set(Calendar.MONTH, todayMonth - 1);

			birthCalendar.add(Calendar.MONTH, -1);
			ageInDay = calendar.getActualMaximum(Calendar.DATE) - birthDay + todayDay;
		}

		if (ageInDay > todayCalendar.getActualMaximum(Calendar.DATE) / 2)
			ageInMonth++;

		ageInMonth = ageInMonth + (ageInYear * 12);
		return ageInMonth;
	}

	/**
	 * Calculate age based on the birth date and the reference date. The returned age is in week. The remaining time will be rounded to the closest one
	 * week value.
	 *
	 * @param birthDate the birth date
	 * @param asOfDate  the reference date to do the age calculation
	 * @return age in week
	 */
	private static int calculateAgeInWeek(final Date birthDate, final Date asOfDate) {

		Calendar birthCalendar = Calendar.getInstance();
		birthCalendar.setTime(birthDate);

		Calendar todayCalendar = Calendar.getInstance();
		todayCalendar.setTime(asOfDate);

		long timeDifference = (todayCalendar.getTimeInMillis() - birthCalendar.getTimeInMillis()) / 1000;

		int ageInWeek = (int) (timeDifference / ONE_WEEK);
		if (timeDifference % ONE_WEEK > ONE_WEEK / 2)
			ageInWeek++;

		return ageInWeek;
	}

	/**
	 * Calculate the z-score value based on the stored values of the WHO standard
	 *
	 * @param patient  the patient
	 * @param asOfDate the reference point for the z-score calculation
	 * @param weight   the weight of the patient
	 * @return the calculated z-score
	 */
	public static Double calculateZScore(final Patient patient, final Date asOfDate, final Double weight) {

		Double zScore = null;
		Date birthDate = patient.getBirthdate();

		// apparently there are records with null birth date
		if (birthDate != null) {

			Calendar birthCalendar = Calendar.getInstance();

			birthCalendar.setTime(birthDate);
			birthCalendar.add(Calendar.YEAR, 5);
			Date fiveYars = birthCalendar.getTime();

			Calendar todayCalendar = Calendar.getInstance();
			todayCalendar.setTime(asOfDate);

			// only do processing for kids younger than 5 years old
			if (fiveYars.after(todayCalendar.getTime())) {

				// decide the gender.
				// our database contains both F or Female and M or Male
				Gender gender = null;
				if (StringUtils.equalsIgnoreCase(MALE_LONG_STRING, patient.getGender())
						|| StringUtils.equalsIgnoreCase(MALE_SHORT_STRING, patient.getGender()))
					gender = Gender.GENDER_MALE;
				else if (StringUtils.equalsIgnoreCase(FEMALE_LONG_STRING, patient.getGender())
						|| StringUtils.equalsIgnoreCase(FEMALE_SHORT_STRING, patient.getGender()))
					gender = Gender.GENDER_FEMALE;

				birthCalendar.setTime(birthDate);
				birthCalendar.add(Calendar.WEEK_OF_YEAR, 13);

				WeightStandard standard;

				UtilService utilService = Context.getService(UtilService.class);
				if (todayCalendar.after(birthCalendar)) {
					Integer ageInMonth = calculateAgeInMonth(patient.getBirthdate(), asOfDate);
					standard = utilService.getWeightStandard(gender, AgeUnit.UNIT_MONTH, ageInMonth);
				} else {
					Integer ageInWeek = calculateAgeInWeek(patient.getBirthdate(), asOfDate);
					standard = utilService.getWeightStandard(gender, AgeUnit.UNIT_WEEK, ageInWeek);
				}

				if (standard != null)
					zScore = zScore(standard.getCurve(), standard.getMean(), standard.getCoef(), weight);
			}

			if (log.isDebugEnabled())
				log.debug("Patient: " + patient.getPatientId() + ", zscore: " + zScore);
		}

		return zScore;
	}
}
