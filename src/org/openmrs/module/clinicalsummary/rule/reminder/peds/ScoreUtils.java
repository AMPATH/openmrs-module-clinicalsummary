package org.openmrs.module.clinicalsummary.rule.reminder.peds;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.SummaryService;
import org.openmrs.module.clinicalsummary.WeightAgeStandard;

public class ScoreUtils {
	
	private static Log log = LogFactory.getLog(ScoreUtils.class);
	
	private static final String MALE_LONG_STRING = "Male";
	
	private static final String MALE_SHORT_STRING = "M";
	
	private static final String FEMALE_LONG_STRING = "Female";
	
	private static final String FEMALE_SHORT_STRING = "F";
	
	// this is the approximation table of z-score to percentile
	// the method will search the closest double value and return the position of that double value that will correspond to the z-score percentile
	private static final Double[] zScores = { -2.326, -2.054, -1.881, -1.751, -1.645, -1.555, -1.476, -1.405, -1.341,
	        -1.282, -1.227, -1.175, -1.126, -1.08, -1.036, -0.994, -0.954, -0.915, -0.878, -0.842, -0.806, -0.772, -0.739,
	        -0.706, -0.674, -0.643, -0.613, -0.583, -0.553, -0.524, -0.496, -0.468, -0.44, -0.412, -0.385, -0.358, -0.332,
	        -0.305, -0.279, -0.253, -0.228, -0.202, -0.176, -0.151, -0.126, -0.1, -0.075, -0.05, -0.025, 0.0, 0.025, 0.05,
	        0.075, 0.1, 0.126, 0.151, 0.176, 0.202, 0.228, 0.253, 0.279, 0.305, 0.332, 0.358, 0.385, 0.412, 0.44, 0.468,
	        0.496, 0.524, 0.553, 0.583, 0.613, 0.643, 0.674, 0.706, 0.739, 0.772, 0.806, 0.842, 0.878, 0.915, 0.954, 0.994,
	        1.036, 1.08, 1.126, 1.175, 1.227, 1.282, 1.341, 1.405, 1.476, 1.555, 1.645, 1.751, 1.881, 2.054, 2.326 };
	
	private ScoreUtils() {
	}
	
	/**
	 * @param zScore
	 * @return
	 */
	public static int searchZScore(Double zScore) {
		// TODO: hack for extreme cases
		if (zScore > zScores[zScores.length - 1])
			return zScores.length;
		if (zScore < zScores[0])
			return 0;
		
		return search(zScore, 0, zScores.length - 1);
	}
	
	private static int search(double zScore, int startIndex, int endIndex) {
		int middleIndex = (endIndex - startIndex) / 2;
		int searchIndex = startIndex + middleIndex;
		
		if (zScores[searchIndex] > zScore) {
			if (between(zScore, searchIndex, searchIndex - 1))
				return closestZScore(zScore, searchIndex, searchIndex - 1);
			endIndex = searchIndex;
			return search(zScore, startIndex, endIndex);
		} else if (zScores[searchIndex] < zScore) {
			if (between(zScore, searchIndex, searchIndex + 1))
				return closestZScore(zScore, searchIndex, searchIndex + 1);
			startIndex = searchIndex;
			return search(zScore, startIndex, endIndex);
		} else {
			return searchIndex + 1;
		}
	}
	
	/**
	 * Check if zScore is between is between zScores[firstIndex] and zScores[secondIndex].
	 * 
	 * @param zScore
	 * @param firstIndex
	 * @param secondIndex
	 * @return
	 */
	private static boolean between(double zScore, int firstIndex, int secondIndex) {
		return ((zScore > zScores[firstIndex] && zScore <= zScores[secondIndex]) || (zScore < zScores[firstIndex] && zScore >= zScores[secondIndex]));
	}
	
	/**
	 * This method will calculate if the zScore is closest to zScores[firstIndex] or
	 * zScores[secondIndex]. The requirement is the zScore is between zScores[firstIndex] and
	 * zScores[secondIndex].
	 * 
	 * @param zScore
	 * @param firstIndex
	 * @param secondIndex
	 * @return
	 */
	private static int closestZScore(double zScore, int firstIndex, int secondIndex) {
		double diffWithFirstIndex = Math.abs(zScore - zScores[firstIndex]);
		double diffWithSecondIndex = Math.abs(zScore - zScores[secondIndex]);
		if (zScore > 0)
			return (diffWithFirstIndex > diffWithSecondIndex ? firstIndex + 1 : secondIndex + 1);
		else
			return (diffWithFirstIndex > diffWithSecondIndex) ? secondIndex + 1 : firstIndex + 1;
	}
	
	/**
	 * @param l the L from the WHO table
	 * @param m the M from the WHO table
	 * @param s the S from the WHO table
	 * @param y the measurement value
	 * @return
	 */
	private static double zScore(double l, double m, double s, double y) {
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
	 * @param l value of L from the WHO table
	 * @param m value of M from the WHO table
	 * @param s value of S from the WHO table
	 * @param cutOff cut-off SD (possible value are -3, 3, 2, -2)
	 * @return
	 */
	private static double standardDeviation(double l, double m, double s, double cutOff) {
		return m * Math.pow((1 + (l * s * cutOff)), 1 / l);
	}
	
	public static Double calculateZScore(Patient patient, Date asOfDate, Double weight) {
		SummaryService service = Context.getService(SummaryService.class);
		
		Double zScore = null;
		Date birthDate = patient.getBirthdate();
		// apparently there are records with null birthdate
		if (birthDate != null) {
			
			Calendar birthCalendar = Calendar.getInstance();
			
			birthCalendar.setTime(birthDate);
			birthCalendar.add(Calendar.YEAR, 5);
			Date fiveYars = birthCalendar.getTime();
			
			birthCalendar.setTime(birthDate);
			birthCalendar.add(Calendar.WEEK_OF_YEAR, 13);
			
			Calendar todayCalendar = Calendar.getInstance();
			todayCalendar.setTime(asOfDate);
			
			// decide the gender.
			// our database contains both F or Female and M or Male
			String gender = StringUtils.EMPTY;
			if (StringUtils.equalsIgnoreCase(MALE_LONG_STRING, patient.getGender())
			        || StringUtils.equalsIgnoreCase(MALE_SHORT_STRING, patient.getGender()))
				gender = MALE_LONG_STRING;
			else if (StringUtils.equalsIgnoreCase(FEMALE_LONG_STRING, patient.getGender())
			        || StringUtils.equalsIgnoreCase(FEMALE_SHORT_STRING, patient.getGender()))
				gender = FEMALE_LONG_STRING;
			
			if (log.isDebugEnabled())
				log.debug("Patient: " + patient.getPatientId() + ", gender: " + gender);
			
			// only do processing for kids younger than 5 years old
			if (fiveYars.after(todayCalendar.getTime()))
				
				// today is after week 13, then we need to calculate the age in month
				if (todayCalendar.after(birthCalendar)) {
					birthCalendar.setTime(birthDate);
					
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
						// decrease the month, this way we get the previous month
						birthCalendar.add(Calendar.MONTH, -1);
						ageInDay = birthCalendar.getActualMaximum(Calendar.DATE) - birthDay + todayDay;
						
						if (ageInDay > birthCalendar.getActualMaximum(Calendar.DATE) / 2)
							ageInMonth++;
					}
					
					ageInMonth = ageInMonth + (ageInYear * 12);
					
					if (log.isDebugEnabled())
						log.debug("Patient: " + patient.getPatientId() + ", age in month: " + ageInMonth);
					
					WeightAgeStandard standard = service.getWeightAgeStandard(ageInMonth, "Month", gender);
					if (standard != null)
						zScore = ScoreUtils.zScore(standard.getlValue(), standard.getmValue(), standard.getsValue(), weight);
				} else {
					birthCalendar.setTime(birthDate);
					
					long diff = todayCalendar.getTimeInMillis() - birthCalendar.getTimeInMillis();
					long week = 1000 * 60 * 60 * 24 * 7;
					long ageInWeek = diff / week;
					// TODO: if the mod if more than half of the week, then round it up
					// or should we use double casting here and do Math.round()
					if (diff % week > week / 2)
						ageInWeek++;
					
					if (log.isDebugEnabled())
						log.debug("Patient: " + patient.getPatientId() + ", age in week: " + ageInWeek);
					
					WeightAgeStandard standard = service.getWeightAgeStandard((int) ageInWeek, "Week", gender);
					if (standard != null)
						zScore = ScoreUtils.zScore(standard.getlValue(), standard.getmValue(), standard.getsValue(), weight);
				}
			
			if (log.isDebugEnabled())
				log.debug("Calculated zscore for patient: " + patient.getPatientId() + ", zscore: " + zScore);
		}
		
		return zScore;
	}
	
	/**
	 * @param l the L from the WHO table
	 * @param m the M from the WHO table
	 * @param s the S from the WHO table
	 * @param z the z score
	 * @return
	 */
	private static double percentile(double l, double m, double s, double z) {
		return m * Math.pow((1 + (l * s * z)), 1 / l);
	}
	
	public static Double calculatePercentile(Patient patient, Date asOfDate, Double weight) {
		SummaryService service = Context.getService(SummaryService.class);
		
		Double zScore = null;
		Double percentile = null;
		
		Date birthDate = patient.getBirthdate();
		// apparently there are records with null birthdate
		if (birthDate != null) {
			
			Calendar birthCalendar = Calendar.getInstance();
			
			birthCalendar.setTime(birthDate);
			birthCalendar.add(Calendar.YEAR, 5);
			Date fiveYars = birthCalendar.getTime();
			
			birthCalendar.setTime(birthDate);
			birthCalendar.add(Calendar.WEEK_OF_YEAR, 13);
			
			Calendar todayCalendar = Calendar.getInstance();
			todayCalendar.setTime(asOfDate);
			
			// today is after week 13, then we need to calculate the age in month
			
			// decide the gender.
			// our database contains both F or Female and M or Male
			String gender = StringUtils.EMPTY;
			if (StringUtils.equalsIgnoreCase(MALE_LONG_STRING, patient.getGender())
			        || StringUtils.equalsIgnoreCase(MALE_SHORT_STRING, patient.getGender()))
				gender = MALE_LONG_STRING;
			else if (StringUtils.equalsIgnoreCase(FEMALE_LONG_STRING, patient.getGender())
			        || StringUtils.equalsIgnoreCase(FEMALE_SHORT_STRING, patient.getGender()))
				gender = FEMALE_SHORT_STRING;
			
			// only do processing for kids younger than 5 years old
			if (fiveYars.after(todayCalendar.getTime()))
				
				if (todayCalendar.after(birthCalendar)) {
					birthCalendar.setTime(birthDate);
					
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
						// decrease the month, this way we get the previous month
						birthCalendar.add(Calendar.MONTH, -1);
						ageInDay = birthCalendar.getActualMaximum(Calendar.DATE) - birthDay + todayDay;
						
						if (ageInDay > birthCalendar.getActualMaximum(Calendar.DATE) / 2)
							ageInMonth++;
					}
					
					ageInMonth = ageInMonth + (ageInYear * 12);
					
					if (log.isDebugEnabled())
						log.debug("Patient: " + patient.getPatientId() + ", age in month: " + ageInMonth);
					
					WeightAgeStandard standard = service.getWeightAgeStandard(ageInMonth, "Month", gender);
					if (standard != null) {
						zScore = ScoreUtils.zScore(standard.getlValue(), standard.getmValue(), standard.getsValue(), weight);
						percentile = ScoreUtils.percentile(standard.getlValue(), standard.getmValue(), standard.getsValue(),
						    zScore);
					}
					
				} else {
					birthCalendar.setTime(birthDate);
					
					long diff = todayCalendar.getTimeInMillis() - birthCalendar.getTimeInMillis();
					long week = 1000 * 60 * 60 * 24 * 7;
					int ageInWeek = (int) (diff / week);
					// TODO: if the mod if more than half of the week, then round it up
					// or should we use double casting here and do Math.round()
					if (diff % week > week / 2)
						ageInWeek++;
					
					if (log.isDebugEnabled())
						log.debug("Patient: " + patient.getPatientId() + ", age in week: " + ageInWeek);
					
					WeightAgeStandard standard = service.getWeightAgeStandard(ageInWeek, "Week", gender);
					if (standard != null) {
						zScore = ScoreUtils.zScore(standard.getlValue(), standard.getmValue(), standard.getsValue(), weight);
						percentile = ScoreUtils.percentile(standard.getlValue(), standard.getmValue(), standard.getsValue(),
						    zScore);
					}
				}
			
			if (log.isDebugEnabled())
				log.debug("Calculated percentile for patient: " + patient.getPatientId() + ", percentile: " + percentile);
		}
		
		return percentile;
	}
}
