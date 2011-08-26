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

package org.openmrs.module.clinicalsummary.web.controller.response;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.enumeration.ActionType;
import org.openmrs.module.clinicalsummary.service.UtilService;
import org.openmrs.module.clinicalsummary.util.response.MedicationResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ResponseIgnoreController {

	private static final Log log = LogFactory.getLog(ResponseIgnoreController.class);

	@RequestMapping(method = RequestMethod.POST, value = "/module/clinicalsummary/response/ignoreResponse")
	public
	@ResponseBody
	Boolean processIgnore(final @RequestParam(required = true, value = "id") Integer responseId,
	                      final @RequestParam(required = true, value = "comment") String comment) {

		if (Context.isAuthenticated()) {
			UtilService service = Context.getService(UtilService.class);
			MedicationResponse medicationResponse = service.getResponse(MedicationResponse.class, responseId);
			medicationResponse.setReviewer(Context.getAuthenticatedUser().getPerson());
			medicationResponse.setDateReviewed(new Date());
			medicationResponse.setReviewComment(comment);
			medicationResponse.setActionType(ActionType.ACTION_IGNORED);
			service.saveResponse(medicationResponse);

			return Boolean.TRUE;
		}

		return Boolean.FALSE;
	}

}
