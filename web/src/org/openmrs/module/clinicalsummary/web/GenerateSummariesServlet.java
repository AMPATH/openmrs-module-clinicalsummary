package org.openmrs.module.clinicalsummary.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.openmrs.Cohort;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.ClinicalSummary;
import org.openmrs.module.clinicalsummary.ClinicalSummaryService;
import org.openmrs.module.clinicalsummary.ClinicalSummaryUtil;
import org.openmrs.module.clinicalsummary.SummaryExportFunctions;
import org.openmrs.reporting.PatientFilter;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.web.bind.ServletRequestUtils;

/**
 * 
 * @author Ben Wolfe
 * @version 1.0
 */
public class GenerateSummariesServlet extends HttpServlet {

	public static final long serialVersionUID = 123423L;

	private Log log = LogFactory.getLog(this.getClass());
	
	private FopFactory fopFactory = FopFactory.newInstance();
	
	private TransformerFactory tFactory = TransformerFactory.newInstance();
	
	/**
	 * View the summary
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {
		
		// get the summary object
		ClinicalSummary summary = null;
		Integer clinicalSummaryId = ServletRequestUtils.getIntParameter(request, "clinicalSummaryId", 0);
		ClinicalSummaryService css = (ClinicalSummaryService)Context.getService(ClinicalSummaryService.class);
		if (clinicalSummaryId == 0)
			summary = css.getPreferredClinicalSummary();
		else
			summary = css.getClinicalSummary(clinicalSummaryId);
		
		// get the patient set
		Integer locationId = ServletRequestUtils.getIntParameter(request, "locationId", 0);
		Integer patientId = ServletRequestUtils.getIntParameter(request, "patientId", 0);
		String patientIdsString = ServletRequestUtils.getStringParameter(request, "patientIds", "");
		String patientIdentifiersString = ServletRequestUtils.getStringParameter(request, "patientIdentifiers", "");
		Integer cohortId = ServletRequestUtils.getIntParameter(request, "cohortId", -1);
		Integer cohortDefinitionId = ServletRequestUtils.getIntParameter(request, "cohortDefinitionId", -1);
		
		//PatientSet patientSet = new PatientSet();
		Cohort patientSet = new Cohort();
		PatientSetService pss = Context.getPatientSetService();
		if (locationId != 0)
			patientSet = pss.getPatientsHavingLocation(locationId);
		else if (patientId != 0)
			patientSet.addMember(patientId);
		else if (patientIdsString.length() > 0)
			patientSet = ClinicalSummaryUtil.parseCommaSeparatedPatientIds(patientIdsString);
		else if (patientIdentifiersString.length() > 0) {
			// find and add patients by identifier
 			PatientService ps = Context.getPatientService();
			for (StringTokenizer st = new StringTokenizer(patientIdentifiersString, ","); st.hasMoreTokens(); ) {
				String id = st.nextToken();
				Collection<Patient> patients = ps.getPatientsByIdentifier(id.trim(), true);
				for (Patient p : patients)
					patientSet.addMember(p.getPatientId());
			}
		}
		else if (cohortId != -1) {
			// hack to hydrate this
		    patientSet = Context.getCohortService().getCohort(cohortId);
//			Cohort cohort = Context.getCohortService().getCohort(cohortId);
//			if (cohort != null)
//				patientSet = cohort.toPatientSet();
		}
/*
		else if (cohortDefinitionId != -1) {
			PatientFilter cohortDefinition = (PatientFilter) Context.getReportService().getReportObject(cohortDefinitionId);
			if (cohortDefinition != null)
				patientSet = cohortDefinition.filter(Context.getPatientSetService().getAllPatients());
		}
*/		
		// Set up a VelocityContext in which to evaluate the template's default
		// values
		try {
			Velocity.init();
		}
		catch (Exception e) {
			log.error("Error initializing Velocity engine", e);
		}
		
		// set up the velocity context with our objects
		VelocityContext velocityContext = new VelocityContext();
		SummaryExportFunctions functions = new SummaryExportFunctions();
		functions.setPatientSet(patientSet);
		
		velocityContext.put("fn", functions);
		velocityContext.put("locale", Context.getLocale());
		velocityContext.put("patientSet", patientSet);
		
		Writer writer = new StringWriter();		
		try {
			Context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENT_COHORTS);
			Velocity.evaluate(velocityContext, writer, this.getClass().getName(), summary.getTemplate());
		}
		catch (ParseErrorException e) {
			throw new ServletException("Error parsing template: " + summary.getTemplate(), e);
		} catch (MethodInvocationException e) {
			throw new ServletException("Error parsing template: " + summary.getTemplate(), e);
		} catch (ResourceNotFoundException e) {
			throw new ServletException("Error parsing template: " + summary.getTemplate(), e);
		}
		finally {
			Context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENT_COHORTS);
			functions.clear();
			System.gc();
		}
		
	    try {
			//Setup a buffer to obtain the content length
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
            // configure foUserAgent as desired (this will let us set creator, etc.)
			
			//Setup FOP
			Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);

			//Setup Transformer
			Source xsltSrc = new StreamSource(new StringReader(summary.getXslt()));
			Transformer transformer = tFactory.newTransformer(xsltSrc);

			//Make sure the XSL transformation's result is piped through to FOP
			Result res = new SAXResult(fop.getDefaultHandler());

			//Setup input
			Source src = new StreamSource(new StringReader(writer.toString()));

			//Start the transformation and rendering process
			transformer.transform(src, res);

			//Prepare response
			String time = new SimpleDateFormat("yyyyMMdd_Hm").format(new Date());
			String filename = summary.getName().replace(" ", "_") + "-" + time + ".pdf";
			setFilename(response, filename);
			response.setContentType("application/pdf");
			response.setContentLength(out.size());
			
			//Send content to Browser
			response.getOutputStream().write(out.toByteArray());
			response.getOutputStream().flush();
		} catch (FOPException e) {
			throw new ServletException("Error generating report", e);
		} catch (TransformerConfigurationException e) {
			throw new ServletException("Error generating report", e);
		} catch (TransformerException e) {
			throw new ServletException("Error generating report", e);
		}
		
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		doGet(request, response);
	}

	private void setFilename(HttpServletResponse response, String filename) {
		response.setHeader("Content-Disposition", "attachment; filename=" + filename);
	}
}
