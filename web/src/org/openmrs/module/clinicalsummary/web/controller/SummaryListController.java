package org.openmrs.module.clinicalsummary.web.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.ClinicalSummaryService;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class SummaryListController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

	/**
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	protected Object formBackingObject(HttpServletRequest request) throws Exception {
		
		ClinicalSummaryService css = (ClinicalSummaryService)Context.getService(ClinicalSummaryService.class);
		
		return css.getClinicalSummaries();
	}
        
}