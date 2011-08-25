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
package org.openmrs.module.clinicalsummary.extension.html;

import java.util.LinkedHashMap;
import java.util.Map;

import org.openmrs.api.context.Context;
import org.openmrs.module.Extension;
import org.openmrs.module.clinicalsummary.Constants;
import org.openmrs.module.web.extension.AdministrationSectionExt;

public class SummaryAdminExt extends AdministrationSectionExt {

	@Override
	public Extension.MEDIA_TYPE getMediaType() {
		return Extension.MEDIA_TYPE.html;
	}

	@Override
	public String getTitle() {
		return "clinicalsummary.title";
	}

	@Override
	public Map<String, String> getLinks() {

		Map<String, String> map = new LinkedHashMap<String, String>();

		if (Context.hasPrivilege(Constants.PRIVILEGE_MANAGE_SUMMARY)) {
			map.put("module/clinicalsummary/summary/summaryList.list", "clinicalsummary.summary");
			map.put("module/clinicalsummary/summary/mappingList.list", "clinicalsummary.mapping");
		}

		if (Context.hasPrivilege(Constants.PRIVILEGE_GENERATE_SUMMARY))
			map.put("module/clinicalsummary/evaluator/evaluateCohort.form", "clinicalsummary.generate");

		if (Context.hasPrivilege(Constants.PRIVILEGE_PRINT_SUMMARY))
			map.put("module/clinicalsummary/printer/printSummaries.form", "clinicalsummary.print");

		if (Context.hasPrivilege(Constants.PRIVILEGE_MANAGE_SUMMARY)) {
			map.put("module/clinicalsummary/download/downloadSummaries.list", "clinicalsummary.download");
			map.put("module/clinicalsummary/upload/uploadSummaries.list", "clinicalsummary.upload");
			map.put("module/clinicalsummary/utils/fileSize.list", "clinicalsummary.filesize");
			map.put("module/clinicalsummary/utils/initialSummaries.form", "clinicalsummary.initial");
			map.put("module/clinicalsummary/utils/orderedObsList.list", "clinicalsummary.ordered");
			map.put("module/clinicalsummary/reminder/reminderList.list", "clinicalsummary.reminder");
			map.put("module/clinicalsummary/response/responseList.list", "clinicalsummary.response");
		}

		return map;
	}

}
