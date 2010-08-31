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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.tasks.AbstractTask;

/**
 *
 */
public class FailedSummaryGeneratorTask extends AbstractTask {
	
	private static final Log log = LogFactory.getLog(FailedSummaryGeneratorTask.class);
	
	private FailedSummaryGeneratorProcessor processor = null;
	
	public FailedSummaryGeneratorTask() {
		if (processor == null)
			processor = new FailedSummaryGeneratorProcessor();
	}
	
	/**
	 * @see org.openmrs.scheduler.tasks.AbstractTask#execute()
	 */
	@Override
	public void execute() {
		Context.openSession();
		
		if (log.isDebugEnabled())
			log.debug("Running Failed Summary Generator Task ...");
		
		try {
			if (!Context.isAuthenticated())
				authenticate();
			processor.processSummary();
		}
		catch (Exception e) {
			// hook a report showing that and error occured when generating the summary
			log.info("Exception thrown while running generator ...", e);
		}
		finally {
			Context.closeSession();
		}
	}
	
	@Override
	public void shutdown() {
		processor = null;
		super.shutdown();
		if (log.isDebugEnabled())
			log.debug("Shutting down Summary Generator Task ...");
	}
}
