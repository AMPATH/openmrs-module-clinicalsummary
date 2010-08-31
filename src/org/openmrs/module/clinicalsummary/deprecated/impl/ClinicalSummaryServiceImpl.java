package org.openmrs.module.clinicalsummary.deprecated.impl;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.deprecated.ClinicalSummary;
import org.openmrs.module.clinicalsummary.deprecated.ClinicalSummaryConstants;
import org.openmrs.module.clinicalsummary.deprecated.ClinicalSummaryService;
import org.openmrs.module.clinicalsummary.deprecated.db.ClinicalSummaryDAO;
import org.openmrs.util.OpenmrsConstants;

public class ClinicalSummaryServiceImpl implements ClinicalSummaryService {
	
	protected final Log log = LogFactory.getLog(getClass());
	private ClinicalSummaryDAO summaryDAO;
    
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
	 * @see org.openmrs.module.clinicalsummary.deprecated.ClinicalSummaryService#createClinicalSummary(org.openmrs.module.clinicalsummary.deprecated.ClinicalSummary)
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
	 * @see org.openmrs.module.clinicalsummary.deprecated.ClinicalSummaryService#getClinicalSummary(java.lang.Integer)
	 */
	public ClinicalSummary getClinicalSummary(Integer id) {
		return summaryDAO.getClinicalSummary(id);
	}

	/**
	 * @see org.openmrs.module.clinicalsummary.deprecated.ClinicalSummaryService#getClinicalSummaries()
	 */
	public List<ClinicalSummary> getClinicalSummaries() {
		return summaryDAO.getClinicalSummaries();
	}

	/**
	 * @see org.openmrs.module.clinicalsummary.deprecated.ClinicalSummaryService#getPreferredClinicalSummary()
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
	 * @see org.openmrs.module.clinicalsummary.deprecated.ClinicalSummaryService#updateClinicalSummary(org.openmrs.module.clinicalsummary.deprecated.ClinicalSummary)
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
	 * @see org.openmrs.module.clinicalsummary.deprecated.ClinicalSummaryService#getObservationsForEncounters(java.util.Collection, org.openmrs.Concept)
	 */
	public Map<Integer, List<Object>> getObservationsForEncounters(Collection<Encounter> encounters, Concept c) {
		return summaryDAO.getObservationsForEncounters(encounters, c);
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
