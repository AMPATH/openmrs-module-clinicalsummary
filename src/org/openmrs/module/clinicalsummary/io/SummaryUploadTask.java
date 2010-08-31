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
package org.openmrs.module.clinicalsummary.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.spec.KeySpec;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.openmrs.module.clinicalsummary.SummaryConstants;
import org.openmrs.util.OpenmrsUtil;

/**
 *
 */
public final class SummaryUploadTask extends SummaryIO {
	
	private static final int IV_SIZE = 16;
	
	private byte[] initVector;
	
	private SummaryUploadTask(String password, String passphrase, String filename) throws GeneralSecurityException {
		super(password, passphrase, filename);
	}
	
	static synchronized void startTask(String password, String passphrase, String filename) {
		if (status != SummaryIOStatus.RUNNING) {
			try {
				instance = new SummaryUploadTask(password, passphrase, filename);
				executorService = Executors.newCachedThreadPool();
				executorService.execute(instance);
				type = SummaryIO.UPLOAD;
				status = SummaryIOStatus.RUNNING;
			}
			catch (Exception e) {
				log.error("Uploading zipped file failed ...", e);
				status = SummaryIOStatus.FAILED;
			}
		}
	}
	
	@Override
	public void run() {
		try {
			processInitVector();
			initializeCipher();
			processSummaries();
			processIndex();
			status = SummaryIOStatus.FINISHED;
		}
		catch (Exception e) {
			log.error("Uploading zipped file failed ...", e);
			status = SummaryIOStatus.FAILED;
		}
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.io.SummaryIO#processInitVector()
	 */
	@Override
	protected void processInitVector() throws IOException, GeneralSecurityException {
		File initVectorFolder = OpenmrsUtil.getDirectoryInApplicationDataDirectory(SummaryConstants.ENCRYPTION_LOCATION);
		File initVectorFile = new File(initVectorFolder, filename);
		InputStream initVectorStream = new FileInputStream(initVectorFile);
		
		initVector = new byte[IV_SIZE];
		
		int ivSize = initVectorStream.read(initVector);
		if (ivSize != IV_SIZE)
			throw new IOException("Secret file is corrupted or invalid secret file are being used.");
		initVectorStream.close();
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.io.SummaryIO#initializeCipher()
	 */
	@Override
	protected void initializeCipher() throws GeneralSecurityException {
		SecretKeyFactory factory = SecretKeyFactory.getInstance(SECRET_KEY_FACTORY);
		KeySpec spec = new PBEKeySpec(password.toCharArray(), passphrase.getBytes(), 1024, 128);
		SecretKey tmp = factory.generateSecret(spec);
		
		if (log.isDebugEnabled())
			log.debug("Secret Key Length: " + tmp.getEncoded().length);
		
		SecretKey secret = new SecretKeySpec(tmp.getEncoded(), KEY_SPEC);
		
		cipher = Cipher.getInstance(CIPHER_CONFIGURATION);
		cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(initVector));
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.io.SummaryIO#processSummaries()
	 */
	@Override
	protected void processSummaries() throws IOException {
		
		File outputPath = OpenmrsUtil.getDirectoryInApplicationDataDirectory(SummaryConstants.GENERATED_PDF_LOCATION);
		
		File inputPath = OpenmrsUtil.getDirectoryInApplicationDataDirectory(SummaryConstants.ZIPPED_LOCATION);
		File inputFile = new File(inputPath, filename + ZIP_EXTENSION);
		
		total = inputFile.length();
		
		FileInputStream outputStream = new FileInputStream(inputFile);
		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(outputStream));
		
		byte data[] = new byte[BUFFER_SIZE];
		
		ZipEntry entry;
		CipherOutputStream dest = null;
		while ((entry = zis.getNextEntry()) != null) {
			
			File file = new File(outputPath, entry.getName());
			FileOutputStream fos = new FileOutputStream(file);
			dest = new CipherOutputStream(fos, cipher);
			
			int count;
			while ((count = zis.read(data, 0, BUFFER_SIZE)) != -1)
				dest.write(data, 0, count);
			
			dest.flush();
			dest.close();
			
			processed = processed + file.length();
		}
		
		zis.close();
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.io.SummaryIO#processIndex()
	 */
	@Override
	protected void processIndex() throws IOException, InterruptedException {
		
		File inputPath = OpenmrsUtil.getDirectoryInApplicationDataDirectory(SummaryConstants.GENERATED_PDF_LOCATION);
		File indexFile = new File(inputPath, "summaryindex.sql");
    	
    	String path = indexFile.getAbsolutePath();
    	path = path.replace("\\", "/");
		
		String[] commands = { "mysql" ,
				              "-e",
				              "source " + path,
				              "-f",
				              "-u" + databaseUsername,
				              "-p" + databasePassword,
				              "-D" + databaseName
				            };

		executeCommand(inputPath, commands);
	}
	
}
