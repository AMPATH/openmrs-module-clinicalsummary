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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.ServiceContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import java.util.List;

@Controller
@RequestMapping("/module/clinicalsummary/utils/fileSize")
public class FileSizeController {

	private static final Log log = LogFactory.getLog(FileSizeController.class);

	@RequestMapping(method = RequestMethod.GET)
	public void populatePage(final ModelMap map) {
	}

	@RequestMapping(method = RequestMethod.POST)
	public void processSubmit(final @RequestParam(required = false, value = "size") Integer size) {
		List<CommonsMultipartResolver> resolvers = ServiceContext.getInstance().getRegisteredComponents(CommonsMultipartResolver.class);
		for (CommonsMultipartResolver resolver : resolvers)
			resolver.setMaxUploadSize(size);
	}

}
