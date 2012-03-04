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

package org.openmrs.module.clinicalsummary.web.controller.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableNameConstants;
import org.openmrs.module.clinicalsummary.rule.medication.AntiRetroViralRule;
import org.openmrs.module.clinicalsummary.service.EvaluatorService;
import org.openmrs.util.OpenmrsUtil;

public class ExtendedData {

    private static final Log log = LogFactory.getLog(ExtendedData.class);

    private static final String FIELD_SEPARATOR = "|";

    private Patient patient;

    private Date referenceDate;

    private List<Encounter> encounters;

    private Map<Concept, List<Obs>> observationsByConcept;

    public ExtendedData(final Patient patient, final Date referenceDate) {
        this.referenceDate = referenceDate;
        this.patient = patient;
    }

    /**
     * @return
     */
    public Patient getPatient() {
        return patient;
    }

    /**
     * @param patient
     */
    public void setPatient(final Patient patient) {
        this.patient = patient;
    }

    /**
     * @return
     */
    public Date getReferenceDate() {
        return referenceDate;
    }

    /**
     * @param referenceDate
     */
    public void setReferenceDate(final Date referenceDate) {
        this.referenceDate = referenceDate;
    }

    /**
     * @return
     */
    public List<Encounter> getEncounters() {
        if (encounters == null)
            encounters = new ArrayList<Encounter>();
        return encounters;
    }

    /**
     * @param encounters
     */
    public void setEncounters(final List<Encounter> encounters) {
        this.encounters = encounters;
    }

    /**
     * @return
     */
    public Map<Concept, List<Obs>> getObservationsByConcept() {
        if (observationsByConcept == null)
            observationsByConcept = new LinkedHashMap<Concept, List<Obs>>();
        return observationsByConcept;
    }

    /**
     * @param observationsByConcept
     */
    public void setObservationsByConcept(final Map<Concept, List<Obs>> observationsByConcept) {
        this.observationsByConcept = observationsByConcept;
    }

    /**
     * @param concept
     * @param observations
     */
    public void addObservations(final Concept concept, final List<Obs> observations) {
        getObservationsByConcept().put(concept, observations);
    }

    /**
     * @param concept
     * @return
     */
    public List<Obs> getObservations(final Concept concept) {
        return getObservationsByConcept().get(concept);
    }

    /**
     * Search encounter that occur around the reference date. The reference date is the approximated date of when
     * the date should be entered by the data assistant.
     *
     * @return
     */
    private Encounter searchEncounterAfterReferenceDate(final Date referenceDate) {
        List<EncounterType> encounterTypes = Collections.emptyList();
        return searchEncounterAfterReferenceDate(encounterTypes, referenceDate);
    }

    private Encounter searchEncounterAfterReferenceDate(final List<EncounterType> encounterTypes,
                                                        final Date referenceDate) {
        Encounter encounter = null;

        Integer counter = 0;
        Boolean shouldStop = Boolean.FALSE;
        while (counter < getEncounters().size() && !shouldStop) {
            Encounter currentEncounter = getEncounters().get(counter++);
            if (currentEncounter.getEncounterDatetime().after(referenceDate)
                    || DateUtils.isSameDay(currentEncounter.getEncounterDatetime(), referenceDate)) {
                if (CollectionUtils.isEmpty(encounterTypes)
                        || OpenmrsUtil.collectionContains(encounterTypes, currentEncounter.getEncounterType()))
                    encounter = currentEncounter;
            } else
                shouldStop = Boolean.TRUE;
        }

        return encounter;
    }

    /**
     * Search obs that comes after reference date
     *
     * @param concept
     * @return
     */
    private Obs searchObservationAfterReferenceDate(final Concept concept, final Date referenceDate) {
        Obs obs = null;

        List<Obs> observations = getObservations(concept);

        Integer counter = 0;
        Boolean shouldStop = Boolean.FALSE;
        while (counter < observations.size() && !shouldStop) {
            // iterate over all observation for the concept
            Obs currentObs = observations.get(counter++);
            // only assign the current one to returned when the result date is after the reference date
            // at the end of the iteration, the returned result will hold the reference to the latest result after the encounter date
            if (currentObs.getObsDatetime().after(referenceDate)
                    || DateUtils.isSameDay(currentObs.getObsDatetime(), referenceDate))
                obs = currentObs;
            else
                shouldStop = Boolean.TRUE;
        }
        return obs;
    }

    /**
     * Search patient latest obs for certain concept before the study enrollment date
     *
     * @param concept       the concept
     * @param referenceDate the enrollment date
     * @return the obs before the enrollment date or null if there's no obs before the enrollment date
     */
    private Obs searchObservationBeforeReferenceDate(Concept concept, Date referenceDate) {
        Obs enrollmentObs = null;

        List<Obs> observations = getObservations(concept);

        Integer counter = 0;
        while (counter < observations.size() && enrollmentObs == null) {
            Obs currentObs = observations.get(counter++);
            if (currentObs.getObsDatetime().before(referenceDate)
                    || DateUtils.isSameDay(currentObs.getObsDatetime(), referenceDate))
                enrollmentObs = currentObs;
        }

        return enrollmentObs;
    }

    /**
     * Search patient latest encounter before the study enrollment date
     *
     * @param referenceDate the enrollment date
     * @return the encounter before the enrollment date or null if there's no encounter before the enrollment date
     */
    private Encounter searchEncounterBeforeReferenceDate(Date referenceDate) {
        Encounter enrollmentEncounter = null;

        Integer counter = 0;
        while (counter < getEncounters().size() && enrollmentEncounter == null) {
            Encounter currentEncounter = getEncounters().get(counter++);
            if (currentEncounter.getEncounterDatetime().before(referenceDate)
                    || DateUtils.isSameDay(currentEncounter.getEncounterDatetime(), referenceDate))
                enrollmentEncounter = currentEncounter;
        }

        return enrollmentEncounter;
    }

    /**
     * Calculate how many times the same provider provides care to the patient
     *
     * @param provider the provider
     * @return list of all encounters with matching provider
     */
    private List<Encounter> searchProvider(final Person provider) {
        List<Encounter> encounters = new ArrayList<Encounter>();
        for (Encounter encounter : getEncounters()) {
            if (OpenmrsUtil.nullSafeEquals(provider, encounter.getProvider()))
                encounters.add(encounter);
        }
        return encounters;
    }

    /**
     * Search for the patient first encounter
     *
     * @return the patient first encounter
     */
    private Encounter getInitialEncounter() {
        Encounter initialEncounter = null;
        if (CollectionUtils.isNotEmpty(encounters))
            initialEncounter = encounters.get(encounters.size() - 1);
        return initialEncounter;
    }

    /**
     * Search for the patient primary clinic
     *
     * @return the patient primary clinic based on the last 3 encounters
     */
    private Location getPrimaryClinic() {
        Location latestLocation = null;

        if (CollectionUtils.isNotEmpty(getEncounters())) {

            Integer counter = 0;
            Encounter encounter = encounters.get(counter++);
            latestLocation = encounter.getLocation();

            Location currentLocation = null;
            while (counter < getEncounters().size() && counter < 3) {
                encounter = encounters.get(counter++);

                if (!OpenmrsUtil.nullSafeEquals(encounter.getLocation(), currentLocation))
                    currentLocation = encounter.getLocation();

                if (OpenmrsUtil.nullSafeEquals(encounter.getLocation(), currentLocation)
                        || OpenmrsUtil.nullSafeEquals(encounter.getLocation(), latestLocation))
                    return currentLocation;
            }
        }

        return latestLocation;
    }

    /**
     * Search for all encounters of the patient to a particular location
     *
     * @param location the location
     * @return list of all relevant encounters
     */
    private List<Encounter> searchVisitCountForLocation(final Location location) {
        List<Encounter> encounterForLocation = new ArrayList<Encounter>();
        for (Encounter encounter : encounters)
            if (OpenmrsUtil.nullSafeEquals(location, encounter.getLocation()))
                encounterForLocation.add(encounter);

        return encounterForLocation;
    }

    public String generatePatientData() {
        // create the string holder
        StringBuilder builder = new StringBuilder();

        // append the initial identifier
        PatientIdentifier patientIdentifier = patient.getPatientIdentifier();

        String identifier = StringUtils.EMPTY;
        if (patientIdentifier != null)
            identifier = patientIdentifier.getIdentifier();
        builder.append(identifier).append(FIELD_SEPARATOR);

        // append the patient internal id
        builder.append(patient.getPatientId()).append(FIELD_SEPARATOR);

        // append the patient names
        PersonName patientName = getPatient().getPersonName();

        String name = StringUtils.EMPTY;
        if (patientName != null)
            name = patientName.getFullName();
        builder.append(name).append(FIELD_SEPARATOR);

        // append the patient age
        builder.append(patient.getAge()).append(FIELD_SEPARATOR);

        // append the gender
        builder.append(getPatient().getGender()).append(FIELD_SEPARATOR);

        // append initial encounter date
        Encounter initialEncounter = getInitialEncounter();

        String initialEncounterDatetime = StringUtils.EMPTY;
        if (initialEncounter != null)
            initialEncounterDatetime = Context.getDateFormat().format(initialEncounter.getEncounterDatetime());
        builder.append(initialEncounterDatetime).append(FIELD_SEPARATOR);

        // append the primary clinic
        Location primaryClinic = getPrimaryClinic();

        String primaryClinicName = StringUtils.EMPTY;
        if (primaryClinic != null)
            primaryClinicName = primaryClinic.getName();
        builder.append(primaryClinicName).append(FIELD_SEPARATOR);

        // append visit count to module 1
        Location location = Context.getLocationService().getLocation("MTRH Module 1");
        List<Encounter> encountersForLocation = searchVisitCountForLocation(location);
        builder.append(encountersForLocation.size()).append(FIELD_SEPARATOR);

        // append visit count to module 2
        location = Context.getLocationService().getLocation("MTRH Module 2");
        encountersForLocation = searchVisitCountForLocation(location);
        builder.append(encountersForLocation.size()).append(FIELD_SEPARATOR);

        // append visit count to module 2
        location = Context.getLocationService().getLocation("MTRH Module 3");
        encountersForLocation = searchVisitCountForLocation(location);
        builder.append(encountersForLocation.size()).append(FIELD_SEPARATOR);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2011);
        calendar.set(Calendar.MONTH, Calendar.JULY);
        calendar.set(Calendar.DATE, 1);

        Date enrollmentDate = calendar.getTime();
        Encounter enrollmentEncounter = searchEncounterBeforeReferenceDate(enrollmentDate);
        if (enrollmentEncounter != null) {
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put(EvaluableConstants.OBS_ENCOUNTER, Arrays.asList(enrollmentEncounter));

            EvaluatorService evaluatorService = Context.getService(EvaluatorService.class);

            Result result = evaluatorService.evaluate(patient, AntiRetroViralRule.TOKEN, parameters);

            String onMedication = String.valueOf(Boolean.FALSE);
            if (CollectionUtils.isEmpty(result))
                onMedication = String.valueOf(Boolean.TRUE);
            builder.append(onMedication).append(FIELD_SEPARATOR);
        }

        for (Concept concept : getObservationsByConcept().keySet()) {
            Obs obs = searchObservationBeforeReferenceDate(concept, enrollmentDate);

            String obsConceptName = StringUtils.EMPTY;
            if (concept != null)
                obsConceptName = concept.getName(Context.getLocale()).getName();
            builder.append(obsConceptName).append(FIELD_SEPARATOR);

            String obsId = StringUtils.EMPTY;
            if (obs != null)
                obsId = String.valueOf(obs.getObsId());
            builder.append(obsId).append(FIELD_SEPARATOR);

            String obsDatetime = StringUtils.EMPTY;
            if (obs != null)
                obsDatetime = Context.getDateFormat().format(obs.getObsDatetime());
            builder.append(obsDatetime).append(FIELD_SEPARATOR);

            String obsValue = StringUtils.EMPTY;
            if (obs != null)
                obsValue = obs.getValueAsString(Context.getLocale());
            builder.append(obsValue).append(FIELD_SEPARATOR);
        }

        return builder.toString();
    }

    public String generateEncounterData() {
        StringBuilder builder = new StringBuilder();

        // append the initial identifier
        PatientIdentifier patientIdentifier = patient.getPatientIdentifier();

        String identifier = StringUtils.EMPTY;
        if (patientIdentifier != null)
            identifier = patientIdentifier.getIdentifier();
        builder.append(identifier).append(FIELD_SEPARATOR);

        // append the patient internal id
        builder.append(patient.getPatientId()).append(FIELD_SEPARATOR);

        // append the patient names
        PersonName patientName = getPatient().getPersonName();
        String name = StringUtils.EMPTY;
        if (patientName != null)
            name = patientName.getFullName();
        builder.append(name).append(FIELD_SEPARATOR);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getReferenceDate());
        calendar.add(Calendar.DATE, -3);
        Date referenceDate = calendar.getTime();

        // Find the encounter data
        Encounter encounter = searchEncounterAfterReferenceDate(referenceDate);

        String encounterId = StringUtils.EMPTY;
        if (encounter != null)
            encounterId = String.valueOf(encounter.getEncounterId());
        builder.append(encounterId).append(FIELD_SEPARATOR);

        String encounterDatetime = StringUtils.EMPTY;
        if (encounter != null)
            encounterDatetime = Context.getDateFormat().format(encounter.getEncounterDatetime());
        builder.append(encounterDatetime).append(FIELD_SEPARATOR);

        String providerId = StringUtils.EMPTY;
        if (encounter != null)
            providerId = String.valueOf(encounter.getProvider().getPersonId());
        builder.append(providerId).append(FIELD_SEPARATOR);

        String providerName = StringUtils.EMPTY;
        if (encounter != null)
            providerName = encounter.getProvider().getPersonName().getFullName();
        builder.append(providerName).append(FIELD_SEPARATOR);

        String previousVisitCount = StringUtils.EMPTY;
        if (encounter != null)
            previousVisitCount = String.valueOf(searchProvider(encounter.getProvider()).size());
        builder.append(previousVisitCount).append(FIELD_SEPARATOR);

        for (Concept concept : getObservationsByConcept().keySet()) {
            Obs obs = searchObservationAfterReferenceDate(concept, referenceDate);

            String obsConceptName = StringUtils.EMPTY;
            if (concept != null)
                obsConceptName = concept.getName(Context.getLocale()).getName();
            builder.append(obsConceptName).append(FIELD_SEPARATOR);

            String obsId = StringUtils.EMPTY;
            if (obs != null)
                obsId = String.valueOf(obs.getObsId());
            builder.append(obsId).append(FIELD_SEPARATOR);

            String obsDatetime = StringUtils.EMPTY;
            if (obs != null)
                obsDatetime = Context.getDateFormat().format(obs.getObsDatetime());
            builder.append(obsDatetime).append(FIELD_SEPARATOR);

            String obsValue = StringUtils.EMPTY;
            if (obs != null)
                obsValue = obs.getValueAsString(Context.getLocale());
            builder.append(obsValue).append(FIELD_SEPARATOR);
        }

        EncounterService service = Context.getEncounterService();

        EncounterType expressHighRiskEncounterType = service.getEncounterType(EvaluableNameConstants.ECHIGHRISK);
        Encounter expressHighRiskEncounter = searchEncounterAfterReferenceDate(
                Arrays.asList(expressHighRiskEncounterType), referenceDate);

        builder.append(EvaluableNameConstants.ECHIGHRISK).append(FIELD_SEPARATOR);

        String expressHighRiskEncounterId = StringUtils.EMPTY;
        if (expressHighRiskEncounter != null)
            expressHighRiskEncounterId = String.valueOf(expressHighRiskEncounter.getId());
        builder.append(expressHighRiskEncounterId).append(FIELD_SEPARATOR);

        String expressHighRiskEncounterDatetime = StringUtils.EMPTY;
        if (expressHighRiskEncounter != null)
            expressHighRiskEncounterDatetime = Context.getDateFormat().format(expressHighRiskEncounter.getEncounterDatetime());
        builder.append(expressHighRiskEncounterDatetime).append(FIELD_SEPARATOR);

        EncounterType expressStableEncounterType = service.getEncounterType(EvaluableNameConstants.ECSTABLE);
        Encounter expressStableEncounter = searchEncounterAfterReferenceDate(
                Arrays.asList(expressStableEncounterType), referenceDate);

        builder.append(EvaluableNameConstants.ECSTABLE).append(FIELD_SEPARATOR);

        String expressStableEncounterId = StringUtils.EMPTY;
        if (expressStableEncounter != null)
            expressStableEncounterId = String.valueOf(expressStableEncounter.getId());
        builder.append(expressStableEncounterId).append(FIELD_SEPARATOR);

        String expressStableEncounterDatetime = StringUtils.EMPTY;
        if (expressStableEncounter != null)
            expressStableEncounterDatetime = Context.getDateFormat().format(expressStableEncounter.getEncounterDatetime());
        builder.append(expressStableEncounterDatetime).append(FIELD_SEPARATOR);


        return builder.toString();
    }
}
