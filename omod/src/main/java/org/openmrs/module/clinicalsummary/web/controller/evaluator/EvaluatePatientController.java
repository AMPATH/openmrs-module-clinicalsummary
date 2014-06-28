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

package org.openmrs.module.clinicalsummary.web.controller.evaluator;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.pdfbox.util.PDFMergerUtility;
import org.apache.xmlgraphics.util.MimeConstants;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.Summary;
import org.openmrs.module.clinicalsummary.evaluator.Evaluator;
import org.openmrs.module.clinicalsummary.evaluator.EvaluatorUtils;
import org.openmrs.module.clinicalsummary.evaluator.LoggerUtils;
import org.openmrs.module.clinicalsummary.evaluator.velocity.VelocityEvaluator;
import org.openmrs.module.clinicalsummary.rule.ResultCacheInstance;
import org.openmrs.module.clinicalsummary.service.IndexService;
import org.openmrs.module.clinicalsummary.service.SummaryService;
import org.openmrs.module.clinicalsummary.web.controller.WebUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.util.Arrays;

/**
 */
@Controller
@RequestMapping(value = "/module/clinicalsummary/evaluator/evaluatePatient")
public class EvaluatePatientController {

    private static final Log log = LogFactory.getLog(EvaluatePatientController.class);

    @RequestMapping(method = RequestMethod.GET)
    public String populatePage(final @RequestParam("patientId") Integer patientId,
                               final HttpServletRequest request,
                               final HttpServletResponse response) {

        IndexService indexService = Context.getService(IndexService.class);

        Patient patient = Context.getPatientService().getPatient(patientId);
        try {
            String filenamePdf = WebUtils.prepareFilename(patientId, Evaluator.FILE_TYPE_PDF);
            File attachmentFile = new File(System.getProperty("java.io.tmpdir"), filenamePdf);
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(attachmentFile));

            PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
            pdfMergerUtility.setDestinationStream(outputStream);

            FopFactory fopFactory = FopFactory.newInstance();
            FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            Evaluator evaluator = new VelocityEvaluator();
            SummaryService summaryService = Context.getService(SummaryService.class);

            String filenameXml = StringUtils.join(Arrays.asList(patientId, Evaluator.FILE_TYPE_XML), ".");
            for (Summary summary : summaryService.getSummaries(patient)) {
                try {
                    double start = System.currentTimeMillis();

                    evaluator.evaluate(summary, patient, Boolean.TRUE);
                    indexService.saveIndex(indexService.generateIndex(patient, summary));

                    File outputDirectory = EvaluatorUtils.getOutputDirectory(summary);
                    File summaryXmlFile = new File(outputDirectory, filenameXml);

                    File tempFile = File.createTempFile("summary", "temp");
                    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(tempFile));
                    Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, bufferedOutputStream);

                    Source xsltSource = new StreamSource(new StringReader(summary.getXslt()));
                    Transformer transformer = transformerFactory.newTransformer(xsltSource);

                    Result result = new SAXResult(fop.getDefaultHandler());
                    Source source = new StreamSource(summaryXmlFile);
                    transformer.transform(source, result);

                    bufferedOutputStream.close();
                    pdfMergerUtility.addSource(tempFile);

                    LoggerUtils.extractLogInformation(documentBuilder.parse(summaryXmlFile), LoggerUtils.getViewingLogFile());
                    ResultCacheInstance.getInstance().clearCache(patient);

                    double elapsed = System.currentTimeMillis() - start;
                    log.info("Velocity evaluator running for " + elapsed + "ms (" + (elapsed / 1000) + "s)");
                } catch (Exception e) {
                    log.error("Summary with type: " + summary.getName() + " for patient: " + patientId, e);
                }
            }
            pdfMergerUtility.mergeDocuments();
            attachAndPurgeFile(response, attachmentFile);
            return null;
        } catch (Exception e) {
            log.error("Failed generating attachment for patient " + patientId, e);
        }

        return "redirect:" + request.getHeader("Referer");
    }

    /**
     * Attach the file to the servlet response body
     *
     * @param response the response
     * @param file     the file
     * @throws Exception
     */
    private void attachAndPurgeFile(final HttpServletResponse response, final File file) throws Exception {
        response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
        response.setContentType(MimeConstants.MIME_PDF);
        response.setContentLength((int) file.length());

        FileCopyUtils.copy(new FileInputStream(file), response.getOutputStream());

        if (!file.delete()) {
            log.info("Deleting temporary file failed!");
        }
    }

}
