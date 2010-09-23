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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.MappingPosition;
import org.openmrs.module.clinicalsummary.SummaryService;
import org.openmrs.module.clinicalsummary.SummaryTemplate;

/**
 *
 */
public class TemplateRegistry {
	
	private static final Log log = LogFactory.getLog(TemplateRegistry.class);
	
	/**
	 * Singleton instance of the class
	 */
	private static TemplateRegistry instance;
	
	/**
	 * Mapping from the position to the template
	 */
	private Map<MappingPosition, Collection<SummaryTemplate>> positionalTemplates;
	
	/**
	 * Inverted mapping of encounter type to the template
	 */
	private Map<EncounterType, Collection<SummaryTemplate>> templateMappings;
	
	/**
	 * The default template that will be given to a patient when the patient doesn't have any encounter
	 */
	private SummaryTemplate defaultTemplate;
	
	/**
	 * Initialize the class
	 */
	private TemplateRegistry() {
		initialize();
	}
	
	/**
	 * Get all templates and create the mapping between encounter type to the template
	 */
	private void initialize() {
		SummaryService summaryService = Context.getService(SummaryService.class);
		
		defaultTemplate = summaryService.getPreferredTemplate();
		
		Collection<SummaryTemplate> templates = summaryService.getAllTemplates();
		for (SummaryTemplate template : templates) {
			MappingPosition position = template.getPosition();
			Collection<SummaryTemplate> positionTemplates = getPositionalTemplates().get(template.getPosition());
			if (positionTemplates == null)
				getPositionalTemplates().put(position, positionTemplates = new HashSet<SummaryTemplate>());
			positionTemplates.add(template);
			
			for (EncounterType type : template.getEncounterTypes()) {
				Collection<SummaryTemplate> mappingTemplates = getTemplateMappings().get(type);
				if (mappingTemplates == null)
					getTemplateMappings().put(type, mappingTemplates = new HashSet<SummaryTemplate>());
				mappingTemplates.add(template);
            }
		}
	}
	
	/**
	 * Private entry point for the TemplateRegistry
	 * 
	 * @return singleton object of the template registry
	 */
	private static synchronized TemplateRegistry getInstance() {
		
		if (log.isDebugEnabled())
			log.debug("Fetching instance and the instance is " + instance + " ... ");
		
		if (instance == null)
			instance = new TemplateRegistry();
		
		return instance;
	}
	
	/**
	 * Return the value of the positionTemplate
	 * 
	 * @return the positionTemplate
	 */
	private final Map<MappingPosition, Collection<SummaryTemplate>> getPositionalTemplates() {
		if (positionalTemplates == null)
			positionalTemplates = new HashMap<MappingPosition, Collection<SummaryTemplate>>();
		return positionalTemplates;
	}
	
	/**
	 * Return the value of the templateMappings
	 * 
	 * @return the templateMappings
	 */
	private final Map<EncounterType, Collection<SummaryTemplate>> getTemplateMappings() {
		if (templateMappings == null)
			templateMappings = new HashMap<EncounterType, Collection<SummaryTemplate>>();
		return templateMappings;
	}
	
	/**
	 * Helper method to get list of summary templates by the mapping position
	 * 
	 * @param position the position of the encounter
	 * @return list of templates for the position
	 */
	private final Collection<SummaryTemplate> getTemplates(MappingPosition position) {
		Collection<SummaryTemplate> templates = getPositionalTemplates().get(position);
		if (templates == null)
			templates = new HashSet<SummaryTemplate>();
		return templates;
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.engine.TemplateRegistry#getTemplates(MappingPosition)
	 */
	public static Collection<SummaryTemplate> getCachedTemplates(MappingPosition position) {
		return getInstance().getTemplates(position);
	}
	
	/**
	 * Get the correct template for a patient based on the latest encounter type. This method is
	 * null safe. When encounter is null, an empty set will be returned.
	 * 
	 * @param encounterType the latest encounter type
	 * @return suitable template based on the mapping defined in the template. Returning preferred
	 *         template when the encounter type is not mapped to any particular template.
	 */
	private final Collection<SummaryTemplate> getTemplates(Encounter encounter) {
		if (encounter == null || !getTemplateMappings().containsKey(encounter.getEncounterType()))
			return Collections.emptySet();
		return getTemplateMappings().get(encounter.getEncounterType());
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.engine.TemplateRegistry#getTemplate(EncounterType)
	 */
	public static Collection<SummaryTemplate> getCachedTemplates(Encounter encounter) {
		return getInstance().getTemplates(encounter);
	}
	
	/**
	 * Get the default template that will be given to the patient when the patient doesn't have any encounters
	 * 
	 * @return the default template
	 */
	private SummaryTemplate getDefault() {
		return defaultTemplate;
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.engine.TemplateRegistry#getDefault()
	 */
	public static SummaryTemplate getDefaultTemplate() {
		return getInstance().getDefault();
	}
	
	/**
	 * Destroy the template registry instances
	 */
	private synchronized void destroy() {
		instance = null;
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.engine.TemplateRegistry#destroy()
	 */
	public static void destroyInstance() {
		getInstance().destroy();
	}
}
