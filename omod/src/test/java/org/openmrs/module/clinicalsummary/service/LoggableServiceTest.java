package org.openmrs.module.clinicalsummary.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.Loggable;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import java.util.List;

/**
 */
public class LoggableServiceTest extends BaseModuleContextSensitiveTest {

	private static final Log log = LogFactory.getLog(LoggableServiceTest.class);

	@Before
	public void setup() throws Exception {
		executeDataSet("org/openmrs/module/clinicalsummary/service/include/LoggableServiceTest-dataset.xml");
	}

	/**
	 * @verifies save the loggable object to the database
	 * @see LoggableService#saveLoggable(org.openmrs.module.clinicalsummary.Loggable)
	 */
	@Test
	public void saveLoggable_shouldSaveTheLoggableObjectToTheDatabase() {

		Patient patient = Context.getPatientService().getPatient(7);

		Loggable loggable = new Loggable();
		loggable.setMessage("First loggable message");
		loggable.setPatient(patient);

		LoggableService loggableService = Context.getService(LoggableService.class);
		loggableService.saveLoggable(loggable);

		Assert.assertNotNull(loggable.getId());
		Assert.assertEquals(patient, loggable.getPatient());
		Assert.assertEquals("First loggable message", loggable.getMessage());
	}

	/**
	 * @verifies return loggable object with the matching id
	 * @see LoggableService#getLoggable(Integer)
	 */
	@Test
	public void getLoggable_shouldReturnLoggableObjectWithTheMatchingId() {

		LoggableService loggableService = Context.getService(LoggableService.class);
		Loggable loggable = loggableService.getLoggable(1);

		Assert.assertNotNull(loggable);
		Assert.assertEquals(Integer.valueOf(1), loggable.getId());
	}

	/**
	 * @verifies return null when no loggable object are found
	 * @see LoggableService#getLoggable(Integer)
	 */
	@Test
	public void getLoggable_shouldReturnNullWhenNoLoggableObjectAreFound() {

		LoggableService loggableService = Context.getService(LoggableService.class);
		Loggable loggable = loggableService.getLoggable(10);

		Assert.assertNull(loggable);
	}

	/**
	 * @verifies return list of loggables for the patient
	 * @see LoggableService#getLoggables(org.openmrs.Patient)
	 */
	@Test
	public void getLoggables_shouldReturnListOfLoggablesForThePatient() {

		Patient patient = Context.getPatientService().getPatient(7);

		LoggableService loggableService = Context.getService(LoggableService.class);
		List<Loggable> loggables = loggableService.getLoggables(patient);

		Assert.assertTrue(CollectionUtils.isNotEmpty(loggables));
		Assert.assertEquals(3, loggables.size());
		for (Loggable loggable : loggables)
			Assert.assertEquals(patient, loggable.getPatient());
	}

	/**
	 * @verifies return empty list when no loggables are found for the patient
	 * @see LoggableService#getLoggables(org.openmrs.Patient)
	 */
	@Test
	public void getLoggables_shouldReturnEmptyListWhenNoLoggablesAreFoundForThePatient() {

		Patient patient = Context.getPatientService().getPatient(6);

		LoggableService loggableService = Context.getService(LoggableService.class);
		List<Loggable> loggables = loggableService.getLoggables(patient);

		Assert.assertTrue(CollectionUtils.isEmpty(loggables));
		Assert.assertEquals(0, loggables.size());
	}
}
