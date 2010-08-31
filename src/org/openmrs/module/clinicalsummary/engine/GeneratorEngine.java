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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.openmrs.Cohort;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.MappingPosition;
import org.openmrs.module.clinicalsummary.SummaryConstants;
import org.openmrs.module.clinicalsummary.SummaryError;
import org.openmrs.module.clinicalsummary.SummaryIndex;
import org.openmrs.module.clinicalsummary.SummaryService;
import org.openmrs.module.clinicalsummary.SummaryTemplate;
import org.openmrs.module.clinicalsummary.cache.DataProvider;
import org.openmrs.module.clinicalsummary.cache.SummaryDataSource;
import org.openmrs.module.clinicalsummary.concept.StandardConceptConstants;
import org.openmrs.module.clinicalsummary.deprecated.SummaryExportFunctions;
import org.openmrs.module.clinicalsummary.rule.RuleConstants;
import org.openmrs.util.OpenmrsUtil;

/**
 *
 */
public class GeneratorEngine {
	
	private static final Log log = LogFactory.getLog(GeneratorEngine.class);
	
	private final File outputLocation;
	
	private final VelocityContextUtils functions;
	
	public GeneratorEngine(DataProvider provider, File outputLocation) {
		this.outputLocation = outputLocation;
		this.functions = new VelocityContextUtils();
		Context.getService(SummaryService.class).setLogicDataSource("summary", new SummaryDataSource(provider));
	}
	
	private Collection<SummaryTemplate> prepareTemplate(Patient patient) {
		Collection<SummaryTemplate> templates = new HashSet<SummaryTemplate>();
		
		// prepare the templates mapped to the latest encounter
		Collection<String> typeNames = new HashSet<String>();
		for (SummaryTemplate summaryTemplate : TemplateRegistry.getCachedTemplates(MappingPosition.LATEST_ENCOUNTER))
			for (EncounterType encounterType : summaryTemplate.getEncounterTypes())
				typeNames.add(encounterType.getName());
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(RuleConstants.INCLUDED_ENCOUNTER_TYPES, typeNames);
		parameters.put(RuleConstants.EVALUATED_ENCOUNTER, SummaryDataSource.ENCOUNTER_TYPE);
		
		org.openmrs.logic.result.Result encounterResults = functions.eval(patient, "Complete Encounter", parameters);
		org.openmrs.logic.result.Result latestResult = encounterResults.latest();
		Encounter latestEncounter = (Encounter) latestResult.getResultObject();
		templates.addAll(TemplateRegistry.getCachedTemplates(latestEncounter));
		
		// prepare the templates mapped to any encounter
		for (SummaryTemplate summaryTemplate : TemplateRegistry.getCachedTemplates(MappingPosition.ANY_ENCOUNTER)) {
			// re-use this thing, recycling is goooodddd!!!!
			typeNames = new HashSet<String>();
			for (EncounterType encounterType : summaryTemplate.getEncounterTypes())
				typeNames.add(encounterType.getName());
			
			parameters = new HashMap<String, Object>();
			parameters.put(RuleConstants.INCLUDED_ENCOUNTER_TYPES, typeNames);
			parameters.put(RuleConstants.EVALUATED_ENCOUNTER, SummaryDataSource.ENCOUNTER_TYPE);
			
			encounterResults = functions.eval(patient, "Complete Encounter", parameters);
			latestResult = encounterResults.latest();
			latestEncounter = (Encounter) latestResult.getResultObject();
			templates.addAll(TemplateRegistry.getCachedTemplates(latestEncounter));
		}
		
		return templates;
	}
	
	@SuppressWarnings("deprecation")
    private void generateSummary(Patient patient) {
		
		if (log.isDebugEnabled())
			log.debug("Generating summary file for patient " + patient.getPatientId() + "...");
		
		try {
			// This is based on:
			// http://velocity.apache.org/engine/releases/velocity-1.6.2/developer-guide.html#Configuring_Logging
			Velocity.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.Log4JLogChute");
			Velocity.setProperty("runtime.log.logsystem.log4j.logger", GeneratorEngine.class.getName());
			Velocity.init();
			
			VelocityContext context = new VelocityContext();
			context.put("patient", patient);
			context.put("functions", functions);
			
			Cohort patientSet = new Cohort();
			patientSet.addMember(patient.getPatientId());
			SummaryExportFunctions exportFunctions = new SummaryExportFunctions();
			exportFunctions.setPatientSet(patientSet);
			
			context.put("fn", exportFunctions);
			context.put("patientSet", patientSet);
			
			Collection<SummaryTemplate> templates = prepareTemplate(patient);
			
			for (SummaryTemplate template : templates) {
				
				try {
					
					Writer writer = new StringWriter();
					Velocity.evaluate(context, writer, GeneratorEngine.class.getName(), template.getTemplate());
					
					String filename = patient.getPatientId() + "_" + template.getTemplateId() + ".pdf";
					File file = new File(outputLocation, filename);
					BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
					
					FOUserAgent foAgent = FopFactory.newInstance().newFOUserAgent();
					Fop fop = FopFactory.newInstance().newFop(MimeConstants.MIME_PDF, foAgent, out);
					Source xsltSource = new StreamSource(new StringReader(template.getXslt()));
					Transformer transformer = TransformerFactory.newInstance().newTransformer(xsltSource);
					Result result = new SAXResult(fop.getDefaultHandler());
					Source source = new StreamSource(new StringReader(writer.toString()));
					transformer.transform(source, result);
					
					out.close();
					
					setIndex(patient, template);
					
				}
				catch (Exception e) {
					setFatalException(patient, e);
					log.error("Failed generating summary ...", e);
				}
			}
		}
		catch (Exception e) {
			setFatalException(patient, e);
			log.error("Failed generating summary ...", e);
		}
	}
	
	private void setIndex(Patient patient, SummaryTemplate template) throws Exception {
		
		SummaryService summaryService = Context.getService(SummaryService.class);
		SummaryIndex index = summaryService.getIndex(patient, template);
		
		if (index == null) {
			index = new SummaryIndex();
			// new index are initiated with the day the patient get the first summary
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.YEAR, 1910);
			calendar.set(Calendar.MONTH, 0);
			calendar.set(Calendar.DATE, 1);
			// initial date of the initial summary will always be 1910-01-01
			// (this is the default in the sql statement to create the table)
			index.setInitialDate(calendar.getTime());
			// initial date of the generated date always will be today
			index.setGeneratedDate(new Date());
		}
		
		index.setPatient(patient);
		index.setTemplate(template);
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(RuleConstants.EVALUATED_CONCEPT, StandardConceptConstants.RETURN_VISIT_DATE);
		
		List<String> typeNames = new ArrayList<String>();
		for (EncounterType encounterType : template.getEncounterTypes())
			typeNames.add(encounterType.getName());
		parameters.put(RuleConstants.INCLUDED_ENCOUNTER_TYPES, typeNames);
		
		org.openmrs.logic.result.Result obsResult = functions.eval(patient, "Datetime Latest Obs", parameters);
		Obs observation = (Obs) obsResult.getResultObject();
		if (observation != null) {
			Location location = observation.getLocation();
			Date earliestDate = summaryService.getEarliestIndex(location);
			// if we have the initial date for this location, then set the initial to that date
			if (earliestDate != null)
				index.setInitialDate(earliestDate);
			index.setLocation(observation.getLocation());
			index.setReturnDate(observation.getValueDatetime());
			// this generated date will prevent us from printing duplicates
			// generated will be set for current date, but it will be set to latest obs date if we have the obs
			index.setGeneratedDate(observation.getDateCreated());
		}
		
		summaryService.saveIndex(index);
	}
	
	private void setFatalException(Patient patient, Exception e) {
		
		SummaryService summaryService = Context.getService(SummaryService.class);
		
		SummaryError error = new SummaryError();
		error.setPatient(patient);
		error.setErrorDetails(e.toString());
		
		summaryService.saveError(error);
	}
	
	public static void generateSummary(Cohort cohort) {
		
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
				GeneratorEngine generatorEngine = new GeneratorEngine(provider, folder);
				
				for (Integer processedId : subCohort.getMemberIds())
					generatorEngine.generateSummary(patientService.getPatient(processedId));
				
				subCohort = new Cohort();
				Context.clearSession();
			}
		}
	}
	
}
