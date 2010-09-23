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

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.SummaryService;
import org.openmrs.module.clinicalsummary.engine.GeneratorThread;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.TaskDefinition;

/**
 *
 */
public class SummaryGeneratorProcessor {
	
	private static final Log log = LogFactory.getLog(SummaryGeneratorTask.class);
	
	/*
	 * This need to be kept in sync with the task property
	 */

	// the property name for all known location grouping
	private static final String LOCATION_GROUP_LIST = "Location Group List";
	
	// the property for last processed location group
	private static final String PROCESSOR_COUNTER = "clinicalsummary.batch.counter";
	
	// the property for last processed location group
	private static final String PROCESSOR_INITIALIZED = "clinicalsummary.batch.initialized";
	
	// task name for the clinical summary batch generation
	private static final String PROCESSOR_NAME = "Batch Printing Summary";
	
	// separator for the initialization
	private static final String WHITESPACE = " ";
	
	// cluster separator
	private static final String CLUSTER_SEPARATOR = ",";
	
	/*
	 * End of scheduler task property
	 */

	public void processSummary() {
		
		SummaryService summaryService = Context.getService(SummaryService.class);
		AdministrationService administrationService = Context.getAdministrationService();
		SchedulerService schedulerService = Context.getSchedulerService();
		
		TaskDefinition taskDefinition = schedulerService.getTaskByName(PROCESSOR_NAME);
		
		String clusterNames = taskDefinition.getProperty(LOCATION_GROUP_LIST);
		if (clusterNames != null) {
			
			String[] clusterName = StringUtils.split(clusterNames, CLUSTER_SEPARATOR);
			
			GlobalProperty globalProperty = administrationService.getGlobalPropertyObject(PROCESSOR_COUNTER);
			Integer clusterOffset = NumberUtils.toInt(globalProperty.getPropertyValue(), -1);
			
			// only do processing if the offset within the array range
			if (clusterOffset >= 0 && clusterOffset < ArrayUtils.getLength(clusterName)) {
				
				// TODO i don't have any better idea to check if each cluster is initialized or not
				// use this as temporary solution
				GlobalProperty initProperty = administrationService.getGlobalPropertyObject(PROCESSOR_INITIALIZED);
				String initString = initProperty.getPropertyValue();
				
				boolean initialized = false;
				if (ArrayUtils.getLength(StringUtils.split(initString)) >= ArrayUtils.getLength(clusterName))
					initialized = true;
				
				String currentCluster = clusterName[clusterOffset];
				String locations = taskDefinition.getProperty(currentCluster);
				
				ExecutorService executorService = Executors.newFixedThreadPool(1);
				
				String[] locationIds = StringUtils.split(locations);
				for (int i = 0; i < ArrayUtils.getLength(locationIds); i++) {
					
					// default return to -1 because no such location with id -1
					Integer locationId = NumberUtils.toInt(locationIds[i], -1);
					
					log.info("Processing location with id: " + locationId);
					
					Location location = Context.getLocationService().getLocation(locationId);
					
					if (!initialized) {
						Cohort cohort = summaryService.getCohortByLocation(location);
						GeneratorThread generatorThread = new GeneratorThread(cohort);
						executorService.execute(generatorThread);
					} else {
						// index date for the observations checking :)
						Calendar calendar = Calendar.getInstance();
						calendar.add(Calendar.DATE, - (clusterName.length + 1));
						Date date = calendar.getTime();
						
						Cohort cohort = summaryService.getCohortByLocation(location, date, new Date());
						GeneratorThread generatorThread = new GeneratorThread(cohort);
						executorService.execute(generatorThread);
					}
				}
				
				if (!initialized) {
					initString = initString + WHITESPACE + "true";
					initProperty.setPropertyValue(initString);
					administrationService.saveGlobalProperty(initProperty);
				}
				
				clusterOffset++;
				if (clusterOffset == ArrayUtils.getLength(clusterName))
					clusterOffset = 0;
				
				globalProperty.setPropertyValue(String.valueOf(clusterOffset));
				administrationService.saveGlobalProperty(globalProperty);
			}
		}
	}
}
