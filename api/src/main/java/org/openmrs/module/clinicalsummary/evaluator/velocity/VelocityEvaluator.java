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

package org.openmrs.module.clinicalsummary.evaluator.velocity;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.Log4JLogChute;
import org.apache.xerces.impl.dv.util.Base64;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.logic.Rule;
import org.openmrs.module.clinicalsummary.Constants;
import org.openmrs.module.clinicalsummary.Summary;
import org.openmrs.module.clinicalsummary.evaluator.Evaluator;
import org.openmrs.module.clinicalsummary.evaluator.EvaluatorUtils;
import org.openmrs.module.clinicalsummary.rule.post.AbstractPostProcessorRule;
import org.openmrs.module.clinicalsummary.service.EvaluatorService;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class VelocityEvaluator implements Evaluator {

	private static final Log log = LogFactory.getLog(VelocityEvaluator.class);

	private final FopFactory fopFactory;

	private final TransformerFactory transformerFactory;

	public VelocityEvaluator() {
		fopFactory = FopFactory.newInstance();
		transformerFactory = TransformerFactory.newInstance();
	}

	/**
	 * Evaluate a summary template
	 *
	 * @param summary      the summary template
	 * @param patient
	 * @param keepArtifact
	 */
	public void evaluate(final Summary summary, final Patient patient, final Boolean keepArtifact) {
		try {
			VelocityEngine engine = new VelocityEngine();
			engine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, Log4JLogChute.class.getName());
			engine.setProperty("runtime.log.logsystem.log4j.logger", VelocityEvaluator.class.getName());
			engine.init();

			VelocityContext context = new VelocityContext();
			context.put("patient", patient);
			context.put("patientId", patient.getPatientId());
			context.put("summary", summary);

			// custom utility functions
			context.put("fn", new VelocityUtils());

			StringWriter writer = new StringWriter();
			engine.evaluate(context, writer, VelocityEvaluator.class.getName(), summary.getXml());
			String artifact = postProcessArtifact(patient, summary, writer.toString());

			if (keepArtifact) {
				String xmlFilename = StringUtils.join(Arrays.asList(patient.getPatientId(), Evaluator.FILE_TYPE_XML), ".");
				File xmlFile = new File(EvaluatorUtils.getOutputDirectory(summary), xmlFilename);
				saveArtifact(artifact, xmlFile);
			}

			String filename = StringUtils.join(Arrays.asList(patient.getPatientId(), Evaluator.FILE_TYPE_PDF), ".");
			File file = new File(EvaluatorUtils.getOutputDirectory(summary), filename);
			OutputStream out = new BufferedOutputStream(new FileOutputStream(file));

			FOUserAgent agent = fopFactory.newFOUserAgent();
			Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, agent, out);

			Source xsltSource = new StreamSource(new StringReader(summary.getXslt()));
			Transformer transformer = transformerFactory.newTransformer(xsltSource);

			Result result = new SAXResult(fop.getDefaultHandler());
			Source source = new StreamSource(new StringReader(artifact));
			transformer.transform(source, result);

			out.close();
		} catch (Exception e) {
			log.error("Evaluating " + summary.getName() + " on patient " + patient.getPatientId() + " failed ...", e);
		}
	}

	/**
	 * @param summary
	 * @param artifact
	 * @return
	 */
	private String postProcessArtifact(final Patient patient, final Summary summary, final String artifact) {
		String processedArtifact = artifact;
		// get all post evaluation processing tokens
		String tokenProperties = Context.getAdministrationService().getGlobalProperty(Constants.POST_EVALUATION_TOKEN);
		// perform post processing according to the configuration
		if (StringUtils.isNotEmpty(tokenProperties)) {
			Map<String, Object> parameters = new HashMap<String, Object>();
			EvaluatorService evaluatorService = Context.getService(EvaluatorService.class);
			for (String tokenProperty : StringUtils.split(tokenProperties, ",")) {
				String token = StringUtils.trim(tokenProperty);
				Rule rule = Context.getLogicService().getRule(token);
				// only process if the rule is subclass of the post processor abstract rule
				if (ClassUtils.isAssignable(rule.getClass(), AbstractPostProcessorRule.class)) {
					parameters.put(Evaluator.POST_EVALUATION_ARTIFACT, Base64.encode(processedArtifact.getBytes()));
					parameters.put(Evaluator.POST_EVALUATION_TEMPLATE, summary.getName());
					processedArtifact = evaluatorService.evaluate(patient, token, parameters).toString();
				}
			}
		}
		return processedArtifact;
	}

	/**
	 * @param artifact
	 * @param file
	 */
	private void saveArtifact(final String artifact, final File file) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(artifact));
			Document document = db.parse(is);

			OutputFormat format = new OutputFormat();
			format.setIndenting(true);
			format.setLineWidth(150);

			XMLSerializer xmlSerializer = new XMLSerializer(new FileWriter(file), format);
			xmlSerializer.serialize(document);
		} catch (Exception e) {
			log.error("Saving xml artifact failed ...", e);
		}
	}
}
