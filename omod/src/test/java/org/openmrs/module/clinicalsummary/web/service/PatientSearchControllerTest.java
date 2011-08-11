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
import org.junit.Test;
import org.openmrs.module.clinicalsummary.web.controller.service.PatientSearchController;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 */
public class PatientSearchControllerTest extends BaseModuleContextSensitiveTest {

	private static final Log log = LogFactory.getLog(PatientSearchControllerTest.class);

	@Autowired
	private PatientSearchController controller;

	/**
	 * @verifies should return patients with name search term
	 * @see org.openmrs.module.clinicalsummary.web.controller.service.PatientSearchController#searchPatient(String, String, String, javax.servlet.http.HttpServletResponse)
	 */
	@Test
	public void searchPatient_shouldReturnPatientsWithNameSearchTerm() throws Exception {
		/*
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("POST");
		request.setRequestURI("/module/clinicalsummary/service/patient/search");
		request.setParameter("term", "Collet");

		MockHttpServletResponse response = new MockHttpServletResponse();

		handlerAdapter.handle(request, response, controller);
		*/

		MockHttpServletResponse response = new MockHttpServletResponse();
		controller.searchPatient("admin", "test", "Collet", response);

		Assert.assertTrue(StringUtils.isNotEmpty(response.getContentAsString()));
		Assert.assertTrue(StringUtils.contains(response.getContentAsString(), "Collet Test Chebaskwony"));
		Assert.assertTrue(StringUtils.contains(response.getContentAsString(), "6TS-4"));
	}

	/**
	 * @verifies should return patients with identifier search term
	 * @see org.openmrs.module.clinicalsummary.web.controller.service.PatientSearchController#searchPatient(String, String, String, javax.servlet.http.HttpServletResponse)
	 */
	@Test
	public void searchPatient_shouldReturnPatientsWithIdentifierSearchTerm() throws Exception {
		/*
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("POST");
		request.setRequestURI("/module/clinicalsummary/service/patient/search");
		request.setParameter("term", "6TS-4");

		MockHttpServletResponse response = new MockHttpServletResponse();

		handlerAdapter.handle(request, response, controller);
		*/

		MockHttpServletResponse response = new MockHttpServletResponse();
		controller.searchPatient("admin", "test", "6TS-4", response);

		Assert.assertTrue(StringUtils.isNotEmpty(response.getContentAsString()));
		Assert.assertTrue(StringUtils.contains(response.getContentAsString(), "Collet Test Chebaskwony"));
		Assert.assertTrue(StringUtils.contains(response.getContentAsString(), "6TS-4"));
	}

	/**
	 * @verifies should return empty list when no patient match search term
	 * @see org.openmrs.module.clinicalsummary.web.controller.service.PatientSearchController#searchPatient(String, String, String, javax.servlet.http.HttpServletResponse)
	 */
	@Test
	public void searchPatient_shouldReturnEmptyListWhenNoPatientMatchSearchTerm() throws Exception {
		/*
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("POST");
		request.setRequestURI("/module/clinicalsummary/service/patient/search");
		request.setParameter("term", "999-3");

		MockHttpServletResponse response = new MockHttpServletResponse();

		handlerAdapter.handle(request, response, controller);
		*/

		MockHttpServletResponse response = new MockHttpServletResponse();
		controller.searchPatient("admin", "test", "999-3", response);

		Assert.assertTrue(StringUtils.isNotEmpty(response.getContentAsString()));
		Assert.assertFalse(StringUtils.contains(response.getContentAsString(), "999-3"));
	}
}
