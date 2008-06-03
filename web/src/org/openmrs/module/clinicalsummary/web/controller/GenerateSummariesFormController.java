package org.openmrs.module.clinicalsummary.web.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.ClinicalSummaryService;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class GenerateSummariesFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

	/**
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	protected Object formBackingObject(HttpServletRequest request) throws Exception {
		
		ClinicalSummaryService css = (ClinicalSummaryService)Context.getService(ClinicalSummaryService.class);
		
		return css.getClinicalSummaries();
	}

	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest)
	 */
	protected Map referenceData(HttpServletRequest request) throws Exception {
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		EncounterService es = Context.getEncounterService();
		ClinicalSummaryService css = (ClinicalSummaryService)Context.getService(ClinicalSummaryService.class);
		
		List<Location> locations = es.getLocations();
		
		map.put("locations", locations);
		map.put("preferredSummary", css.getPreferredClinicalSummary()); 
		
		return map;
	}
	
}