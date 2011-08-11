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
import org.openmrs.module.clinicalsummary.io.utils.TaskConstants;
import org.openmrs.module.clinicalsummary.web.controller.MimeType;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.security.spec.KeySpec;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 */
@Controller
@RequestMapping("/module/clinicalsummary/upload/validateUpload")
public class UploadValidatorController {

	private static final Log log = LogFactory.getLog(UploadValidatorController.class);

	public static final int INIT_VECTOR_SIZE = 16;

	@RequestMapping(method = RequestMethod.POST)
	public void validate(final @RequestParam(required = true, value = "password") String password,
	                     final @RequestParam(required = true, value = "passphrase") String passphrase,
	                     final @RequestParam(required = true, value = "secretFile") MultipartFile secret,
	                     final @RequestParam(required = true, value = "summaries") MultipartFile summaries,
	                     final HttpServletResponse response) {
		try {
			byte[] initVector = FileCopyUtils.copyToByteArray(secret.getInputStream());

			SecretKeyFactory factory = SecretKeyFactory.getInstance(TaskConstants.SECRET_KEY_FACTORY);
			KeySpec spec = new PBEKeySpec(password.toCharArray(), passphrase.getBytes(), 1024, 128);
			SecretKey tmp = factory.generateSecret(spec);

			SecretKey secretKey = new SecretKeySpec(tmp.getEncoded(), TaskConstants.KEY_SPEC);

			Cipher cipher = Cipher.getInstance(TaskConstants.CIPHER_CONFIGURATION);
			cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(initVector));

			ZipInputStream zis = new ZipInputStream(new BufferedInputStream(summaries.getInputStream()));

			byte data[] = new byte[TaskConstants.BUFFER_SIZE];

			// get a sample of pdf file from the zipped document
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null)
				if (StringUtils.endsWithIgnoreCase(entry.getName(), "pdf"))
					break;

			if (entry != null) {
				response.setHeader("Content-Disposition", "attachment; filename=example.pdf");
				response.setContentType(MimeType.APPLICATION_PDF);
				response.setContentLength((int) entry.getSize());

				CipherOutputStream dest = new CipherOutputStream(response.getOutputStream(), cipher);

				int count;
				while ((count = zis.read(data, 0, TaskConstants.BUFFER_SIZE)) != -1)
					dest.write(data, 0, count);

				dest.close();
			}
			zis.close();
		} catch (Exception e) {
			log.error("Uploading zipped file failed ...", e);
		}
	}
}
