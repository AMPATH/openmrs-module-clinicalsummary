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
package org.openmrs.module.clinicalsummary.task;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.SummaryError;
import org.openmrs.module.clinicalsummary.SummaryService;
import org.openmrs.module.clinicalsummary.engine.GeneratorThread;

/**
 *
 */
public class FailedSummaryGeneratorProcessor {
	
	private static final Log log = LogFactory.getLog(FailedSummaryGeneratorProcessor.class);
	
	public FailedSummaryGeneratorProcessor() {
	}
	
	public void processSummary() {
		SummaryService summaryService = Context.getService(SummaryService.class);
		List<SummaryError> errors = summaryService.getAllErrors();
		
		int counter = 0;
		
		ExecutorService executorService = Executors.newFixedThreadPool(1);
		for (SummaryError error : errors) {
			Cohort cohort = new Cohort();
			Patient patient = error.getPatient();
			cohort.addMember(patient.getPatientId());
			
			summaryService.deleteError(error);
			
			GeneratorThread generatorThread = new GeneratorThread(cohort);
			executorService.execute(generatorThread);
			
			if (log.isDebugEnabled())
				log.debug("Generating " + cohort.size() + " summaries from failed list ...");
			
			counter++;
			if (counter % 100 == 0)
				Context.clearSession();
		}
	}
}
