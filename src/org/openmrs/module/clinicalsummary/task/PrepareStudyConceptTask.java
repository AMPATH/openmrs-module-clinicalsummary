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
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNameTag;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.tasks.AbstractTask;

/**
 *
 */
public class PrepareStudyConceptTask extends AbstractTask {
	
	public static final String PEDIATRIC_CONTROL = "PEDIATRIC CONTROL";
	
	public static final String PEDIATRIC_INTERVENTION = "PEDIATRIC INTERVENTION";
	
	private static final Log log = LogFactory.getLog(PrepareStudyConceptTask.class);
	
	/**
	 * @see org.openmrs.scheduler.tasks.AbstractTask#execute()
	 */
	@Override
	public void execute() {
		log.debug("Running register default rule task ...");
		Context.openSession();
		try {
			if (!Context.isAuthenticated())
				authenticate();
			
			createConcept(PrepareStudyConceptTask.PEDIATRIC_CONTROL);
			createConcept(PrepareStudyConceptTask.PEDIATRIC_INTERVENTION);
			
		} catch (Exception e) {
			log.info("Exception thrown while running generator ...", e);
		} finally {
			Context.closeSession();
		}
	}
	
	private void createConcept(String name) {
		Concept existingConcept = Context.getConceptService().getConcept(name);
		if (existingConcept == null) {
			
			Concept concept = new Concept();
			concept.setConceptClass(Context.getConceptService().getConceptClassByName("Misc"));
			concept.setDatatype(Context.getConceptService().getConceptDatatypeByName("N/A"));
			
			ConceptName conceptName = new ConceptName(name, Context.getLocale());
			conceptName.addTag(new ConceptNameTag("preferred_en", "Preferred name in a english"));
			concept.addName(conceptName);
			
			Context.getConceptService().saveConcept(concept);
		}
	}
	
	@Override
	public void shutdown() {
		super.shutdown();
		log.debug("Shutting down Summary Generator Task ...");
	}
}
