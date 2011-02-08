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
package org.openmrs.module.clinicalsummary.engine;

import java.io.File;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.clinicalsummary.SummaryConstants;
import org.openmrs.module.clinicalsummary.cache.DataProvider;
import org.openmrs.module.clinicalsummary.cache.SummaryDataSource;
import org.openmrs.util.OpenmrsUtil;

/**
 *
 */
public class GeneratorThread implements Runnable {
	
	private static final Log log = LogFactory.getLog(GeneratorThread.class);
	
	private enum Status {
		IDLE, RUNNING, FINISHED, ERROR
	}
	
	private static Integer currentId;
	
	private static Integer processed;
	
	private static Integer total;
	
	private static Status status;
	
	private final Cohort cohort;
	
	private final UserContext userContext;
	
	public GeneratorThread(Cohort cohort) {
		resetThread();
		
		total = cohort.size();
		
		this.cohort = cohort;
		this.userContext = Context.getUserContext();
	}
	
	/**
	 */
	public static void resetThread() {
		status = Status.IDLE;
		processed = 0;
		currentId = -1;
	}
	
	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			Context.openSession();
			
			// the thread is not authenticated, so we pass the user context here
			Context.setUserContext(userContext);
			
			status = Status.RUNNING;
			
			PatientService patientService = Context.getPatientService();
			File folder = OpenmrsUtil.getDirectoryInApplicationDataDirectory(SummaryConstants.GENERATED_PDF_LOCATION);
			
			// need to slice the cohort to minimize the memory footprint of
			// the encounter buffer here. if the logic service is fast enough,
			// we wouldn't need to do this actually
			
			int counter = 0;
			Cohort subCohort = new Cohort();
			for (Integer patientId : cohort.getMemberIds()) {
				subCohort.addMember(patientId);
				
				counter++;
				
				if (subCohort.size() >= SummaryConstants.MAX_COHORT_SIZE || counter >= cohort.getSize()) {
					
					DataProvider provider = new DataProvider(subCohort);
					SummaryDataSource summaryDataSource = new SummaryDataSource(provider);
					GeneratorEngine generatorEngine = new GeneratorEngine(summaryDataSource, folder);
					
					for (Integer processedId : subCohort.getMemberIds()) {
						generatorEngine.generateSummary(patientService.getPatient(currentId = processedId));
						processed++;
					}
					
					subCohort.setMemberIds(new TreeSet<Integer>());
					Context.clearSession();
				}
			}
		} catch (Exception e) {
			status = Status.ERROR;
			log.error("Exception encountered when trying to generate summaries ...", e);
		} finally {
			status = Status.FINISHED;
			Context.closeSession();
		}
	}
	
	/**
	 * @return
	 */
	public static boolean isRunning() {
		return status == Status.RUNNING;
	}
	
	/**
	 * @return
	 */
	public static Integer currentPatient() {
		return currentId;
	}
	
	/**
	 * @return
	 */
	public static Integer getProcessed() {
		return processed;
	}
	
	/**
	 * @return
	 */
	public static Integer getTotal() {
		return total;
	}
}
