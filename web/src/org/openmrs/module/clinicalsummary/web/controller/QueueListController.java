package org.openmrs.module.clinicalsummary.web.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.ClinicalSummaryQueueItem;
import org.openmrs.module.clinicalsummary.ClinicalSummaryService;
import org.openmrs.module.clinicalsummary.ClinicalSummaryUtil;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.WebConstants;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class QueueListController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
   
	/**
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	protected Object formBackingObject(HttpServletRequest request) throws Exception {

		ClinicalSummaryService css = (ClinicalSummaryService)Context.getService(ClinicalSummaryService.class);

		MessageSourceAccessor msa = getMessageSourceAccessor();
		// Date
		//Date start, end;
		String datePattern = ServletRequestUtils.getStringParameter(request, "datePattern");
		String startDate = ServletRequestUtils.getStringParameter(request, "startDate", "01/01/1900");
		String endDate = ServletRequestUtils.getStringParameter(request, "endDate", "01/01/3000");
		if (null == startDate || startDate.length() < 2) {
			startDate = new String("01/01/1900");
		}
		if (null == endDate || endDate.length() < 2) {
			endDate = new String("01/01/3000");
		}
		// Location
		List<String> locations = new ArrayList<String>();
		locations.add("'" + ServletRequestUtils.getStringParameter(request, "locationFilter") + "'");
		// Status
		List<String> statuses = new ArrayList<String>(7);
		statuses.add("'" + ServletRequestUtils.getStringParameter(request, "statusFilter") + "'");
		String sort = ServletRequestUtils.getStringParameter(request, "sortColumn");
		// Offset, Limit
		int offset = ServletRequestUtils.getIntParameter(request, "offset", 0);
		int limit = ServletRequestUtils.getIntParameter(request, "limit", 50);

		try {
			SimpleDateFormat format = new SimpleDateFormat(datePattern.replaceAll("m", "M"));
			Date start = format.parse(startDate);
			Date end = format.parse(endDate);
			String orderByIdentifier = msa.getMessage("Patient.identifier");
			String orderByDate = msa.getMessage("Encounter.datetime");
			if (orderByIdentifier.equals(sort)) {
				return css.getQueueItems(start, end, locations, statuses, ClinicalSummaryUtil.ORDER.IDENTIFIER, offset, limit);							
			}
			if (orderByDate.equals(sort)) {
				return css.getQueueItems(start, end, locations, statuses, ClinicalSummaryUtil.ORDER.ENCOUNTER_DATE, offset, limit);				
			}
			return css.getQueueItems(start, end, locations, statuses, ClinicalSummaryUtil.ORDER.IDENTIFIER, offset, limit);							
		}
		catch (ParseException pe) {
			log.debug("ClinicalSummaryModule - QueueListController.formBackingObject: Could not parse date parameters.  startDate:" + startDate + " endDate: " + endDate + " Error Message:" + pe.getMessage());
		}
		catch (Exception e) {
			log.debug("ClinicalSummaryModule - QueueListController.formBackingObject: Too many errors acquiring parameters. Bailing out. Message: " + e.getMessage());
		}		
		// Default ORDER BY Patient Identifier
		return css.getQueueItems(null, null, locations, statuses, ClinicalSummaryUtil.ORDER.IDENTIFIER, offset, limit);							
	}

	
	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object object, BindException errors) throws Exception {
		
		ClinicalSummaryService css = (ClinicalSummaryService)Context.getService(ClinicalSummaryService.class);
    	HttpSession httpSession = request.getSession();
		String[] queueIds = ServletRequestUtils.getStringParameters(request, "queueId");
				
		List<Integer> queueIdList = new Vector<Integer>();
		for (String queueId : queueIds) {
			log.debug("queue id: " + queueId);
			queueIdList.add(Integer.valueOf(queueId));
		}
		
		if (queueIds == null)
			throw new ServletException("You must select some queue items"); // TODO add to bindexceptions instead
		
		// TODO add listener here to know when its done printing

		MessageSourceAccessor msa = getMessageSourceAccessor();
		try {
			String removeSummaries = msa.getMessage("clinicalsummary.queue.removeSummaries");
			String generateSummaries = msa.getMessage("clinicalsummary.queue.generateSummaries");
			String printSummaries = msa.getMessage("clinicalsummary.queue.printSummaries");
		
			if (removeSummaries.equals(request.getParameter("action"))) {
				for (Integer id : queueIdList)
					css.deleteQueueItem(css.getQueueItem(id));
		    	httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR , "clinicalsummary.queue.removed");				
				//return showForm(request, response, errors);
				//return new ModelAndView(new RedirectView(getSuccessViewAndParameters(getSuccessView(),filterMap)));
				return super.showNewForm(request, response);
			}
			else if (generateSummaries.equals(request.getParameter("action"))) {
				for (Integer id : queueIdList)
					css.generatePatientSummary(css.getQueueItem(id), false);
				
				if (!Context.isAuthenticated()) {
					// Check if still authenticated for when generating summaries for a long time at once.
					return new ModelAndView(new RedirectView(request.getContextPath() + "/logout"));
				}
	
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR , "clinicalsummary.queue.generated");									
				// POOR: The following method does not need URL parameters but does NOT return a *new* formBackingObject
				//return showForm(request, response, errors);
				// FAIR: The following method returns a new formBackingObject but uses URL parameters 
				//return new ModelAndView(new RedirectView(getSuccessViewAndParameters(getSuccessView(),filterMap)));
				// BETTER: The following method returns a new formBackingObject and does not need URL parameters
		    	// TODO: Fix authentication exception after a really long time of generating summaries.	
		    	//Context.
				return super.showNewForm(request, response);
			}
			else if (printSummaries.equals(request.getParameter("action"))) {
				if (css.printClinicalSummaryQueueItems(queueIdList))
					css.setQueueStatus(queueIdList, ClinicalSummaryQueueItem.CLINICAL_SUMMARY_QUEUE_STATUS.PRINTED);
				else {
					log.error("Unable to print clinical summary: " + OpenmrsUtil.join(queueIdList, ","));
					css.setQueueStatus(queueIdList, ClinicalSummaryQueueItem.CLINICAL_SUMMARY_QUEUE_STATUS.ERROR);
				}
				if (!Context.isAuthenticated()) {
					// Check if still authenticated for when printing summaries for a long time at once.
					return new ModelAndView(new RedirectView(request.getContextPath() + "/logout"));
				}
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR , "clinicalsummary.queue.printed");				
				return super.showNewForm(request, response);
			}
			else {
				return super.showNewForm(request, response);
			}
		} 
		catch (NoSuchMessageException nsme){
			log.error(nsme);
	    	httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR , "clinicalsummary.queue.error");							
			return showForm(request, response, errors);
		}
			
	}
		
	@Override
	protected Map referenceData(HttpServletRequest arg0) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		ArrayList<String> queuePageList = new ArrayList<String>(6);
		queuePageList.add("20");
		queuePageList.add("50");
		queuePageList.add("100");
		queuePageList.add("200");
		queuePageList.add("500");
		queuePageList.add("1000");
		
		map.put("queuePage", queuePageList);
		map.put("status", ClinicalSummaryQueueItem.CLINICAL_SUMMARY_QUEUE_STATUS.values() );
		
		return map;
	}	
}
