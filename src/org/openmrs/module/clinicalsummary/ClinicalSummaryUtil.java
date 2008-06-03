/**
 * Auto generated file comment
 */
package org.openmrs.module.clinicalsummary;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import org.openmrs.Cohort;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;

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
     * Returns a File with name and path for a clinical summary queue item.
     * 
     * @param queueItem
     * @param dir ClinicalSummaryUtil.DIRECTORY Available values are GENERATED
     *        and TO_PRINT
     * @param date
     * @return
     */
    public static File getOutFile(ClinicalSummaryQueueItem queueItem,
            ClinicalSummaryUtil.DIRECTORY dir, Date date) {

        StringBuilder filename = new StringBuilder();

        Location location = Context.getEncounterService().getLocation(
                queueItem.getLocation().getLocationId());
        if (null == location.getName()) {
            filename.append("default_");
        } else {
            String loc = location.getName() + "_";
            loc = loc.replaceAll(" ", "-");
            filename.append(loc);
        }

        Patient patient = Context.getPatientService().getPatient(
                queueItem.getPatient().getPatientId());
        if (null == patient.getPatientIdentifier()) {
            String id = patient.getPatientId().toString() + "_";
            id = id.replaceAll(" ", "-");
            filename.append(id);
        } else {
            String identifier = patient.getPatientIdentifier().getIdentifier()
                    + "_";
            identifier = identifier.replaceAll(" ", "-");
            filename.append(identifier);
        }

        // TODO: Should this date be today's date or the encounter date? (since
        // the encounter date is used in the queueItem)
        Date today = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ssz");

        if (date != null)
            filename.append(df.format(date));
        else
            filename.append(df.format(today));
        filename.append(".pdf");

        return new File(ClinicalSummaryUtil.getOutDir(queueItem, dir)
                .toString(), filename.toString());
    }

    /**
     * Returns File of the location where clinical summary queue item will be
     * generated or printed. If
     * 
     * @param queueItem is null, then returns absolute path for global property
     *        of either ClinicalSummaryUtil.DIRECTORY.GENERATED or
     *        ClinicalSummaryUtil.DIRECTORY.TO_PRINT
     * 
     * @param queueItem
     * @param dir ClinicalSummaryUtil.DIRECTORY Either GENERATED or TO_PRINT
     * @return
     */
    public static File getOutDir(ClinicalSummaryQueueItem queueItem,
            ClinicalSummaryUtil.DIRECTORY dir) {
        if (dir == ClinicalSummaryUtil.DIRECTORY.GENERATED) {
            String path;
            if (null == queueItem)
                path = OpenmrsUtil.getDirectoryInApplicationDataDirectory(
                        Context.getAdministrationService().getGlobalProperty(
                                "clinicalsummary.queueItemGenerateDir"))
                        .getAbsolutePath();
            else
                path = OpenmrsUtil.getDirectoryInApplicationDataDirectory(
                        Context.getAdministrationService().getGlobalProperty(
                                "clinicalsummary.queueItemGenerateDir"))
                        .getAbsolutePath()
                        + File.separator
                        + ClinicalSummaryUtil.getLocationDir(queueItem);
            File file = new File(path);
            if (!file.exists())
                file.mkdirs();
            return file;
        } else {
            String path;
            if (null == queueItem)
                path = OpenmrsUtil
                        .getDirectoryInApplicationDataDirectory(
                                Context
                                        .getAdministrationService()
                                        .getGlobalProperty(
                                                "clinicalsummary.queueItemPrintDir")
                                        .toString()).getAbsolutePath();
            else
                path = OpenmrsUtil
                        .getDirectoryInApplicationDataDirectory(
                                Context
                                        .getAdministrationService()
                                        .getGlobalProperty(
                                                "clinicalsummary.queueItemPrintDir")
                                        .toString()).getAbsolutePath()
                        + File.separator
                        + ClinicalSummaryUtil.getLocationDir(queueItem);
            File file = new File(path);
            if (!file.exists())
                file.mkdirs();
            return file;
        }
    }

    /**
     * Returns a String of the Location name for the queueItem. Replaces any
     * spaces with underscore.
     * 
     * @param queueItem
     * @return
     */
    public static String getLocationDir(ClinicalSummaryQueueItem queueItem) {
        Location loc = Context.getEncounterService().getLocation(
                queueItem.getLocation().getLocationId());
        String locationDirectory = loc.getName().replaceAll(" ", "_");
        if (null == locationDirectory
                || locationDirectory.toLowerCase().equals("null"))
            return "default";
        else
            return locationDirectory;
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

    /**
     * The idea for this was to check the file system for clinical summaries and
     * add them to the database if they are not already there. This would happen
     * each time the module is loaded.
     * 
     * TODO: Finish implementing this, or better yet, delete it.
     * 
     * public static ClinicalSummaryQueueItem createQueueItemFromFile(File file) {
     * if (null == file.getName() ||
     * !file.getName().toLowerCase().endsWith(".pdf")) return null; String[]
     * path = file.getParent().split("\\|/"); String[] fileName =
     * file.getName().toLowerCase().replaceAll(".pdf", "").split("_", 2);
     * DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ssz"); String
     * location = path[path.length-1].replaceAll("_", " "); String identifier =
     * fileName[0];
     * 
     * ClinicalSummaryQueueItem queueItem = new ClinicalSummaryQueueItem(); List<PatientIdentifier>
     * identifiers = new ArrayList<PatientIdentifier>(); for
     * (org.openmrs.PatientIdentifierType t :
     * Context.getPatientService().getPatientIdentifierTypes()) {
     * identifiers.addAll(Context.getPatientService().getPatientIdentifiers(identifier,
     * t)); } //if (identifiers.size() != 1) // return null; // TODO: check that
     * identifiers contains exactly one unique identifier Patient p =
     * identifiers.get(0).getPatient(); Location l =
     * Context.getEncounterService().getLocationByName(location); if (null == p ||
     * null == l) { return null; } queueItem.setPatient(p);
     * queueItem.setLocation(l); try { Date day = df.parse(fileName[1]);
     * queueItem.setEncounterDatetime(day); queueItem.setDateCreated(day); }
     * catch (Exception e) { Date day = new Date();
     * queueItem.setEncounterDatetime(day); queueItem.setDateCreated(day); }
     * return queueItem; }
     */

}
