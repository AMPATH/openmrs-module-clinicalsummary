package org.openmrs.module.clinicalsummary.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.enumeration.FetchOrdering;
import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
import org.openmrs.module.clinicalsummary.util.FetchRestriction;
import org.openmrs.test.BaseModuleContextSensitiveTest;

/**
 */
public class CoreServiceTest extends BaseModuleContextSensitiveTest {

	private static final Log log = LogFactory.getLog(CoreServiceTest.class);

	@Before
	public void setup() throws Exception {
		executeDataSet("org/openmrs/module/clinicalsummary/service/include/CoreServiceTest-dataset.xml");
	}

	/**
	 * @verifies return all patient id with certain location on their observations between certain date
	 * @see CoreService#getDateCreatedCohort(org.openmrs.Location, java.util.Date, java.util.Date)
	 */
	@Test
	public void getCohortByLocation_shouldReturnAllPatientIdWithCertainLocationOnTheirObservationsBetweenCertainDate() {
		try {
			Location location = Context.getLocationService().getLocation(1);

			Date startDate = Context.getDateFormat().parse("01/01/2005");
			Date endDate = Context.getDateFormat().parse("01/01/2011");

			CoreService coreService = Context.getService(CoreService.class);
			Cohort cohort = coreService.getDateCreatedCohort(location, startDate, endDate);

			Assert.assertNotNull(cohort);
			Assert.assertTrue(CollectionUtils.isNotEmpty(cohort.getMemberIds()));
			Assert.assertEquals("7", StringUtils.join(cohort.getMemberIds(), ","));

		} catch (Exception e) {
			log.info("Exception thrown in the test ...", e);
		}
	}

	/**
	 * @verifies return empty cohort when no patient match the criteria
	 * @see CoreService#getDateCreatedCohort(org.openmrs.Location, java.util.Date, java.util.Date)
	 */
	@Test
	public void getCohortByLocation_shouldReturnEmptyCohortWhenNoPatientMatchTheCriteria() {
		try {
			Location location = Context.getLocationService().getLocation(1);

			Date startDate = Context.getDateFormat().parse("01/01/2005");
			Date endDate = Context.getDateFormat().parse("01/01/2007");

			CoreService coreService = Context.getService(CoreService.class);
			Cohort cohort = coreService.getDateCreatedCohort(location, startDate, endDate);

			Assert.assertNotNull(cohort);
			Assert.assertTrue(CollectionUtils.isEmpty(cohort.getMemberIds()));
			Assert.assertEquals(StringUtils.EMPTY, StringUtils.join(cohort.getMemberIds(), ","));

		} catch (Exception e) {
			log.info("Exception thrown in the test ...", e);
		}
	}

	/**
	 * @verifies return all encounters that match the search criteria
	 * @see CoreService#getPatientEncounters(Integer, java.util.Map, org.openmrs.module.clinicalsummary.util.FetchRestriction)
	 */
	@Test
	public void getPatientEncounters_shouldReturnAllEncountersThatMatchTheSearchCriteria() {

		Location firstLocation = Context.getLocationService().getLocation(1);
		Location secondLocation = Context.getLocationService().getLocation(2);

		EncounterType firstEncounterType = Context.getEncounterService().getEncounterType(1);
		EncounterType secondEncounterType = Context.getEncounterService().getEncounterType(2);
		EncounterType sixEncounterType = Context.getEncounterService().getEncounterType(6);

		CoreService coreService = Context.getService(CoreService.class);

		Map<String, Collection<OpenmrsObject>> restrictions = new Hashtable<String, Collection<OpenmrsObject>>();
		restrictions.put(EvaluableConstants.ENCOUNTER_LOCATION, Arrays.<OpenmrsObject>asList(firstLocation));

		FetchRestriction fetchRestriction = new FetchRestriction();

		List<Encounter> encounters = coreService.getPatientEncounters(7, restrictions, fetchRestriction);
		Assert.assertNotNull(encounters);
		Assert.assertTrue(CollectionUtils.isNotEmpty(encounters));
		Assert.assertEquals(2, encounters.size());
		Assert.assertEquals(Integer.valueOf(4), encounters.get(0).getEncounterId());

		fetchRestriction.setFetchOrdering(FetchOrdering.ORDER_ASCENDING);
		encounters = coreService.getPatientEncounters(7, restrictions, fetchRestriction);
		Assert.assertEquals(Integer.valueOf(3), encounters.get(0).getEncounterId());

		fetchRestriction.setSize(1);
		encounters = coreService.getPatientEncounters(7, restrictions, fetchRestriction);
		Assert.assertEquals(1, encounters.size());

		fetchRestriction = new FetchRestriction();

		restrictions.put(EvaluableConstants.ENCOUNTER_TYPE, Arrays.<OpenmrsObject>asList(firstEncounterType));
		encounters = coreService.getPatientEncounters(7, restrictions, fetchRestriction);
		Assert.assertEquals(1, encounters.size());
		Assert.assertEquals(Integer.valueOf(4), encounters.get(0).getEncounterId());
	}

	/**
	 * @verifies return empty list when no encounter match the criteria
	 * @see CoreService#getPatientEncounters(Integer, java.util.Map, org.openmrs.module.clinicalsummary.util.FetchRestriction)
	 */
	@Test
	public void getPatientEncounters_shouldReturnEmptyListWhenNoEncounterMatchTheCriteria() {
		Location firstLocation = Context.getLocationService().getLocation(1);

		CoreService coreService = Context.getService(CoreService.class);

		Map<String, Collection<OpenmrsObject>> restrictions = new Hashtable<String, Collection<OpenmrsObject>>();
		restrictions.put(EvaluableConstants.ENCOUNTER_LOCATION, Arrays.<OpenmrsObject>asList(firstLocation));

		FetchRestriction fetchRestriction = new FetchRestriction();

		List<Encounter> encounters = coreService.getPatientEncounters(10, restrictions, fetchRestriction);
		Assert.assertNotNull(encounters);
		Assert.assertTrue(CollectionUtils.isEmpty(encounters));
	}

	/**
	 * @verifies return all observations that match the search criteria
	 * @see CoreService#getPatientObservations(Integer, java.util.Map, org.openmrs.module.clinicalsummary.util.FetchRestriction)
	 */
	@Test
	public void getPatientObservations_shouldReturnAllObservationsThatMatchTheSearchCriteria() {

		Concept sevenConcept = Context.getConceptService().getConcept(7);
		Concept twentyOneConcept = Context.getConceptService().getConcept(21);
		Concept fiveSevenconcept = Context.getConceptService().getConcept(5497);

		Encounter thirdEncounter = Context.getEncounterService().getEncounter(3);
		Encounter fourthEncounter = Context.getEncounterService().getEncounter(4);

		Map<String, Collection<OpenmrsObject>> restrictions = new Hashtable<String, Collection<OpenmrsObject>>();

		CoreService coreService = Context.getService(CoreService.class);

		restrictions.put(EvaluableConstants.OBS_ENCOUNTER, Arrays.<OpenmrsObject>asList(thirdEncounter));
		List<Obs> observations = coreService.getPatientObservations(7, restrictions, new FetchRestriction());
		Assert.assertNotNull(observations);
		Assert.assertTrue(CollectionUtils.isNotEmpty(observations));
		Assert.assertEquals(2, observations.size());

		restrictions.put(EvaluableConstants.OBS_ENCOUNTER, Arrays.<OpenmrsObject>asList(fourthEncounter));
		observations = coreService.getPatientObservations(7, restrictions, new FetchRestriction());
		Assert.assertNotNull(observations);
		Assert.assertTrue(CollectionUtils.isNotEmpty(observations));
		Assert.assertEquals(6, observations.size());

		restrictions.put(EvaluableConstants.OBS_CONCEPT, Arrays.<OpenmrsObject>asList(fiveSevenconcept));
		observations = coreService.getPatientObservations(7, restrictions, new FetchRestriction());
		Assert.assertEquals(1, observations.size());

		restrictions.remove(EvaluableConstants.OBS_ENCOUNTER);
		observations = coreService.getPatientObservations(7, restrictions, new FetchRestriction());
		Assert.assertEquals(2, observations.size());

		restrictions.put(EvaluableConstants.OBS_CONCEPT, Arrays.<OpenmrsObject>asList(twentyOneConcept));
		restrictions.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.<OpenmrsObject>asList(sevenConcept));
		observations = coreService.getPatientObservations(7, restrictions, new FetchRestriction());
		Assert.assertEquals(1, observations.size());
	}

	/**
	 * @verifies return empty list when no observation match the criteria
	 * @see CoreService#getPatientObservations(Integer, java.util.Map, org.openmrs.module.clinicalsummary.util.FetchRestriction)
	 */
	@Test
	public void getPatientObservations_shouldReturnEmptyListWhenNoObservationMatchTheCriteria() {
		Concept sevenConcept = Context.getConceptService().getConcept(7);

		CoreService coreService = Context.getService(CoreService.class);

		Map<String, Collection<OpenmrsObject>> restrictions = new Hashtable<String, Collection<OpenmrsObject>>();
		restrictions.put(EvaluableConstants.OBS_CONCEPT, Arrays.<OpenmrsObject>asList(sevenConcept));

		FetchRestriction fetchRestriction = new FetchRestriction();

		List<Obs> observations = coreService.getPatientObservations(21, restrictions, fetchRestriction);
		Assert.assertNotNull(observations);
		Assert.assertTrue(CollectionUtils.isEmpty(observations));
	}
}
