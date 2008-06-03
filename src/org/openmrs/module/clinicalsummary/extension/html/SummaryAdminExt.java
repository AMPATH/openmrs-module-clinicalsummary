package org.openmrs.module.clinicalsummary.extension.html;

import java.util.LinkedHashMap;
import java.util.Map;

import org.openmrs.api.context.Context;
import org.openmrs.module.Extension;
import org.openmrs.module.web.extension.AdministrationSectionExt;

public class SummaryAdminExt extends AdministrationSectionExt {

	public Extension.MEDIA_TYPE getMediaType() {
		return Extension.MEDIA_TYPE.html;
	}
	
	public String getTitle() {
		return "clinicalsummary.title";
	}
	
	public String getRequiredPrivilege() {
		return "View Patients,View Observations";
	}
	
	public Map<String, String> getLinks() {
		
		Map<String, String> map = new LinkedHashMap<String, String>();
		
		if (Context.hasPrivilege("Manage Clinical Summaries"))
			map.put("module/clinicalsummary/summary.list", "clinicalsummary.manage");
		if (Context.hasPrivilege("View Clinical Summary"))
			map.put("module/clinicalsummary/generateSummaries.form", "clinicalsummary.generateSummaries");
		if (Context.hasPrivilege("View Clinical Summary"))
			map.put("module/clinicalsummary/queue.list", "clinicalsummary.queue.title");
		if (Context.hasPrivilege("View Clinical Summary"))
			map.put("module/clinicalsummary/queueDirectory.list", "clinicalsummary.directory.title");
		
		return map;
	}
	
}
