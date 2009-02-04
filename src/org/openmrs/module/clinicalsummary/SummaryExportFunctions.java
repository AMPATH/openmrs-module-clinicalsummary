package org.openmrs.module.clinicalsummary;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.*;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.reporting.export.DataExportFunctions;

public class SummaryExportFunctions extends DataExportFunctions {

    public final Log log = LogFactory.getLog(this.getClass());

    protected Map<Integer, List<List<Object>>> patientIdObsValueMapLeft = null;

    protected Map<Integer, List<List<Object>>> patientIdObsValueMapRight = null;

    protected Set<Concept> testOrderConcepts = null;
        
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
     * @param attrListObj
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
     * @param 
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
        boolean sameAsOneCD4 = false;
        // output format for date
        DateFormat reminderFormat = new SimpleDateFormat("MMMMM yyyy");
        DateFormat obsFormat = new SimpleDateFormat("dd-MMM-yyyy");
        // tomorrow
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        // twoMosAgo
        Calendar twoMosAgo = Calendar.getInstance();
        twoMosAgo.add(Calendar.DAY_OF_YEAR, -60);
        // five months ago
        Calendar sixMosAgo = Calendar.getInstance();
        sixMosAgo.add(Calendar.DAY_OF_YEAR, -180);
        // eleven months ago
        Calendar twelveMosAgo = Calendar.getInstance();
        twelveMosAgo.add(Calendar.DAY_OF_YEAR, -360);
        // the time of this obs
        Calendar obsDatetime = Calendar.getInstance();
        Concept cd4 = getConcept("5497"); // name='CD4, BY FACS'
        Set<Obs> obs = Context.getObsService().getObservations(getPatient(),
                cd4, false);

        List<String> cd4Concepts = new ArrayList<String>(3);
        cd4Concepts.add("5497");
        cd4Concepts.add("1871");
        cd4Concepts.add("657");
        Obs pendingCD4 = getLatestPendingTest(cd4Concepts);
        // Get the latest of the pending cd4 count orders.
        Boolean pendingOrderForCD4 = false;
        if (pendingCD4 != null) {
            pendingOrderForCD4 = true;
            log.debug("pendingCD4: " + pendingCD4.getConcept() + ", " + pendingCD4.getObsDatetime());
        }
        log.debug("Pending Order for CD4?: " + pendingOrderForCD4);

        /*
         * No CD4 count.
         */
        if (obs.isEmpty()) {
            if (!pendingOrderForCD4) {
                return "Please order CD4 count now (no prior CD4 count on record).";
            } else if (pendingCD4.getObsDatetime().before(sixMosAgo.getTime())) {
                return "Please order CD4 count now (last CD4 ordered over 6 months ago).";
            }
        }
        /*
         * More than one CD4 count
         */
        else if (obs.size() > 1) {
            Calendar firstCD4 = Calendar.getInstance();
            Calendar lastCD4 = Calendar.getInstance();
            Calendar thisCD4 = Calendar.getInstance();
            Double firstCD4val = 0.0;
            Double lastCD4val = 0.0;
            int cnt = 0;
            for (Obs o : obs) {
                if (++cnt == 1) {
                    firstCD4.setTime(o.getObsDatetime());
                    lastCD4.setTime(o.getObsDatetime());
                    firstCD4val = o.getValueNumeric();
                    lastCD4val = o.getValueNumeric();
                }
                thisCD4.setTime(o.getObsDatetime());
                if (thisCD4.before(firstCD4)) {
                    firstCD4.setTime(thisCD4.getTime());
                    firstCD4val = o.getValueNumeric();
                }
                if (thisCD4.after(lastCD4)) {
                    lastCD4.setTime(thisCD4.getTime());
                    lastCD4val = o.getValueNumeric();
                }
            }
            log.debug("Time of firstCD4: " + firstCD4.getTime() + ", lastCD4: " + lastCD4.getTime());
            // All CD4's taken within this time frame can be considered just one CD4 count taken.
            if (Math.abs(lastCD4.getTimeInMillis() - firstCD4.getTimeInMillis()) < THREE_DAYS) {
                sameAsOneCD4 = true;
            // Two CD4's, one less than 400, last taken 6 mos ago, no recent order.
            } else if (obs.size() == 2 && (firstCD4val < 400 || lastCD4val < 400)
                    && lastCD4.before(sixMosAgo) && !pendingOrderForCD4) {
                Calendar order = Calendar.getInstance();
                order.setTime(lastCD4.getTime());
                order.add(Calendar.DAY_OF_YEAR, 180);
                // ... order cd4 on max(today, lastCD4 + 6mos).
                if (order.before(Calendar.getInstance())) {
                    order.setTime(new Date());
                }
                return new String("Please Order CD4 count now (one of" +
                        "first two CD4s was less than 400, repeat should be in 6 months)");
            // More than two CD4's and no recent order.
            } else if (lastCD4.before(sixMosAgo) && lastCD4val < 400 && !pendingOrderForCD4) {
                return new String("Please Order CD4 count now " +
                        "(last CD4 was less than 400, repeat should be in 6 months)");
            } else if (lastCD4.before(twelveMosAgo) && lastCD4val >= 400 && !pendingOrderForCD4) {
                return new String("Please order CD4 count now (last CD4 count over 12 months ago).");
            } else if (lastCD4val < 400 && pendingOrderForCD4 && pendingCD4.getObsDatetime().before(sixMosAgo.getTime())) {
                return new String("Please order CD4 count now (last CD4 &lt; 400 and CD4 ordered over 6 months ago).");
            } else if (lastCD4val >= 400 && pendingOrderForCD4 && pendingCD4.getObsDatetime().before(twelveMosAgo.getTime())) {
                return new String("Please order CD4 count now (last CD4 ordered over 12 months ago).");
            }
        }
        /*
         * Exactly one CD4 count
         */
        if (obs.size() == 1 || sameAsOneCD4) {
            for (Obs o : obs) {
                obsDatetime.setTime(o.getObsDatetime());
                if (obsDatetime.before(sixMosAgo) && !pendingOrderForCD4) {
                    return "Please order CD4 count now (last CD4 count over 6 months ago).";
                } else if (pendingOrderForCD4 && pendingCD4.getObsDatetime().before(sixMosAgo.getTime())) {
                    return "Please order CD4 count now (last CD4 ordered over 6 months ago).";
                }
            }
        }
        return " ";
    }

    /**
     * Initialize the set of available testOrderConcepts once.  Call this method
     * before using testOrderConcepts.
     * @return  false if already initialized, true otherwise
     */
    private Boolean initTestOrderConcepts() {
        if (testOrderConcepts != null && testOrderConcepts.size() > 0) {
            return false;
        }
        testOrderConcepts = new HashSet<Concept>();
        // Get the Concept for "TEST ORDERED"
        Concept testOrdered = conceptService.getConcept(Integer.valueOf(1271));
        Map<Concept, List<Concept>> exceptions = new HashMap<Concept, List<Concept>>();
        Concept cd4Panel = conceptService.getConcept(Integer.valueOf(657));
        // This should not be hard-coded... cd4 panel should be a set that contains these concepts.
        List<Concept> cd4PanelMembers = new ArrayList<Concept>();
        cd4PanelMembers.add(conceptService.getConcept(Integer.valueOf(5497))); // CD4, BY FACS
        cd4PanelMembers.add(conceptService.getConcept(Integer.valueOf(1871))); // LAST CD4, BY FACS
        exceptions.put(cd4Panel, cd4PanelMembers);
        for (ConceptAnswer test : testOrdered.getAnswers()) {
            // Add the test to the list of available tests...
            testOrderConcepts.add(test.getAnswerConcept());
            // ... and add set members ...
            if (test.getAnswerConcept().isSet()) {
                Collection<ConceptSet> sets = test.getAnswerConcept().getConceptSets();
                for (ConceptSet set : sets) {
                    testOrderConcepts.add(set.getConcept());
                }
            }
            // ... and add hard-coded exceptions for special cases
            if (exceptions.containsKey(test.getAnswerConcept())) {
                testOrderConcepts.addAll(exceptions.get(test.getAnswerConcept()));
            }
        }
        return true;
    }

    
    /**
     * Returns Map<Concept, Obs> of tests that were ordered and are still pending results.
     *
     * @return
     */
    public Map<Integer, Obs> getPendingTestsOrdered(List<String> testIdsOrNames) {
        // This must be called first!
        Boolean initialize = initTestOrderConcepts();
         // Get the Concept for "TEST ORDERED"
        Concept testOrdered = conceptService.getConcept(Integer.valueOf(1271));
        // Create a Concept set from the answer Concepts of all possible tests that could be ordered.
        Set<Concept> tests = new HashSet<Concept>(testOrdered.getAnswers().size());
        // Get pending tests from the parameter list of tests that are answers to concept 1271.
        if (null != testIdsOrNames && testIdsOrNames.size() > 0) {
            for (String test : testIdsOrNames) {
                Concept concept = Context.getConceptService().getConcept(test);
                if (null != concept && testOrderConcepts.contains(concept)) {
                    tests.add(concept);
                }
            }
        }
        // Otherwise get all pending tests for all answers to concept 1271
        else {
            tests.addAll(testOrderConcepts);
        }
        // This patient
        List<Person> whom = new ArrayList<Person>();
        whom.add(super.getPatient());
        // The only question is the "TEST ORDERED" Concept
        List<Concept> questions = new ArrayList<Concept>();
        questions.add(testOrdered);
        // Each answer will be searched individually for the last "TEST ORDERED" for this test.
        List<Concept> answers = new ArrayList<Concept>();
        // Collect each last test ordered into a set of orders.
        Map<Integer, Obs> latestPendingTests = new HashMap<Integer, Obs>(tests.size());
        // Collect each last test result into a set of results.
//        Map<Integer, Obs> latestTestResults = new HashMap<Integer, Obs>(tests.size());
        Calendar onOrAfter = Calendar.getInstance();
        List<String> sort = new Vector<String>();
        sort.add("obsDatetime");
        for (Concept test : tests) {
            try {
                answers.add(test);
                Integer conceptId = test.getConceptId();
                if (conceptId == 1019) {  // Complete Blood Count
                    answers.add(Context.getConceptService().getConcept(Integer.valueOf(21)));
                }
                else if (conceptId == 21) { // Hemoglobin
                    answers.add(Context.getConceptService().getConcept(Integer.valueOf(1019)));
                }
                else if (conceptId == 657) { // CD4 Panel
                    answers.add(Context.getConceptService().getConcept(Integer.valueOf(5497)));
                    answers.add(Context.getConceptService().getConcept(Integer.valueOf(1871)));
                }
                else if (conceptId == 5497) { // CD4, By FACS
                    answers.add(Context.getConceptService().getConcept(Integer.valueOf(657)));
                    answers.add(Context.getConceptService().getConcept(Integer.valueOf(1871)));
                }
                else if (conceptId == 1871) { // Last CD4, by FACS
                    answers.add(Context.getConceptService().getConcept(Integer.valueOf(5497)));
                    answers.add(Context.getConceptService().getConcept(Integer.valueOf(657)));
                }
                else if (conceptId == 856) { // HIV Viral Load, Quantitative
                    answers.add(Context.getConceptService().getConcept(Integer.valueOf(1305)));
                }
                else if (conceptId == 1305) { // HIV Viral Load, Qualitative
                    answers.add(Context.getConceptService().getConcept(Integer.valueOf(856)));
                }
                else if (conceptId == 1353) { // Chemistry Lab Tests
                    answers.add(Context.getConceptService().getConcept(Integer.valueOf(654)));
                }
                else if (conceptId == 654) { // SGPT
                    answers.add(Context.getConceptService().getConcept(Integer.valueOf(1353)));
                }
                String answerString = "Answers: ";
                for (Concept c : answers) {
                   answerString += c.getConceptId() + " ";
                }
                log.debug(answerString);
                // Get the latest test ordered.
                List<Obs> order = Context.getObsService().getObservations(whom, null, questions, answers,
                        null, null, sort, 1, null, null, null, false);
                // If there IS a test on order...
                if (order != null && order.size() > 0 && order.get(0) != null) {
                    log.debug("Latest test ordered: " + order.get(0).getValueCoded().getConceptId() + ", " + order.get(0).getObsDatetime());
                    // ...get the latest test result ON or AFTER the date of the lastest test ordered.
                    onOrAfter.setTime(order.get(0).getObsDatetime());
                    onOrAfter.add(Calendar.DAY_OF_YEAR, -1);
                    List<Obs> result = Context.getObsService().getObservations(whom, null, answers, null,
                            null, null, sort, 1, null, onOrAfter.getTime(), null, false);
                    // ...and if there are NO test results after the test was ordered...
                    if (result == null || result.size() < 1 || result.get(0) == null) {
                        // ... then this is a Pending Test Ordered
                        latestPendingTests.put(test.getConceptId(), order.get(0));
                        log.debug("Latest pending test: " + order.get(0).getValueCoded().getConceptId() + ", " + order.get(0).getObsDatetime());
                    }
                    else {
                        log.debug("Latest result: " + result.get(0).getConcept().getConceptId() + ", " + result.get(0).getObsDatetime());
                    }
                }
                log.debug(answerString);
                answers.clear();
            } catch (Exception e) {
                log.error("Obs for test " + test.getName().getName()
                        + " and patient " + patient.getPatientId()
                        + " could not be retrieved.", e);
                answers.clear();
            }
        }
        return latestPendingTests;
    }


    /**
     * Get the latest pending test order from the set of pending tests ordered.
     * @param testIdsOrNames only search from this list of test orders.
     * @return null if no tests are pending, the latest test order Obs otherwise
     */
    public Obs getLatestPendingTest(List<String> testIdsOrNames) {
        Map<Integer, Obs> tests = getPendingTestsOrdered(testIdsOrNames);
        Date tmpDate = null;
        Obs latestPendingTest = null;
        for (Integer key : tests.keySet()) {
            if (tmpDate == null || tests.get(key).getObsDatetime().after(tmpDate)) {
                tmpDate = tests.get(key).getObsDatetime();
                latestPendingTest = tests.get(key);
            }
        }
        return latestPendingTest;
    }

    /**
     * Convenience method to see if any tests listed in Concept 1271 are on order and pending results.
     * @return  true if there are tests pending with no Obs as result yet.
     */
    public Boolean hasPendingTestsOrdered() {
        Map<Integer, Obs> pending = getPendingTestsOrdered();
        if (pending.isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * Get the Obs for a pending Test Ordered for the given test.
     * @param conceptIdOrName Test Ordered.
     * @return
     */
    public Obs getPendingTestOrdered(String conceptIdOrName) {
        Concept concept = Context.getConceptService().getConcept(conceptIdOrName);
        if (concept != null) {
            List<String> conceptIds = new ArrayList<String>();
            conceptIds.add(concept.getConceptId().toString());
            return getPendingTestsOrdered(conceptIds).get(concept);
        }
        return null;
    }

    /**
     * Convenience no-parameter method to get pending tests ordered.
     * Returns Map<testOrderedConceptId, Obs> of any outstanding test orders from tests listed in concept 1271
     * @return
     */
    public Map<Integer, Obs> getPendingTestsOrdered() {
        return getPendingTestsOrdered(null);
    }

    /**
     * Returns Set<Obs> of the latest tests that were ordered.
     *
     * @return
     */
    public Set<Obs> getLatestTestsOrdered() {
        // Get the Concept for "TEST ORDERED"
        Concept testOrdered = conceptService.getConcept(Integer.valueOf(1271));
        // Create a Concept set from the answer Concepts of all possible tests that could be ordered.
        Set<Concept> tests = new HashSet<Concept>(testOrdered.getAnswers().size());
        for (ConceptAnswer test : testOrdered.getAnswers()) {
            tests.add(test.getAnswerConcept());
        }
        // This patient
        List<Person> whom = new ArrayList<Person>();
        whom.add(super.getPatient());
        // The only question is the "TEST ORDERED" Concept
        List<Concept> questions = new ArrayList<Concept>();
        questions.add(testOrdered);
        // Each answer will be searched individually for the last "TEST ORDERED" for this test.
        List<Concept> answers = new ArrayList<Concept>();
        // Collect each last test ordered into a set of orders.
        Set<Obs> latestTestsOrdered = new HashSet<Obs>(tests.size());
        // Collect each last test result into a set of results.
        for (Concept test : tests) {
            try {
                answers.add(test);
                // Get each latest test ordered.
                List<Obs> order = Context.getObsService().getObservations(whom, null, questions, answers,
                        null, null, null, 1, null, null, null, false);
                if (order != null && order.size() > 0 && order.get(0) != null) {
                    latestTestsOrdered.add(order.get(0));
                }
                answers.clear();
            } catch (Exception e) {
                log.error("Obs for test " + test.getName().getName()
                        + " and patient " + patient.getPatientId()
                        + " could not be retrieved.", e);
                answers.clear();
            }
        }
        return latestTestsOrdered;
    }

    /**
     * Returns Set<Obs> of the latest tests that were ordered.
     *
     * @return
     */
    public Obs getLatestTestOrdered(Concept test) {
        // Get the Concept for "TEST ORDERED"
        Concept testOrdered = conceptService.getConcept(Integer.valueOf(1271));
        // Create a Concept set from the answer Concepts of all possible tests that could be ordered.
        Set<Concept> availableTests = new HashSet<Concept>(testOrdered.getAnswers().size());
        for (ConceptAnswer answer : testOrdered.getAnswers()) {
            availableTests.add(answer.getAnswerConcept());
        }
        if (!availableTests.contains(test)) {
            log.debug("Concept " + test.getConceptId()
                    + " is not a valid Test to Order, not an answer for Concept 1271.");
            return null;
        }
        // This patient
        List<Person> whom = new ArrayList<Person>();
        whom.add(super.getPatient());
        // The only question is the "TEST ORDERED" Concept
        List<Concept> questions = new ArrayList<Concept>();
        questions.add(testOrdered);
        // Each answer will be searched individually for the last "TEST ORDERED" for this test.
        List<Concept> answers = new ArrayList<Concept>();
        answers.add(test);
        // Collect each last test ordered into a set of orders.
        Obs latestTestOrdered = null;
        // Collect each last test result into a set of results.
        try {
            // Get each latest test ordered.
            List<Obs> order = Context.getObsService().getObservations(whom, null, questions, answers,
                    null, null, null, 1, null, null, null, false);
            if (order != null && order.size() > 0 && order.get(0) != null) {
                latestTestOrdered = order.get(0);
            }
        } catch (Exception e) {
            log.error("Obs for test " + test.getName().getName()
                    + " and patient " + patient.getPatientId()
                    + " could not be retrieved.", e);
        }
        return latestTestOrdered;
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
        Concept REFILLED = new Concept(1406);

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
            if (ARV_PLAN == null || ARV_PLAN.contains(CONTINUE_REGIMEN)
                    || ARV_PLAN.contains(DOSING_CHANGE) || ARV_PLAN.contains(REFILLED)) {
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