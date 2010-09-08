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
package org.openmrs.module.clinicalsummary.web.service;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.openmrs.Cohort;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.SummaryService;
import org.openmrs.module.clinicalsummary.SummaryTemplate;
import org.openmrs.module.clinicalsummary.cache.DataProvider;
import org.openmrs.module.clinicalsummary.cache.SummaryDataSource;
import org.openmrs.module.clinicalsummary.deprecated.SummaryExportFunctions;
import org.openmrs.module.clinicalsummary.engine.GeneratorEngine;
import org.openmrs.module.clinicalsummary.engine.VelocityContextUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SummaryXMLController {
	
	private static final Log log = LogFactory.getLog(SummaryXMLController.class);
	
	@SuppressWarnings("deprecation")
    @RequestMapping("/module/clinicalsummary/xml")
	public void generate(@RequestParam(required = true, value = "patientId") int patientId,
	                     @RequestParam(required = true, value = "templateId") int templateId, HttpServletResponse response) {
		
		if (Context.isAuthenticated()) {
			
			try {
	            Patient patient = Context.getPatientService().getPatient(patientId);
	            SummaryTemplate template = Context.getService(SummaryService.class).getTemplate(templateId);

	            // This is based on:
	            // http://velocity.apache.org/engine/releases/velocity-1.6.2/developer-guide.html#Configuring_Logging
	            Velocity.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.Log4JLogChute");
	            Velocity.setProperty("runtime.log.logsystem.log4j.logger", GeneratorEngine.class.getName());
	            Velocity.init();
	            
	            Cohort cohort = new Cohort();
	            cohort.addMember(patientId);
	            
	            DataProvider provider = new DataProvider(cohort);
	            Context.getService(SummaryService.class).setLogicDataSource("summary", new SummaryDataSource(provider));
	            
	            SummaryExportFunctions exportFunctions = new SummaryExportFunctions();
	            exportFunctions.setPatientSet(cohort);
	            
	            VelocityContext context = new VelocityContext();
	            context.put("patient", patient);
	            context.put("functions", new VelocityContextUtils());
	            context.put("fn", exportFunctions);
	            context.put("patientSet", cohort);
	            
	            response.setContentType("text/xml");
	            Velocity.evaluate(context, response.getWriter(), GeneratorEngine.class.getName(), template.getTemplate());
	            
            } catch (Exception e) {
	            log.error("Error generating xml data ...", e);
            }
		}
	}
}
