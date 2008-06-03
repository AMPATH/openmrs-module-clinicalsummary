package org.openmrs.module.clinicalsummary.web.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.ClinicalSummaryQueueItem;
import org.openmrs.module.clinicalsummary.ClinicalSummaryService;
import org.openmrs.module.clinicalsummary.ClinicalSummaryQueueItem.CLINICAL_SUMMARY_QUEUE_STATUS;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.WebConstants;

/**
 * Hack
 */
public class ProcessPrintingQueueServlet extends HttpServlet {

	private static final long serialVersionUID = -3545085468235057302L;

	private Log log = LogFactory.getLog(this.getClass());
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// check for authenticated users
		if (!Context.isAuthenticated()) {
			request.getSession().setAttribute(WebConstants.OPENMRS_LOGIN_REDIRECT_HTTPSESSION_ATTR,
				request.getContextPath() + "/moduleServlet/clinicalsummary/processPrintingQueue");
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.session.expired");
			response.sendRedirect(request.getContextPath() + "/logout");
			return;
		}
		
		ClinicalSummaryService css = (ClinicalSummaryService)Context.getService(ClinicalSummaryService.class);
		
		List<CLINICAL_SUMMARY_QUEUE_STATUS> statuses = new Vector<CLINICAL_SUMMARY_QUEUE_STATUS>();
		//statuses.add(CLINICAL_SUMMARY_QUEUE_STATUS.WAITING_ON_LABS);
		statuses.add(CLINICAL_SUMMARY_QUEUE_STATUS.PENDING);
		//statuses.add(CLINICAL_SUMMARY_QUEUE_STATUS.ERROR);

		//statuses.add(CLINICAL_SUMMARY_QUEUE_STATUS.PRINTED);
		statuses.add(CLINICAL_SUMMARY_QUEUE_STATUS.GENERATED);
		
		int chunkSize = 25;
		int x = 0;
		List<ClinicalSummaryQueueItem> queueItems = css.getQueueItems(null, statuses);
		
		//PatientSet patientSet = new PatientSet();
		Cohort patientSet = new Cohort();
		List<Integer> queueIdList = new ArrayList<Integer>();
		Integer size = queueItems.size();
		while (x < size) {
			ClinicalSummaryQueueItem queueItem = queueItems.get(x++);
			patientSet.addMember(queueItem.getPatient().getPatientId());
			queueIdList.add(queueItem.getClinicalSummaryQueueId());
			
			if (x % chunkSize == 0 || x >= size) {
//				if (css.generateSummariesForPatients(patientSet, false))
				if (css.printClinicalSummaryQueueItems(queueIdList))
					css.setQueueStatus(queueIdList, ClinicalSummaryQueueItem.CLINICAL_SUMMARY_QUEUE_STATUS.PRINTED);
				else {
					log.error("Unable to print clinical summary: " + OpenmrsUtil.join(queueIdList, ","));
					css.setQueueStatus(queueIdList, ClinicalSummaryQueueItem.CLINICAL_SUMMARY_QUEUE_STATUS.ERROR);
				}
				//patientSet = new PatientSet();
				patientSet = new Cohort();
				queueIdList.clear();
				System.gc();
				System.gc();
			}
		}
		
	}
	
}
