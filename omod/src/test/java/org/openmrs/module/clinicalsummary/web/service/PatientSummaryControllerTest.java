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

package org.openmrs.module.clinicalsummary.web.service;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.clinicalsummary.web.controller.service.PatientSummaryController;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 */
public class PatientSummaryControllerTest extends BaseModuleContextSensitiveTest {

	private static final Log log = LogFactory.getLog(PatientSummaryControllerTest.class);

	@Autowired
	private PatientSummaryController controller;

	@Before
	public void prepare() throws Exception {
		executeDataSet("org/openmrs/module/clinicalsummary/service/include/IndexServiceTest-dataset.xml");
	}

	/**
	 * @verifies return summary data for patient and summary
	 * @see org.openmrs.module.clinicalsummary.web.controller.service.PatientSummaryController#searchSummary(String, String, String, Integer, javax.servlet.http.HttpServletResponse)
	 */
	@Test
	public void searchSummary_shouldReturnSummaryDataForPatientAndSummary() throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();
		controller.searchSummary("admin", "test", "7", 3, response);

		Assert.assertFalse(StringUtils.isNotEmpty(response.getContentAsString()));
	}

	/**
	 * @verifies return empty data when no index found for the patient and summary
	 * @see org.openmrs.module.clinicalsummary.web.controller.service.PatientSummaryController#searchSummary(String, String, String, Integer, javax.servlet.http.HttpServletResponse)
	 */
	@Test
	public void searchSummary_shouldReturnEmptyDataWhenNoIndexFoundForThePatientAndSummary() throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();
		controller.searchSummary("admin", "test", "7", 4, response);

		Assert.assertFalse(StringUtils.isNotEmpty(response.getContentAsString()));
	}
}
