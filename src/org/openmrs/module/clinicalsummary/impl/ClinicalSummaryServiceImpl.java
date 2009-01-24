package org.openmrs.module.clinicalsummary.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;

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
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.ClinicalSummary;
import org.openmrs.module.clinicalsummary.ClinicalSummaryConstants;
import org.openmrs.module.clinicalsummary.ClinicalSummaryQueueItem;
import org.openmrs.module.clinicalsummary.ClinicalSummaryService;
import org.openmrs.module.clinicalsummary.ClinicalSummaryUtil;
import org.openmrs.module.clinicalsummary.SummaryExportFunctions;
import org.openmrs.module.clinicalsummary.ClinicalSummaryQueueItem.CLINICAL_SUMMARY_QUEUE_STATUS;
import org.openmrs.module.clinicalsummary.ReminderLog;
import org.openmrs.module.clinicalsummary.db.ClinicalSummaryDAO;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;

public class ClinicalSummaryServiceImpl implements ClinicalSummaryService {
	
	protected final Log log = LogFactory.getLog(getClass());
	private ClinicalSummaryDAO summaryDAO; 
	private FopFactory fopFactory = FopFactory.newInstance();
	private TransformerFactory tFactory = TransformerFactory.newInstance();
	private ReminderLog reminder = new ReminderLog();
    
	/**
	 * @return the summaryDAO
	 */
	public ClinicalSummaryDAO getSummaryDAO() {
		return summaryDAO;
	}

	/**
	 * @param summaryDAO the summaryDAO to set
	 */
	public void setSummaryDAO(ClinicalSummaryDAO summaryDAO) {
		this.summaryDAO = summaryDAO;
	}

	/**
	 * @see org.openmrs.module.clinicalsummary.ClinicalSummaryService#createClinicalSummary(org.openmrs.module.clinicalsummary.ClinicalSummary)
	 */
	public void createClinicalSummary(ClinicalSummary summary) {
		
		User u = Context.getAuthenticatedUser();
		
		summary.setCreator(u);
		summary.setDateCreated(new Date());
		summary.setChangedBy(u);
		summary.setDateChanged(new Date());
		
		summaryDAO.createClinicalSummary(summary);
	}

	/**
	 * @see org.openmrs.module.clinicalsummary.ClinicalSummaryService#getClinicalSummary(java.lang.Integer)
	 */
	public ClinicalSummary getClinicalSummary(Integer id) {
		return summaryDAO.getClinicalSummary(id);
	}

	/**
	 * @see org.openmrs.module.clinicalsummary.ClinicalSummaryService#getClinicalSummaries()
	 */
	public List<ClinicalSummary> getClinicalSummaries() {
		return summaryDAO.getClinicalSummaries();
	}

	/**
	 * @see org.openmrs.module.clinicalsummary.ClinicalSummaryService#getPreferredClinicalSummary()
	 */
	public ClinicalSummary getPreferredClinicalSummary() {
		
		List<ClinicalSummary> summaries = getClinicalSummaries();
		for (ClinicalSummary summary : summaries) {
			if (summary.getPreferred() == true)
				return summary;
		}
		
		if (summaries != null && summaries.size() > 0)
			return summaries.get(0);
		
		return null;
	}

	/**
	 * @see org.openmrs.module.clinicalsummary.ClinicalSummaryService#updateClinicalSummary(org.openmrs.module.clinicalsummary.ClinicalSummary)
	 */
	public void updateClinicalSummary(ClinicalSummary summary) {
		
		if (summary.getClinicalSummaryId() == null) {
			createClinicalSummary(summary);
			return;
		}
		
		summary.setChangedBy(Context.getAuthenticatedUser());
		summary.setDateChanged(new Date());
		
		summaryDAO.updateClinicalSummary(summary);
	}

	/**
	 * @see org.openmrs.module.clinicalsummary.ClinicalSummaryService#getObservationsForEncounters(java.util.Collection, org.openmrs.Concept)
	 */
	public Map<Integer, List<Object>> getObservationsForEncounters(Collection<Encounter> encounters, Concept c) {
		return summaryDAO.getObservationsForEncounters(encounters, c);
	}


	/**
	 * @see org.openmrs.module.clinicalsummary.ClinicalSummaryService#createQueueItem(org.openmrs.module.clinicalsummary.ClinicalSummaryQueueItem)
	 */
	public Integer createQueueItem(ClinicalSummaryQueueItem item) {
		if (item.getDateCreated() == null)
			item.setDateCreated(new Date());
		log.debug("Created Clinical Summary Queue Item. " + item.getClinicalSummaryQueueId());
		Integer id = summaryDAO.createQueueItem(item);
		return id;
	}
	

	/**
	 * @see org.openmrs.module.clinicalsummary.ClinicalSummaryService#deleteQueueItem(org.openmrs.module.clinicalsummary.ClinicalSummaryQueueItem)
	 */
	public void deleteQueueItem(ClinicalSummaryQueueItem item) {
		File delFile = new File(ClinicalSummaryUtil.getOutDir(item, ClinicalSummaryUtil.DIRECTORY.GENERATED).toString(), item.getFileName());
		if (delFile.exists())
			delFile.delete();
		summaryDAO.deleteQueueItem(item);
	}
	

	/**
	 * @see org.openmrs.module.clinicalsummary.ClinicalSummaryService#getQueueItems(java.util.Date, java.util.List)
	 */
	public List<ClinicalSummaryQueueItem> getQueueItems(Date beforeOrEqualToDate, List<CLINICAL_SUMMARY_QUEUE_STATUS> status) {
		List<String> stringStatus = null;
		
		if (status != null) {
			stringStatus = new Vector<String>();
			for (CLINICAL_SUMMARY_QUEUE_STATUS stat : status) {
				stringStatus.add(stat.name());
			}
		}
		
		return summaryDAO.getQueueItems(beforeOrEqualToDate, stringStatus);
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.ClinicalSummaryService#getQueueItems(java.util.Date, java.util.List, ClinicalSummaryUtil.ORDER)
	 */
	public List<ClinicalSummaryQueueItem> getQueueItems(Date beginDate, Date endDate, List<String> locations, List<String> statuses, ClinicalSummaryUtil.ORDER order, int offset, int limit) {
        return summaryDAO.getQueueItems(beginDate, endDate, locations, statuses, order, offset, limit);
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.ClinicalSummaryService#getQueueItem(Integer)
	 */
	public ClinicalSummaryQueueItem getQueueItem(Integer queueId) {
        return summaryDAO.getQueueItem(queueId);
	}

	/**
	 * @see org.openmrs.module.clinicalsummary.ClinicalSummaryService#updateQueueItem(org.openmrs.module.clinicalsummary.ClinicalSummaryQueueItem)
	 */
	public void updateQueueItem(ClinicalSummaryQueueItem item) {
		summaryDAO.updateQueueItem(item);
	}

	
	/**
	 * @see org.openmrs.module.clinicalsummary.ClinicalSummaryService#getQueuePatientIds(java.util.List, org.openmrs.module.clinicalsummary.ClinicalSummaryQueueItem.CLINICAL_SUMMARY_QUEUE_STATUS)
	 */
	@SuppressWarnings("unchecked")
	public List<Integer> getQueuePatientIds(List<Integer> queueIds, CLINICAL_SUMMARY_QUEUE_STATUS statusToSet) {
		if (queueIds == null || queueIds.size() < 1) 
			throw new APIException("queueIds must not be empty");
		
		return (List<Integer>)summaryDAO.getQueuePatientIds(queueIds, statusToSet);
	}

	
	/**
	 * @see org.openmrs.module.clinicalsummary.ClinicalSummaryService#setQueueStatus(java.util.List, org.openmrs.module.clinicalsummary.ClinicalSummaryQueueItem.CLINICAL_SUMMARY_QUEUE_STATUS)
	 */
	public void setQueueStatus(List<Integer> queueIds, CLINICAL_SUMMARY_QUEUE_STATUS statusToSet) {
		if (queueIds == null || queueIds.size() < 1) 
			throw new APIException("queueIds must not be empty");
		
		summaryDAO.setQueueStatus(queueIds, statusToSet);
	}
    
    /**
	 * Generates a clinical summary .pdf to a "generated" directory of the file system.  
     * 
     * @param queueItem
     * @param interactive
     * @return
     */
    public Boolean generatePatientSummary(ClinicalSummaryQueueItem queueItem, boolean interactive) {
        //TODO: logReminder is set to 'true' only for testing.  This should be set to 'false'.
        return generatePatientSummary(queueItem, interactive, false);
    }
	
	/**
	 * Generates a clinical summary .pdf to a "generated" directory of the file system.  
	 * TODO: Remove code related to printing, since this used to send the clinical summaries to the printer.
     * 
     * @param queueItem 
     * @param interactive
     * @param logReminder If true, will log the clinical summary patient reminder to the reminder log file.
     * @return
	 */
	public Boolean generatePatientSummary(ClinicalSummaryQueueItem queueItem, boolean interactive, boolean logReminder) {
		//PatientSet patientSet = new PatientSet();
		Cohort patientSet = new Cohort();
		patientSet.addMember(queueItem.getPatient().getPatientId());
		ClinicalSummary summary = getPreferredClinicalSummary();
		
		// TODO centralize this a little more.  The generateSummariesServlet also has 90% similar code
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
        Writer reminderMsg = new StringWriter();
        try {
			Velocity.evaluate(velocityContext, writer, this.getClass().getName(), summary.getTemplate());
            Velocity.evaluate(velocityContext, reminderMsg, "reminderlog", "$fn.getCD4CountReminder()");
		}
		catch (IOException e) {
			throw new APIException("Error parsing template: " + summary.getTemplate(), e);
		} catch (ParseErrorException e) {
			throw new APIException("Error parsing template: " + summary.getTemplate(), e);
		} catch (MethodInvocationException e) {
			throw new APIException("Error parsing template: " + summary.getTemplate(), e);
		} catch (ResourceNotFoundException e) {
			throw new APIException("Error parsing template: " + summary.getTemplate(), e);
		}
		finally {
			functions.clear();
			System.gc();
		}
		
	    try {
	    	// Try to connect to mapped network drive, if necessary.
	    	/*
			if (!connectMappedDrive()) {
				log.error("ClinicalSummaryServiceImpl.java(358): Mapped Network Drive cannot be connected.");
				return false;
			}
			*/	    	
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
			
			String xml = writer.toString();
			
			if (log.isDebugEnabled())
				log.debug("xml: \n" + xml);
			
			//Setup input
			Source src = new StreamSource(new StringReader(xml));

			//Start the transformation and rendering process
			transformer.transform(src, res);
			
			byte[] outBytes = out.toByteArray();
			
			if (log.isDebugEnabled())
				log.debug("Out byte array: " + outBytes.length);
			
			// Get the output file.
			File file = ClinicalSummaryUtil.getOutFile(queueItem, ClinicalSummaryUtil.DIRECTORY.GENERATED, null);

			// Delete previously generated file, if it exists.
			if (queueItem.hasFileName()) {
				File delFile = new File(file.getParent(), queueItem.getFileName());
				if (delFile.exists())
					delFile.delete();
			}

			// Set the queueItem fileName and (if not WAITING_ON_LABS) set status to GENERATED.
			queueItem.setFileName(file.getName());
			if ((null == queueItem.getStatus()) || (queueItem.getStatus() != ClinicalSummaryQueueItem.CLINICAL_SUMMARY_QUEUE_STATUS.WAITING_ON_LABS))
				queueItem.setStatus(ClinicalSummaryQueueItem.CLINICAL_SUMMARY_QUEUE_STATUS.GENERATED);
			this.getSummaryDAO().updateQueueItem(queueItem);

            if (logReminder) {
                reminder.logReminder(queueItem, reminderMsg.toString());
            }
			
			try {
				FileOutputStream outStream = new FileOutputStream(file);
				outStream.write(outBytes);
				outStream.flush();
				outStream.close();
			}
			catch (IOException io) {
				log.error("Unable to write output file: " + io.getMessage());
				return false;
			}
			
			return true;
						
		} catch (FOPException e) {
			throw new APIException("Error generating report", e);
		} catch (TransformerConfigurationException e) {
			throw new APIException("Error generating report", e);
		} catch (TransformerException e) {
			throw new APIException("Error generating report", e);
		} finally {
            functions.clear();
            System.gc();
        }
		
	}
	
	/**
	 *  Moves clinical summary .pdf's from the "generated" directory in the file system to the "toPrint" 
	 *  directory of the file system.  From here, we are using Batch & Print Pro to monitor the "toPrint"
	 *  directory and print when a .pdf is placed there.
	 */
	public Boolean printClinicalSummaryQueueItems(List<Integer> queueIds) {
		/*
		if (!connectMappedDrive()) {
			log.error("ClinicalSummaryServiceImpl.java(358): Mapped Network Drive cannot be connected.");
			return false;
		}
		*/
		for (Integer id : queueIds) {
			try {
				ClinicalSummaryQueueItem queueItem = this.getQueueItem(id);
				if (queueItem.getStatus() == ClinicalSummaryQueueItem.CLINICAL_SUMMARY_QUEUE_STATUS.WAITING_ON_LABS) {
					boolean wait = this.generatePatientSummary(queueItem, false);
				}
				File currentFile = new File(ClinicalSummaryUtil.getOutDir(queueItem, ClinicalSummaryUtil.DIRECTORY.GENERATED).toString(), queueItem.getFileName().toString());
				File destFile = new File(ClinicalSummaryUtil.getOutDir(queueItem, ClinicalSummaryUtil.DIRECTORY.TO_PRINT).toString(), queueItem.getFileName().toString());
				if (!currentFile.exists())
					currentFile.mkdirs();
				InputStream inputStream = new FileInputStream(currentFile);
				OutputStream outputStream = new FileOutputStream(destFile);
				OpenmrsUtil.copyFile(inputStream, outputStream);
				inputStream.close();
				currentFile.delete();
				// Remove queueItem from database.
				summaryDAO.deleteQueueItem(queueItem);
			}
			catch (Exception e) {
				log.error(e.getMessage() + " " + e.getLocalizedMessage());
			}
		}
		return true;
	}
	
	/**
	 * Test and Connect (if necessary) to a Windows XP mapped network drive.
	 * Apache Tomcat must be run under an account with network privileges.
	 * 
	 * See Windows XP command 'net use /?' for more information.
	 * net use deviceName: \\\\Server\\ShareName 'password' /USER:'domain\\userName'
	 * 
	 * Example Scenario:
	 * 
	 * The OpenMRS Webapp is on the Server.  The Clinical Summaries will be generated and 
	 * printed on a networked computer named "PRINTCOMP 1".
	 *   
	 * The Global Property for 'clinicalsummary.queueItemGenerateDir' 
	 * is '\\PRINTCOMP 1\openmrs\clinicalsummary\generated'
	 * 
	 * The following lines exist in the OPENMRS-runtime.properties file:
	 * clinicalsummary.network_device_name=Z
	 * clinicalsummary.network_share_name=C$
	 * clinicalsummary.network_username=Network User
	 * clinicalsummary.network_password=Top Secret	
	 * 
	 * Therefore, the mapped network drive will be tested and reconnected if necessary 
	 * using the Windows XP "net use" command:
	 * net use Z: \\PRINTCOMP 1\C$ 'Top Secret' /USER:'PRINTCOMP 1/Network User'
	 * 
	 * @return
	 */
	public Boolean connectMappedDrive( ) {
		// Get global property for network drive
		String directory = Context.getAdministrationService().getGlobalProperty("clinicalsummary.queueItemGenerateDir");
		try {
			// Test if the directory exists or can be created.  If so, we are done here.
			File dir = new File(directory);
			dir.createNewFile();
			return true;
		}
		catch (IOException ioe) {
			// File does not exist.  We need to connect the network drive.
			log.debug("Need to map drive for clinicalsummary.queueItemGenerateDir since it does not exist: " + directory, ioe);
		}
		// Get runtime properties for network drive:
		String deviceName = Context.getRuntimeProperties().getProperty(ClinicalSummaryConstants.CLINICALSUMMARY_RP_NETWORK_DEVICE_NAME);		
		String shareName = Context.getRuntimeProperties().getProperty(ClinicalSummaryConstants.CLINICALSUMMARY_RP_NETWORK_SHARE_NAME);		
		String userName = Context.getRuntimeProperties().getProperty(ClinicalSummaryConstants.CLINICALSUMMARY_RP_NETWORK_USERNAME);
		String password = Context.getRuntimeProperties().getProperty(ClinicalSummaryConstants.CLINICALSUMMARY_RP_NETWORK_PASSWORD);

		/* net use deviceName: \\\\Server\\ShareName 'password' /USER:'domain\\userName' */
		
		if (!directory.startsWith("\\\\")) {
			log.debug("ClinicalSummaryServiceImpl.java(396): This is not a network path: " + directory);
			return true;
		}
		deviceName = deviceName.replaceAll(":", "");
		shareName = shareName.replaceAll(":","");
		directory = directory.replaceAll("\\\\","/").replaceAll("//","/");
		String[] folders = directory.split("/");
		int i = 0;
		for (; i<folders.length; i++) {
			if (folders[i].length() > 1) break;
		}
		String domain = folders[i];
		String path = "\\\\" + folders[i] + "\\" + shareName;

		String disconnect = "cmd /C \"net use " + deviceName + " /DELETE \"";
		//String command = "net use " + deviceName + ": " + path + " \'" + password + "\' /USER:\'" + domain + "\\" + userName + "\'";
		String command = "cmd /C net use \"" + deviceName + ": " + path + " " + password + " /USER:" + domain + "\\" + userName + "\"";

		// Do NOT uncomment this line.  Password will show up in the log file.
		//log.debug("ClinicalSummaryServiceImpl.java(456) " + command);
		
		if (OpenmrsConstants.OPERATING_SYSTEM.toLowerCase().contains("windows")) {
			try {
				Runtime.getRuntime().exec(disconnect);
				//log.debug("Exec: " + disconnect);
				Runtime.getRuntime().exec(command);
				//log.debug("Exec: " + command);
			}
			catch (IOException ioe) {
				log.error("Error connecting to network drive: " + command, ioe);
				return false;
			}
		}

		return true;
	}
	
}
