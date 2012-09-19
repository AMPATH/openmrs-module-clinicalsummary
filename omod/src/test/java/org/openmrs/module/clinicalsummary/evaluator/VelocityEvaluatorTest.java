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
package org.openmrs.module.clinicalsummary.evaluator;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.junit.Test;

public class VelocityEvaluatorTest {

    private static final Log log = LogFactory.getLog(VelocityEvaluatorTest.class);

    @Test
    public void evaluate_shouldEvaluateAndGenerateSummary() throws Exception {
        FopFactory fopFactory = FopFactory.newInstance();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        File file = new File("/home/nribeka/Summary/OutputExample.pdf");
        OutputStream out = new BufferedOutputStream(new FileOutputStream(file));

        FOUserAgent agent = fopFactory.newFOUserAgent();
        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, agent, out);

        BufferedInputStream xmlInputStream = new BufferedInputStream(new FileInputStream("/home/nribeka/Summary/phc-adult.xml"));
        BufferedInputStream xsltInputStream = new BufferedInputStream(new FileInputStream("/home/nribeka/Summary/phc-adult-xslfo.xml"));

        Source xsltSource = new StreamSource(xsltInputStream);
        Transformer transformer = transformerFactory.newTransformer(xsltSource);

        Result result = new SAXResult(fop.getDefaultHandler());
        Source source = new StreamSource(xmlInputStream);
        transformer.transform(source, result);

        out.close();
    }

    public void evaluate_shouldSubstituteString() throws Exception {
        File directory = new File("/home/nribeka/Desktop/Yaw");

        File substitutionFile = new File(directory, "substitute.txt");

        Map<String, String> substitutes = new HashMap<String, String>();
        // read and create the substitution map
        String line = null;
        BufferedReader reader = new BufferedReader(new FileReader(substitutionFile));
        while ((line = reader.readLine()) != null) {
            String[] elements = StringUtils.split(line, ",");
            if (elements.length == 2)
                substitutes.put(elements[0], elements[1]);
        }
        reader.close();

        // start reading and substitute the file
        FileFilter fileFilter = new WildcardFileFilter("invalid*.txt");
        for (File file : directory.listFiles(fileFilter)) {
            System.out.println(file.getName());

            File outputFile = new File(directory, FilenameUtils.getBaseName(file.getName()));

            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));

            String invalidLine = null;
            while ((invalidLine = bufferedReader.readLine()) != null) {
                System.out.println(invalidLine);
                String[] elements = StringUtils.splitPreserveAllTokens(invalidLine, ",");
                StringBuilder builder = new StringBuilder();
                if (substitutes.containsKey(elements[0]))
                    builder.append(substitutes.get(elements[0]));
                else
                    builder.append(elements[0]);
                builder.append(",");
                builder.append(elements[1]);
                bufferedWriter.write(builder.toString());
                bufferedWriter.newLine();
            }

            bufferedReader.close();
            bufferedWriter.close();
        }
    }

}
