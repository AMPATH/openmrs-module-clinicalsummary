package org.openmrs.module.clinicalsummary.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.EncounterType;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.Mapping;
import org.openmrs.module.clinicalsummary.Summary;
import org.openmrs.module.clinicalsummary.enumeration.MappingType;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 */
public class SummaryServiceTest extends BaseModuleContextSensitiveTest {

	private static final Log log = LogFactory.getLog(SummaryServiceTest.class);

	@Before
	public void setup() throws Exception {
		executeDataSet("org/openmrs/module/clinicalsummary/service/include/SummaryServiceTest-dataset.xml");
	}

	/**
	 * @verifies save summary object to the database
	 * @see SummaryService#saveSummary(org.openmrs.module.clinicalsummary.Summary)
	 */
	@Test
	public void saveSummary_shouldSaveSummaryObjectToTheDatabase() {

		Summary summary = new Summary();
		summary.setName("Second Summary Template");
		summary.setDescription("Second summary template description");
		summary.setXml("Some xml text");
		summary.setXslt("Some xslt text");

		assertNull(summary.getId());
		Assert.assertEquals(Integer.valueOf(0), summary.getRevision());

		SummaryService summaryService = Context.getService(SummaryService.class);

		summaryService.saveSummary(summary);

		Assert.assertNotNull(summary.getId());
		Assert.assertEquals(Integer.valueOf(1), summary.getRevision());
		Assert.assertEquals("Second Summary Template", summary.getName());
		Assert.assertEquals("Second summary template description", summary.getDescription());
		Assert.assertEquals("Some xml text", summary.getXml());
		Assert.assertEquals("Some xslt text", summary.getXslt());
	}

	/**
	 * @verifies update the summary object to the database
	 * @see SummaryService#saveSummary(org.openmrs.module.clinicalsummary.Summary)
	 */
	@Test
	public void saveSummary_shouldUpdateTheSummaryObjectToTheDatabase() {

		SummaryService summaryService = Context.getService(SummaryService.class);

		Summary summary = summaryService.getSummary(1);

		Assert.assertEquals(Integer.valueOf(0), summary.getRevision());
		Assert.assertEquals("First Summary Template", summary.getName());

		summary.setName("Zero Summary Template");

		summaryService.saveSummary(summary);
		Assert.assertEquals(Integer.valueOf(1), summary.getRevision());
		Assert.assertEquals("Zero Summary Template", summary.getName());
	}

	/**
	 * @verifies return summary object with the input id
	 * @see SummaryService#getSummary(Integer)
	 */
	@Test
	public void getSummary_shouldReturnSummaryObjectWithTheInputId() {

		SummaryService summaryService = Context.getService(SummaryService.class);

		Summary summary = summaryService.getSummary(1);
		Assert.assertEquals(Integer.valueOf(1), summary.getId());
	}

	/**
	 * @verifies return null when no summary found with the input id
	 * @see SummaryService#getSummary(Integer)
	 */
	@Test
	public void getSummary_shouldReturnNullWhenNoSummaryFoundWithTheInputId() {

		SummaryService summaryService = Context.getService(SummaryService.class);

		Summary summary = summaryService.getSummary(10);
		assertNull(summary);
	}

	/**
	 * @verifies return all registered summaries
	 * @see SummaryService#getAllSummaries()
	 */
	@Test
	public void getAllSummaries_shouldReturnAllRegisteredSummaries() {

		SummaryService summaryService = Context.getService(SummaryService.class);

		List<Summary> summaries = summaryService.getAllSummaries();
		Assert.assertTrue(CollectionUtils.isNotEmpty(summaries));
		Assert.assertEquals(2, summaries.size());
	}

	/**
	 * @verifies return empty list when no summary are registered
	 * @see SummaryService#getAllSummaries()
	 */
	@Test
	public void getAllSummaries_shouldReturnEmptyListWhenNoSummaryAreRegistered() {

		SummaryService summaryService = Context.getService(SummaryService.class);

		List<Summary> summaries = summaryService.getAllSummaries();
		Assert.assertTrue(CollectionUtils.isNotEmpty(summaries));
		Assert.assertEquals(2, summaries.size());

		for (Summary summary : summaries) {
			summary.setRetired(Boolean.TRUE);
			summary.setRetiredBy(Context.getAuthenticatedUser());
			summary.setDateRetired(new Date());
			summary.setRetireReason(StringUtils.EMPTY);

			summaryService.saveSummary(summary);
		}

		summaries = summaryService.getAllSummaries();
		Assert.assertTrue(CollectionUtils.isEmpty(summaries));
	}

	/**
	 * @verifies saved the newly created summary mapping
	 * @see SummaryService#saveMapping(org.openmrs.module.clinicalsummary.Mapping)
	 */
	@Test
	public void saveMapping_shouldSavedTheNewlyCreatedSummaryMapping() {

		SummaryService summaryService = Context.getService(SummaryService.class);

		Summary summary = summaryService.getSummary(1);
		EncounterType encounterType = Context.getEncounterService().getEncounterType(1);

		Mapping mapping = new Mapping();
		mapping.setEncounterType(encounterType);
		mapping.setSummary(summary);
		mapping.setMappingType(MappingType.LATEST_ENCOUNTER);

		assertNull(mapping.getId());

		summaryService.saveMapping(mapping);

		Assert.assertNotNull(mapping.getId());
		Assert.assertEquals(MappingType.LATEST_ENCOUNTER, mapping.getMappingType());
		Assert.assertEquals(encounterType, mapping.getEncounterType());
		Assert.assertEquals(summary, mapping.getSummary());
	}

	/**
	 * @verifies return all saved summary mappings
	 * @see SummaryService#getAllMappings()
	 */
	@Test
	public void getAllMappings_shouldReturnAllSavedSummaryMappings() {

		SummaryService summaryService = Context.getService(SummaryService.class);

		List<Mapping> mappings = summaryService.getAllMappings();
		Assert.assertTrue(CollectionUtils.isNotEmpty(mappings));
		Assert.assertEquals(4, mappings.size());

		EncounterType encounterType = Context.getEncounterService().getEncounterType(1);
		Summary summary = summaryService.getSummary(1);

		Mapping mapping = new Mapping();
		mapping.setEncounterType(encounterType);
		mapping.setSummary(summary);
		mapping.setMappingType(MappingType.LATEST_ENCOUNTER);

		summaryService.saveMapping(mapping);

		mappings = summaryService.getAllMappings();
		Assert.assertTrue(CollectionUtils.isNotEmpty(mappings));
		Assert.assertEquals(5, mappings.size());
	}

	/**
	 * @verifies return all mappings for a certain summary
	 * @see SummaryService#getMappings(org.openmrs.module.clinicalsummary.Summary, org.openmrs.EncounterType,
	 *      org.openmrs.module.clinicalsummary.enumeration.MappingType)
	 */
	@Test
	public void getMappings_shouldReturnAllMappingsForACertainSummary() {

		SummaryService summaryService = Context.getService(SummaryService.class);

		Summary summary = summaryService.getSummary(1);
		List<Mapping> mappings = summaryService.getMappings(summary, null, null);
		Assert.assertTrue(CollectionUtils.isNotEmpty(mappings));
		Assert.assertEquals(2, mappings.size());
		for (Mapping mapping : mappings)
			Assert.assertEquals(summary, mapping.getSummary());
	}

	/**
	 * @verifies return all mappings for the encounter type
	 * @see SummaryService#getMappings(org.openmrs.module.clinicalsummary.Summary, org.openmrs.EncounterType,
	 *      org.openmrs.module.clinicalsummary.enumeration.MappingType) )
	 */
	@Test
	public void getMappings_shouldReturnAllMappingsForTheEncounterType() {

		EncounterType encounterType = Context.getEncounterService().getEncounterType(1);

		SummaryService summaryService = Context.getService(SummaryService.class);

		List<Mapping> mappings = summaryService.getMappings(null, encounterType, null);
		Assert.assertTrue(CollectionUtils.isNotEmpty(mappings));
		Assert.assertEquals(2, mappings.size());
		for (Mapping mapping : mappings)
			Assert.assertEquals(encounterType, mapping.getEncounterType());
	}

    @Test
    public void getSummaryByUuid_shouldReturnSummaryObjectWithUUID() throws Exception {
        SummaryService summaryService = Context.getService(SummaryService.class);
        Summary summaryByUuid = summaryService.getSummaryByUuid("61ae96f4-6afe-4351-b6f8-fd4fc383cce1");

        assertNotNull(summaryByUuid);
    }

    @Test
    public void getSummaryByUuid_shouldReturnNullWithNotObjectWithUUID() throws Exception {
        SummaryService summaryService = Context.getService(SummaryService.class);
        Summary summaryByUuid = summaryService.getSummaryByUuid("random-uuid");

        assertNull(summaryByUuid);
    }
}
