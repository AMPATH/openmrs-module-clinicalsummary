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

package org.openmrs.module.clinicalsummary.web.controller.upload;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.Constants;
import org.openmrs.module.clinicalsummary.io.SummariesTaskInstance;
import org.openmrs.module.clinicalsummary.io.utils.TaskConstants;
import org.openmrs.module.clinicalsummary.io.utils.TaskUtils;
import org.openmrs.module.clinicalsummary.util.ServerUtil;
import org.openmrs.module.clinicalsummary.web.controller.WebUtils;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 */
@Controller
@RequestMapping("/module/clinicalsummary/upload/uploadSummaries")
public class UploadSummariesController {

    private static final Log log = LogFactory.getLog(UploadSummariesController.class);

    private static final int IV_SIZE = 16;

    @RequestMapping(method = RequestMethod.GET)
    public void populatePage(final ModelMap map) {
    }

    @RequestMapping(method = RequestMethod.POST)
    public String processSubmit(final @RequestParam(required = true, value = "password") String password,
                              final @RequestParam(required = true, value = "summaries") MultipartFile summaries,
                              final HttpServletRequest request) {
        if (Context.isAuthenticated()) {
            if (!ServerUtil.isCentral()) {
                try {
                    String filename = WebUtils.prepareFilename(null, null);
                    log.info("Creating zipped file: " + filename);
                    File encryptedPath = OpenmrsUtil.getDirectoryInApplicationDataDirectory(Constants.ENCRYPTION_LOCATION);
                    String encryptedFilename = StringUtils.join(Arrays.asList(filename, TaskConstants.FILE_TYPE_ENCRYPTED), ".");
                    OutputStream encryptedOutputStream = new FileOutputStream(new File(encryptedPath, encryptedFilename));
                    FileCopyUtils.copy(summaries.getInputStream(), encryptedOutputStream);
                    validate(filename, password);
                    upload(filename, password);
                } catch (Exception e) {
                    log.error("Unable to process uploaded documents.", e);
                    HttpSession session = request.getSession();
                    session.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Unable to validate upload parameters. Please try again.");
                    return "redirect:" + request.getHeader("Referer");
                }
            }
        }
        return null;
    }

    public void upload(final String filename, final String password) throws Exception {
        log.info("Processing zip file and init vector!");
        SummariesTaskInstance.getInstance().startUploading(password, filename);
    }

    public void validate(final String filename, final String password) throws Exception {
        String encryptedFilename = StringUtils.join(Arrays.asList(filename, TaskConstants.FILE_TYPE_ENCRYPTED), ".");
        ZipFile encryptedFile = new ZipFile(new File(TaskUtils.getEncryptedOutputPath(), encryptedFilename));

        byte[] initVector = null;
        byte[] encryptedSampleBytes = null;
        Enumeration<? extends ZipEntry> entries = encryptedFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            String zipEntryName = zipEntry.getName();
            if (zipEntryName.endsWith(TaskConstants.FILE_TYPE_SECRET)) {
                InputStream inputStream = encryptedFile.getInputStream(zipEntry);
                initVector = FileCopyUtils.copyToByteArray(inputStream);
                if (initVector.length != IV_SIZE) {
                    throw new Exception("Secret file is corrupted or invalid secret file are being used.");
                }
            } else if (zipEntryName.endsWith(TaskConstants.FILE_TYPE_SAMPLE)) {
                InputStream inputStream = encryptedFile.getInputStream(zipEntry);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                FileCopyUtils.copy(inputStream, baos);
                encryptedSampleBytes = baos.toByteArray();
            }
        }

        if (initVector != null && encryptedSampleBytes != null) {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(TaskConstants.SECRET_KEY_FACTORY);
            KeySpec spec = new PBEKeySpec(password.toCharArray(), password.getBytes(), 1024, 128);
            SecretKey tmp = factory.generateSecret(spec);
            // generate the secret key
            SecretKey secretKey = new SecretKeySpec(tmp.getEncoded(), TaskConstants.KEY_SPEC);
            // create the cipher
            Cipher cipher = Cipher.getInstance(TaskConstants.CIPHER_CONFIGURATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(initVector));
            // decrypt the sample
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(encryptedSampleBytes);
            CipherInputStream cipherInputStream = new CipherInputStream(byteArrayInputStream, cipher);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileCopyUtils.copy(cipherInputStream, baos);

            String sampleText = baos.toString();
            if (!sampleText.contains("This is sample text")) {
                throw new Exception("Upload parameters incorrect!");
            }
        }
    }
}
