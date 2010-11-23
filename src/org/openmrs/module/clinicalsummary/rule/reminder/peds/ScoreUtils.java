package org.openmrs.module.clinicalsummary.rule.reminder.peds;

public class ScoreUtils {
	
	private ScoreUtils() {
	}
	
	/**
	 * @param l the L from the WHO table
	 * @param m the M from the WHO table
	 * @param s the S from the WHO table
	 * @param y the measurement value
	 * @return
	 */
	public static double zScore(double l, double m, double s, double y) {
		double zIndex = (Math.pow((y / m), l) - 1) / (s * l);
		if (zIndex > 3) {
			return (3 + ((y - standardDeviation(l, m, s, 3)) / (standardDeviation(l, m, s, 3) - standardDeviation(l, m, s, 2))));
		} else if (zIndex < -3) {
			return (-3 + ((y - standardDeviation(l, m, s, -3)) / (standardDeviation(l, m, s, -2) - standardDeviation(l, m, s, -3))));
		} else
			return zIndex;
	}
	
	/**
	 * @param l the L from the WHO table
	 * @param m the M from the WHO table
	 * @param s the S from the WHO table
	 * @param z the z score
	 * @return
	 */
	public static double percentile(double l, double m, double s, double z) {
		return m * Math.pow((1 + (l * s * z)), 1 / l);
	}
	
	/**
	 * @param l value of L from the WHO table
	 * @param m value of M from the WHO table
	 * @param s value of S from the WHO table
	 * @param cutOff cut-off SD (possible value are -3, 3, 2, -2)
	 * @return
	 */
	public static double standardDeviation(double l, double m, double s, double cutOff) {
		return m * Math.pow((1 + (l * s * cutOff)), 1 / l);
	}
	
}
