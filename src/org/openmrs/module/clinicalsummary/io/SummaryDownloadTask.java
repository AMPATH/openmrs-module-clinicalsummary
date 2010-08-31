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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.spec.KeySpec;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.openmrs.module.clinicalsummary.SummaryConstants;
import org.openmrs.util.OpenmrsUtil;

/**
 *
 */
public final class SummaryDownloadTask extends SummaryIO {
	
	private SummaryDownloadTask(String password, String passphrase, String filename) throws GeneralSecurityException {
		super(password, passphrase, filename);
	}

	static synchronized void startTask(String password, String passphrase, String filename) {
		if (status != SummaryIOStatus.RUNNING) {
			try {
	            instance = new SummaryDownloadTask(password, passphrase, filename);
	            executorService = Executors.newCachedThreadPool();
	            executorService.execute(instance);
	            type = SummaryIO.DOWNLOAD;
				status = SummaryIOStatus.RUNNING;
            }
            catch (Exception e) {
    	        log.error("Preparing zipped file failed ...", e);
				status = SummaryIOStatus.FAILED;
            }
		}
	}
	
	/**
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		try {
			initializeCipher();
			processIndex();
			processSummaries();
			processInitVector();
			status = SummaryIOStatus.FINISHED;
        }
        catch (Exception e) {
			log.error("Preparing zipped file failed ...", e);
			status = SummaryIOStatus.FAILED;
        }
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.io.SummaryIO#initializeCipher()
	 */
	@Override
	protected final void initializeCipher() throws GeneralSecurityException {
		SecretKeyFactory factory = SecretKeyFactory.getInstance(SECRET_KEY_FACTORY);
		KeySpec spec = new PBEKeySpec(password.toCharArray(), passphrase.getBytes(), 1024, 128);
		SecretKey tmp = factory.generateSecret(spec);
		
		SecretKey secret = new SecretKeySpec(tmp.getEncoded(), KEY_SPEC);
		
		cipher = Cipher.getInstance(CIPHER_CONFIGURATION);
		cipher.init(Cipher.ENCRYPT_MODE, secret);
	}

	/**
	 * @see org.openmrs.module.clinicalsummary.io.SummaryIO#processIndex()
	 */
	@Override
    protected final void processIndex() throws IOException, InterruptedException {
		File inputPath = OpenmrsUtil.getDirectoryInApplicationDataDirectory(SummaryConstants.GENERATED_PDF_LOCATION);
		File indexFile = new File(inputPath, "summaryindex.sql");
		// all tables that must be taken to the standalone or remote machine
		String[] tables = { "clinical_summary",
							"clinical_summary_index",
							"clinical_summary_mapping",
							"location",
		        			"patient_identifier_type",
		        			"person",
		        			"patient",
		        			"patient_identifier",
		        			"person_name",
		        			"person_address",
		        			"privilege",
		        			"role",
		        			"role_privilege",
		        			"role_role",
		        			"user_group",
		        			"user_property",
		        			"user_role",
		        			"users" };
		
		// command that need to be executed to get the required tables
		String[] commands = { "mysqldump",
							"-u" + databaseUsername,
							"-p" + databasePassword,
							"-x",
							"-q",
							"-e",
							"--add-drop-table",
							"-r", indexFile.getAbsolutePath(),
							databaseName };
		
		commands = (String[]) ArrayUtils.addAll(commands, tables);
		executeCommand(inputPath, commands);
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.io.SummaryIO#processSummaries()
	 */
	@Override
    protected final void processSummaries() throws IOException {
		
		File inputPath = OpenmrsUtil.getDirectoryInApplicationDataDirectory(SummaryConstants.GENERATED_PDF_LOCATION);
		
		File outputPath = OpenmrsUtil.getDirectoryInApplicationDataDirectory(SummaryConstants.ZIPPED_LOCATION);
		File outputFile = new File(outputPath, filename + ZIP_EXTENSION);
		
		FileOutputStream outputStream = new FileOutputStream(outputFile);
		ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(outputStream));
		
		File files[] = inputPath.listFiles();
		
		total = FileUtils.sizeOfDirectory(inputPath);
		
		CipherInputStream origin = null;
		
		byte data[] = new byte[BUFFER_SIZE];
		for (File file : files) {
			
			// exclude directory
			if (file.isDirectory())
				continue;
			
			FileInputStream inputStream = new FileInputStream(file);
			origin = new CipherInputStream(inputStream, cipher);
			
			// create a zip entry with only the file name to prevent creating the entire folder structure when we un-zip the file
			ZipEntry entry = new ZipEntry(file.getName());
			out.putNextEntry(entry);
			
			int count;
			while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1)
				out.write(data, 0, count);
			
			origin.close();
			
			processed = processed + file.length();
		}
		
		out.close();
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.io.SummaryIO#processInitVector()
	 */
	@Override
	protected final void processInitVector() throws IOException, GeneralSecurityException {
		AlgorithmParameters params = cipher.getParameters();
		byte[] initVector = params.getParameterSpec(IvParameterSpec.class).getIV();
		File initVectorFolder = OpenmrsUtil.getDirectoryInApplicationDataDirectory(SummaryConstants.ENCRYPTION_LOCATION);
		File initVectorFile = new File(initVectorFolder, filename);
		FileOutputStream fos = new FileOutputStream(initVectorFile);
		fos.write(initVector);
		fos.close();
	}
}
