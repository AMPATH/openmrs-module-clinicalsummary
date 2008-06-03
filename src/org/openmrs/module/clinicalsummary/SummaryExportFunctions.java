package org.openmrs.module.clinicalsummary;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.reporting.export.DataExportFunctions;

public class SummaryExportFunctions extends DataExportFunctions {

    public final Log log = LogFactory.getLog(this.getClass());

    protected Map<Integer, List<List<Object>>> patientIdObsValueMapLeft = null;

    protected Map<Integer, List<List<Object>>> patientIdObsValueMapRight = null;

    public SummaryExportFunctions() {
        super();

        this.validEncounterTypes.add(new EncounterType(1));
        this.validEncounterTypes.add(new EncounterType(2));
        this.validEncounterTypes.add(new EncounterType(3));
        this.validEncounterTypes.add(new EncounterType(4));
        this.validEncounterTypes.add(new EncounterType(14));
        this.validEncounterTypes.add(new EncounterType(15));
    }

    /**
     * Returns a list of obs rows
     * 
     * [{concept value}, {attr 1}, {attr 2}, etc]
     * 
     * @param conceptNameLeft
     * @param conceptNameRight
     * @param attrObj
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public List<List<Object>> getIntersectedObs(String conceptNameLeft,
            String conceptNameRight, Object attrListObj) throws Exception {

        List<String> attrs = (List<String>) attrListObj;

        Concept conceptLeft = getConcept(conceptNameLeft);
        Concept conceptRight = getConcept(conceptNameRight);

        if (patientIdObsValueMapLeft == null) {
            patientIdObsValueMapLeft = patientSetService.getObservationsValues(
                    getPatientSet(), conceptLeft, attrs);
            patientIdObsValueMapRight = patientSetService
                    .getObservationsValues(getPatientSet(), conceptRight, attrs);
        }

        List<List<Object>> answersLeft = patientIdObsValueMapLeft
                .get(getPatientId());
        List<List<Object>> answersRight = patientIdObsValueMapRight
                .get(getPatientId());

        if (answersLeft == null)
            answersLeft = new Vector<List<Object>>();

        if (answersRight == null)
            answersRight = new Vector<List<Object>>();

        Map<Object, List<Object>> answerHash = new Hashtable<Object, List<Object>>();

        // Create hash with an entry for the most recent of each left-sided
        // observation
        for (int i = answersLeft.size() - 1; i >= 0; i--) {
            List<Object> leftRow = answersLeft.get(i);
            Object answer = leftRow.get(0);
            if (answer != null)
                if (!answerHash.containsKey(answer))
                    answerHash.put(answer, leftRow);
        }

        // Remove any observations from the hash where there is a later entry in
        // the right-side
        // list of observations
        for (int i = answersRight.size() - 1; i >= 0; i--) {
            List<Object> rightRow = answersRight.get(i);
            Date rightDate = (Date) rightRow.get(1);
            Object answer = rightRow.get(0);
            if (answer != null)
                if (answerHash.containsKey(answer)) {
                    List<Object> leftRow = answerHash.get(answer);
                    Date leftDate = (Date) leftRow.get(1);
                    if (leftDate.before(rightDate))
                        answerHash.remove(answer);
                }
        }

        // answersLeft and answersRight are always sorted from [most recent] ->
        // [oldest]
        // if we loop over answersRight from the top down, and inner loop over
        // answersLeft top down,
        // we can compare/remove obs from left
        /*
         * for (List<Object> row : answersRight) { Object rightValue =
         * row.get(0); Date rightDate = (Date)row.get(1); int x = 0; while (x <
         * answersLeft.size()) { List<Object> leftRow = answersLeft.get(x++);
         * if (rightValue.equals(leftRow.get(0))) { Date leftDate = (Date)
         * leftRow.get(1); if (leftDate.before(rightDate))
         * answersLeft.remove(--x); } } }
         * 
         * return answersLeft;
         */

        List<List<Object>> sortedAnswers = new Vector<List<Object>>();
        if (answerHash.size() > 0)
            for (List<Object> value : answerHash.values())
                sortedAnswers.add(value);
        Collections.sort(sortedAnswers, new DateComparator());

        return sortedAnswers;
    }

    /**
     * Returns a list of [number, (years|months|days)]
     * 
     * @param d
     * @return
     */
    public List<Object> getAge(Date birthdate) {
        List<Object> returnList = new Vector<Object>();

        if (birthdate != null) {
            Calendar today = Calendar.getInstance();

            Calendar bday = new GregorianCalendar();
            bday.setTime(birthdate);

            int years = today.get(Calendar.YEAR) - bday.get(Calendar.YEAR);

            // tricky bit:
            // set birthday calendar to this year
            // if the current date is less that the new 'birthday', subtract a
            // year
            bday.set(Calendar.YEAR, today.get(Calendar.YEAR));
            if (today.before(bday)) {
                years = years - 1;
            }

            if (years > 1) {
                returnList.add(years);
                returnList.add("years");
                return returnList;
            } else {
                // calculate months
                int months = today.get(Calendar.MONTH)
                        - bday.get(Calendar.MONTH);
                if (months < 0) // if this overlaps the new year
                    months = 12 + months + (years > 0 ? 12 : 0);

                if (months > 1) {
                    returnList.add(months);
                    returnList.add("months");
                    return returnList;
                }

                // calculate weeks
                int days = today.get(Calendar.DAY_OF_YEAR)
                        - bday.get(Calendar.DAY_OF_YEAR);
                if (days < 0) // if this overlaps the new year
                    days = 365 - bday.get(Calendar.DAY_OF_YEAR)
                            + today.get(Calendar.DAY_OF_YEAR);

                if (days > 7) {
                    returnList.add((int) days / 7);
                    returnList.add("weeks");
                    return returnList;
                } else {
                    returnList.add(days);
                    returnList.add("days");
                    return returnList;
                }

            }
        }

        returnList.add("");
        returnList.add("");
        return returnList;
    }

    /**
     * Finds all <code>conceptNames</code>s within the last
     * <code>withinNumberOfMonths</code> months
     * 
     * @param conceptName
     * @param withinNumberOfMonths
     * @return
     * @throws Exception
     */
    public List<Object> getObsTimeframe(String conceptName,
            Integer withinNumberOfMonths) throws Exception {
        Concept c = getConcept(conceptName);
        List<String> arr = new Vector<String>();

        arr.add("obsDatetime");
        List<List<Object>> rows = getLastNObsWithValues(-1, c, arr);

        Calendar cutoffDate = Calendar.getInstance();
        cutoffDate.add(Calendar.MONTH, -1 * withinNumberOfMonths);

        List<Object> obsValues = new Vector<Object>();

        Calendar currentDate = Calendar.getInstance();

        for (List<Object> vals : rows) {
            currentDate.setTime((Date) vals.get(1));
            if (cutoffDate.compareTo(currentDate) < 0) {
                obsValues.add(vals.get(0));
            }
        }

        return obsValues;
    }

    /**
     * Simple temporary hack method to get Clinical Summary reminder for CD4
     * count. The rules are: 1. If there has never been a CD4 count for this
     * patient, then remind to have one taken now. 2. If there has been more
     * than one CD4 count taken, do nothing for now. This is not yet
     * implemented. 3. If there has been exactly one CD4 count, and it was more
     * than six months ago, remind to check CD4 count now. If it was less than
     * six months ago, then remind to check CD4 count on the specified month and
     * year. 4. If there have been more than one CD4 count and all CD4 counts
     * occurred within the same 24-hour period, then treat it as if there were
     * exactly one CD4 count.
     * 
     * @return String reminder
     * @throws Exception
     */
    public String getCD4CountReminder() throws Exception {
        // days in milliseconds;
        long ONE_DAY = 86400000;
        long THREE_DAYS = ONE_DAY * 3;
        // duplicate cd4 count flag.
        boolean duplicateCD4Obs = false;
        // output format for date
        DateFormat reminderFormat = new SimpleDateFormat("MMMMM yyyy");
        DateFormat obsFormat = new SimpleDateFormat("dd-MMM-yyyy");
        // tomorrow
        GregorianCalendar tomorrow = new GregorianCalendar();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        // five months ago
        GregorianCalendar fiveMosAgo = new GregorianCalendar();
        fiveMosAgo.add(Calendar.MONTH, -5);
        // the time of this obs
        GregorianCalendar obsDatetime = new GregorianCalendar();
        Concept cd4 = getConcept("5497"); // name='CD4, BY FACS'
        Set<Obs> obs = Context.getObsService().getObservations(getPatient(),
                cd4, false);
        /* 
         * No CD4 count.
         */
        if (obs.isEmpty()) {
            return "Please order CD4 count now (no prior CD4 count on record).";
        }
        /*
         * More than one CD4 count
         */
        else if (obs.size() > 1) {
            GregorianCalendar firstCD4 = new GregorianCalendar();
            GregorianCalendar lastCD4 = new GregorianCalendar();
            GregorianCalendar thisCD4 = new GregorianCalendar();
            int cnt = 0;
            for (Obs o : obs) {
                if (++cnt == 1) {
                    firstCD4.setTime(o.getObsDatetime());
                    lastCD4.setTime(o.getObsDatetime());
                }
                thisCD4.setTime(o.getObsDatetime());
                firstCD4 = thisCD4.before(firstCD4) ? thisCD4 : firstCD4;
                lastCD4 = thisCD4.after(lastCD4) ? thisCD4 : lastCD4;
            }
            if (Math
                    .abs(lastCD4.getTimeInMillis() - firstCD4.getTimeInMillis()) < THREE_DAYS) {
                // All CD4's taken within this time frame can be considered just one CD4 count taken. 
                duplicateCD4Obs = true;
            } else {
                return " ";
            }
        }
        /*
         * Exactly one CD4 count
         */
        if (obs.size() == 1 || duplicateCD4Obs) {
            for (Obs o : obs) {
                obsDatetime.setTime(o.getObsDatetime());
                if (obsDatetime.before(fiveMosAgo)) {
                    // Maybe sometime give a more intelligent message.
                    // int months = tomorrow.get(Calendar.MONTH) - obsDatetime.get(Calendar.MONTH);
                    // int years = tomorrow.get(Calendar.YEAR) - obsDatetime.get(Calendar.YEAR);
                    // String mosAgo = (months > 1) ? months + " months" : months + " month";
                    // String yrsAgo = (years > 1) ? years + " years" : years + " year";
                    // Right now this will suffice:
                    return "Please order CD4 count now (last CD4 count over 5 months ago).";
                } else if (obsDatetime.after(fiveMosAgo)
                        && obsDatetime.before(tomorrow)) {
                    Calendar nextCD4 = (Calendar) obsDatetime.clone();
                    nextCD4.add(Calendar.MONTH, 6);
                    return new String("Next CD4 count due in "
                            + reminderFormat.format(nextCD4.getTime())
                            + " (last CD4 count was " + o.getValueNumeric()
                            + " on "
                            + obsFormat.format(obsDatetime.getTime())
                            + ").");
                } else {
                    return " ";
                }
            }
        }
        return " ";
    }
    
    /**
     * Returns a patient property as a list
     * 
     * @param className
     * @param property
     * @return list of values for given property
     */
    @SuppressWarnings("unchecked")
    public Object[] getPatientAttrAsList(String className, String property) {
        return (Object[]) super.getPatientAttr(className, property, true);
    }

    protected List<EncounterType> validEncounterTypes = new Vector<EncounterType>(); // set

    // in
    // constructor

    protected Map<Integer, Encounter> patientIdLastValidEncounter = null;

    protected Map<Integer, Object /* Date */> patientIdLastValidEncounterDatetime = null;

    // protected Map<Integer, Map<Integer, List<List<Object>>>> conceptMap =
    // null;
    protected Map<Integer, Map<Integer, List<Object>>> conceptMap = null;

    protected Map<Integer, Concept> cachedConcepts = null;

    /**
     * 
     * @return List of [medication, date] objects
     */
    public List<List<Object>> getAmpathActiveMedications() {

        ClinicalSummaryService css = (ClinicalSummaryService) Context
                .getService(ClinicalSummaryService.class);

        // set up all of the obs and encounter maps
        if (conceptMap == null) {
            // conceptMap = new HashMap<Integer, Map<Integer,
            // List<List<Object>>>>();
            conceptMap = new HashMap<Integer, Map<Integer, List<Object>>>();

            List<String> attrs = new Vector<String>();
            attrs.add("obsDatetime");

            patientIdLastValidEncounter = patientSetService
                    .getEncountersByType(getPatientSet(), validEncounterTypes);
            patientIdLastValidEncounterDatetime = patientSetService
                    .getEncounterAttrsByType(getPatientSet(),
                            validEncounterTypes, "encounterDatetime");
            Collection<Encounter> encounters = patientIdLastValidEncounter
                    .values();

            Integer[] questionConceptIds = { 966, 1088, 1107, 1109, 1111, 1112,
                    1193, 1250, 1255, 1261, 1263, 1264, 1265, 1268, 1270, 1277,
                    1278 };

            ConceptService cs = Context.getConceptService();
            for (Integer conceptId : questionConceptIds) {
                conceptMap.put(conceptId, css.getObservationsForEncounters(
                        encounters, cs.getConcept(conceptId)));
            }

            Integer[] cachedConceptIds = { 656, 747 };

            cachedConcepts = new HashMap<Integer, Concept>();
            for (Integer conceptId : cachedConceptIds) {
                cachedConcepts.put(conceptId, cs.getConcept(conceptId));
            }

        }

        List<Object> meds = new Vector<Object>();

        Integer pId = getPatientId();

        Concept CHANGE_REGIMEN = new Concept(1259);
        Concept CONTINUE_REGIMEN = new Concept(1257);
        Concept STOP_ALL = new Concept(1260);
        Concept START_DRUGS = new Concept(1256);
        Concept DOSING_CHANGE = new Concept(981);

        addAll(meds, conceptMap.get(1193).get(pId)); // CURRENT MEDICATIONS
        addAll(meds, conceptMap.get(1112).get(pId)); // PATIENT REPORTED
        // CURRENT CRYPTOCOCCUS
        // TREATMENT
        addAll(meds, conceptMap.get(1109).get(pId)); // PATIENT REPORTED
        // CURRENT PCP
        // PROPHYLAXIS
        addAll(meds, conceptMap.get(1107).get(pId)); // PATIENT REPORTED
        // CURRENT TUBERCULOSIS
        // PROPHYLAXIS
        addAll(meds, conceptMap.get(1111).get(pId)); // PATIENT REPORTED
        // CURRENT TUBERCULOSIS
        // TREATMENT

        List<Object> ARVS_STARTED = conceptMap.get(1250).get(pId);
        if (ARVS_STARTED != null)
            addAll(meds, ARVS_STARTED);
        else {
            List<Object> ARV_PLAN = conceptMap.get(1255).get(pId);
            if (ARV_PLAN == null || ARV_PLAN.contains(CONTINUE_REGIMEN)) {
                addAll(meds, conceptMap.get(966).get(pId)); // CURRENT
                // ANTIRETROVIRAL
                // DRUGS USED FOR
                // TRANSMISSION
                // PROPHYLAXIS
                addAll(meds, conceptMap.get(1088).get(pId)); // CURRENT
                // ANTIRETROVIRAL
                // DRUGS USED
                // FOR TREATMENT
            }
        }

        List<Object> CRYPT_TREATMENT_PLAN = conceptMap.get(1277).get(pId);
        Concept FLUCONAZOLE = cachedConcepts.get(747);
        if (CRYPT_TREATMENT_PLAN != null
                && CRYPT_TREATMENT_PLAN.contains(STOP_ALL)) {
            meds.remove(FLUCONAZOLE);
        } else if (containsAny(CRYPT_TREATMENT_PLAN, new Concept[] {
                START_DRUGS, CONTINUE_REGIMEN }))
            meds.add(FLUCONAZOLE);
        addAll(meds, conceptMap.get(1278).get(pId)); // CRYPTOCOCCUS
        // TREATMENT STARTED

        List<Object> PCP_PRO_STARTED = conceptMap.get(1263).get(pId);
        List<Object> PCP_PRO_PLAN = conceptMap.get(1261).get(pId);
        if (PCP_PRO_STARTED != null
                || containsAny(PCP_PRO_PLAN, new Concept[] { START_DRUGS,
                        CHANGE_REGIMEN, STOP_ALL, DOSING_CHANGE })) {
            meds.remove(new Concept(916)); // TRIMETHOPRIM AND SULFAMETHOXAZOLE
            meds.remove(new Concept(92)); // DAPSONE
        }
        addAll(meds, conceptMap.get(1263).get(pId)); // PCP PROPHYLAXIS
        // STARTED

        List<Object> TB_PRO_PLAN = conceptMap.get(1265).get(pId);
        if (TB_PRO_PLAN != null && TB_PRO_PLAN.contains(STOP_ALL))
            meds.remove(cachedConcepts.get(656)); // ISONIAZID
        else if (containsAny(TB_PRO_PLAN, new Concept[] { START_DRUGS,
                CONTINUE_REGIMEN, DOSING_CHANGE })) {
            meds.add(cachedConcepts.get(656)); // ISONIAZID
        }
        addAll(meds, conceptMap.get(1264).get(pId)); // TUBERCULOSIS
        // PROPHYLAXIS STARTED

        List<Object> TB_TREATMENT_STARTED = conceptMap.get(1270).get(pId);
        List<Object> TB_TREATMENT_PLAN = conceptMap.get(1268).get(pId);
        if (TB_TREATMENT_STARTED != null
                || containsAny(TB_TREATMENT_PLAN, new Concept[] { START_DRUGS,
                        CHANGE_REGIMEN, STOP_ALL, DOSING_CHANGE })) {
            meds.remove(new Concept(1108)); // ETHAMBUTOL AND ISONIZAID
            meds.remove(new Concept(1131)); // RIFAMPICIN ISONIAZID PYRAZINAMIDE
            // AND ETHAMBUTOL
            meds.remove(new Concept(438)); // STREPTOMYCIN
            meds.remove(new Concept(5829)); // PYRAZINAMIDE
            meds.remove(new Concept(656)); // ISONIAZID
            meds.remove(new Concept(745)); // ETHAMBUTOL
            meds.remove(new Concept(767)); // RIFAMPICIN
            meds.remove(new Concept(768)); // RIFAMPICIN ISONIAZID AND
            // PYRAZINAMIDE
        }
        addAll(meds, conceptMap.get(1270).get(pId)); // TUBERCULOSIS
        // TREATMENT STARTED

        // remove the possible answers we don't care about
        List<Concept> nonAnswers = new Vector<Concept>();
        nonAnswers.add(new Concept(5622)); // OTHER NON-CODED
        nonAnswers.add(new Concept(1067)); // UNKNOWN
        nonAnswers.add(new Concept(1065)); // YES
        nonAnswers.add(new Concept(1107)); // NONE
        nonAnswers.add(new Concept(5424)); // OTHER ANTIRETROVIRAL DRUG
        nonAnswers.add(new Concept(5811)); // UNKNOWN ANTIRETROVIRAL DRUG

        meds.removeAll(nonAnswers);

        // the date on all meds will currently just be the encounter datetime
        Date medStartTime = (Date) patientIdLastValidEncounterDatetime.get(pId);

        // shrink the med list to just unique answers
        List<Object> uniqueMeds = new Vector<Object>();
        List<List<Object>> returnMeds = new Vector<List<Object>>();
        for (Object med : meds) {
            if (!uniqueMeds.contains(med)) {
                uniqueMeds.add(med);
                List<Object> medRow = new Vector<Object>();
                medRow.add(med);
                medRow.add(medStartTime);
                returnMeds.add(medRow);
            }
        }

        return returnMeds;
    }

    /**
     * Null <code>addAll<code> method
     * 
     * @param currentMeds
     * @param newMeds
     */
    private void addAll(List<Object> currentMeds, List<Object> newMeds) {
        if (currentMeds == null || newMeds == null)
            return;

        currentMeds.addAll(newMeds);
    }

    /**
     * Checks list <code>actualAnswers</code> for the existence of at least
     * one <code>possibleAnswers</code>
     * 
     * @param actualAnswers
     * @param possibleAnswers
     * @return
     */
    private Boolean containsAny(List<Object> actualAnswers,
            Concept[] possibleAnswers) {

        if (actualAnswers == null || possibleAnswers == null)
            return false;

        for (Concept possibleAnswer : possibleAnswers) {
            if (actualAnswers.contains(possibleAnswer))
                return true;
        }

        return false;
    }

    /**
     * Clean up the objects alloted
     * 
     * @see org.openmrs.reporting.export.DataExportFunctions#clear()
     */
    public void clear() {
        super.clear();

        patientIdObsValueMapLeft = null;
        patientIdObsValueMapRight = null;
    }

    /**
     * Date Comparator for obs:date lists. Assumes each entry in the list will
     * be a List<Object> with [0] = obs value and [1] = obs datetime
     * 
     * Sorts the list from newest to oldest
     */
    private class DateComparator implements Comparator<List<Object>> {
        public int compare(List<Object> a1, List<Object> a2) {
            Date d1 = (Date) a1.get(1);
            Date d2 = (Date) a2.get(1);
            int retVal = -1 * d1.compareTo(d2);
            if (retVal == 0) {
                Concept c1 = (Concept) a1.get(0);
                Concept c2 = (Concept) a2.get(0);
                retVal = c1.getName().getName().compareTo(
                        c2.getName().getName());
            }
            return retVal;
        }
    }
}