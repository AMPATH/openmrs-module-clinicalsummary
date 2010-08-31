/**
 * Auto generated file comment
 */
package org.openmrs.module.clinicalsummary.deprecated;

import java.util.StringTokenizer;

import org.openmrs.Cohort;

/**
 * Utility Class for Clinical Summary Module
 */
public class ClinicalSummaryUtil {

    public static enum DIRECTORY {
        GENERATED, TO_PRINT
    }

    public static enum ORDER {
        IDENTIFIER, ENCOUNTER_DATE
    }

    /**
     * Returns a Cohort from a String of comma separated patientId's This method
     * replaces the same method in org.openmrs.reporting.PatientSet, which does
     * not exist after OpenMRS 1.2
     * 
     * @param s
     * @return
     */
    public static Cohort parseCommaSeparatedPatientIds(String s) {
        Cohort ret = new Cohort();
        for (StringTokenizer st = new StringTokenizer(s, ","); st
                .hasMoreTokens();) {
            String id = st.nextToken();
            ret.addMember(new Integer(id.trim()));
        }
        return ret;
    }
}
