package org.openmrs.module.clinicalsummary.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.Index;
import org.openmrs.module.clinicalsummary.Summary;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import java.util.Date;
import java.util.List;

/**
 */
public class IndexServiceTest extends BaseModuleContextSensitiveTest {

	private static final Log log = LogFactory.getLog(IndexServiceTest.class);

	@Before
	public void setup() throws Exception {
		executeDataSet("org/openmrs/module/clinicalsummary/service/include/IndexServiceTest-dataset.xml");
	}

	/**
	 * @verifies save the Index to the database
	 * @see IndexService#saveIndex(org.openmrs.module.clinicalsummary.Index)
	 */
	@Test
	public void saveIndex_shouldSaveTheIndexToTheDatabase() {

		Summary summary = Context.getService(SummaryService.class).getSummary(1);
		Patient patient = Context.getPatientService().getPatient(7);
		Location location = Context.getLocationService().getLocation(1);

		Index index = new Index(patient, summary, new Date());
		index.setLocation(location);

		Assert.assertNull(index.getId());

		Context.getService(IndexService.class).saveIndex(index);

		Assert.assertNotNull(index.getId());
		Assert.assertEquals(patient, index.getPatient());
		Assert.assertEquals(summary, index.getSummary());
		Assert.assertEquals(location, index.getLocation());
	}

	/**
	 * @verifies return index with matching id
	 * @see IndexService#getIndex(Integer)
	 */
	@Test
	public void getIndex_shouldReturnIndexWithMatchingId() {
		Index index = Context.getService(IndexService.class).getIndex(1);
		Assert.assertNotNull(index);
		Assert.assertEquals(Integer.valueOf(1), index.getId());
	}

	/**
	 * @verifies return null when no Index match the id
	 * @see IndexService#getIndex(Integer)
	 */
	@Test
	public void getIndex_shouldReturnNullWhenNoIndexMatchTheId() {
		Index index = Context.getService(IndexService.class).getIndex(10);
		Assert.assertNull(index);
	}

	/**
	 * @verifies return all indexes for the location
	 * @see IndexService#getIndexes(org.openmrs.Location, Summary, java.util.Date, java.util.Date)
	 */
	@Test
	public void getIndexes_shouldReturnAllIndexesForTheLocation() {
		Location location = Context.getLocationService().getLocation(1);
		List<Index> indexes = Context.getService(IndexService.class).getIndexes(location, null, null, null);

		Assert.assertTrue(CollectionUtils.isNotEmpty(indexes));
		Assert.assertEquals(3, indexes.size());
		for (Index index : indexes)
			Assert.assertEquals(location, index.getLocation());
	}

	/**
	 * @verifies return empty list when no index are found for the location
	 * @see IndexService#getIndexes(org.openmrs.Location, Summary, java.util.Date, java.util.Date)
	 */
	@Test
	public void getIndexes_shouldReturnEmptyListWhenNoIndexAreFoundForTheLocation() {
		Location location = Context.getLocationService().getLocation(3);
		List<Index> indexes = Context.getService(IndexService.class).getIndexes(location, null, null, null);

		Assert.assertTrue(CollectionUtils.isEmpty(indexes));
		Assert.assertEquals(0, indexes.size());
	}

	/**
	 * @verifies return list of all index for a patient
	 * @see IndexService#getIndexes(org.openmrs.Patient)
	 */
	@Test
	public void getIndexes_shouldReturnListOfAllIndexForAPatient() {
		Patient patient = Context.getPatientService().getPatient(8);
		List<Index> indexes = Context.getService(IndexService.class).getIndexes(patient);

		Assert.assertTrue(CollectionUtils.isNotEmpty(indexes));
		Assert.assertEquals(2, indexes.size());
		for (Index index : indexes)
			Assert.assertEquals(patient, index.getPatient());
	}

	/**
	 * @verifies return empty list when no index are found for the patient
	 * @see IndexService#getIndexes(org.openmrs.Patient)
	 */
	@Test
	public void getIndexes_shouldReturnEmptyListWhenNoIndexAreFoundForThePatient() {
		Location location = Context.getLocationService().getLocation(6);
		List<Index> indexes = Context.getService(IndexService.class).getIndexes(location, null, null, null);

		Assert.assertTrue(CollectionUtils.isEmpty(indexes));
		Assert.assertEquals(0, indexes.size());
	}
}
