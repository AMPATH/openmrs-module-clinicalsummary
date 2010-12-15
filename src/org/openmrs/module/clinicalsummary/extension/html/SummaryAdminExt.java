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
import org.openmrs.module.clinicalsummary.SummaryConstants;
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
		
		if (Context.hasPrivilege(SummaryConstants.PRIV_MANAGE_SUMMARY))
			map.put("module/clinicalsummary/summaryList.list", "clinicalsummary.manage");
		
		if (Context.hasPrivilege(SummaryConstants.PRIV_GENERATE_SUMMARY))
			map.put("module/clinicalsummary/generateSummaries.form", "clinicalsummary.generateSummaries");
		
		if (Context.hasPrivilege(SummaryConstants.PRIV_PRINT_SUMMARY))
			map.put("module/clinicalsummary/printSummaries.form", "clinicalsummary.printSummaries");
		
		if (Context.hasPrivilege(SummaryConstants.PRIV_MANAGE_SUMMARY)) {
			map.put("module/clinicalsummary/threadDownload.form", "clinicalsummary.threadDownload");
			map.put("module/clinicalsummary/threadUpload.form", "clinicalsummary.threadUpload");
		}
		
		if (Context.hasPrivilege(SummaryConstants.PRIV_PRINT_SUMMARY))
			map.put("module/clinicalsummary/summarySearch.form", "clinicalsummary.search");
		
		if (Context.hasPrivilege(SummaryConstants.PRIV_MANAGE_SUMMARY))
			map.put("module/clinicalsummary/initialSummary.form", "clinicalsummary.initial");
		
		if (Context.hasPrivilege(SummaryConstants.PRIV_MANAGE_SUMMARY))
			map.put("module/clinicalsummary/obsPairList.form", "clinicalsummary.obs.pair");
		
		return map;
	}
	
}
