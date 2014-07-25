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

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.AlgorithmParameters;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.clinicalsummary.enumeration.TaskStatus;
import org.openmrs.module.clinicalsummary.evaluator.Evaluator;
import org.openmrs.module.clinicalsummary.io.utils.TaskConstants;
import org.openmrs.module.clinicalsummary.io.utils.TaskUtils;

/**
 *
 */
class DownloadSummariesTask extends SummariesTask {

    private static final Log log = LogFactory.getLog(DownloadSummariesTask.class);

    private Boolean partial;

    public DownloadSummariesTask(final String password, final String filename, final Boolean partial) {
        super(password, filename);
        this.partial = partial;
    }

    /**
     * Method to initialize the cipher object with the correct encryption algorithm.
     *
     * @throws Exception
     */
    protected final void initializeCipher() throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(TaskConstants.SECRET_KEY_FACTORY);
        KeySpec spec = new PBEKeySpec(password.toCharArray(), password.getBytes(), 1024, 128);
        SecretKey tmp = factory.generateSecret(spec);

        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), TaskConstants.KEY_SPEC);

        if (log.isDebugEnabled())
            log.debug("Encrypting with: " + secret.getAlgorithm());

        cipher = Cipher.getInstance(TaskConstants.CIPHER_CONFIGURATION);
        cipher.init(Cipher.ENCRYPT_MODE, secret);
    }

    /**
     * Method that will be called to process the index file. Index file contains sql statement for the index table in the database. Download process will
     * execute mysqldump to get the content of the database. To execute mysqldump correctly, the database user must have RELOAD privilege
     *
     * @throws Exception
     */
    protected final void processIndex() throws Exception {
        File indexFile = new File(TaskUtils.getSummaryOutputPath(), TaskConstants.INDEX_CONFIGURATION_SQL_FILE);
        // all tables that must be taken to the standalone or remote machine
        String[] tables = {"clinical_summary", "clinical_summary_index", "clinical_summary_mapping", "location",
                "patient_identifier_type", "person", "patient", "patient_identifier", "person_name", "person_address",
                "privilege", "role", "role_privilege", "role_role", "user_property", "user_role", "users"};

        // command that need to be executed to get the required tables
        String[] commands = {"mysqldump", "-u" + databaseUser, "-p" + databasePassword, "-h" + databaseHost, "-P" + databasePort, "-x", "-q", "-e",
                "--add-drop-table", "-r", indexFile.getAbsolutePath(), databaseName};

        commands = (String[]) ArrayUtils.addAll(commands, tables);
        TaskUtils.executeCommand(TaskUtils.getSummaryOutputPath(), commands);
    }

    /**
     * Method that will be called to process the summary collection file. Download process will create one zipped and encrypted collection of summary
     * files.
     * <p/>
     * this.passphrase = password;
     *
     * @throws Exception
     */
    protected final void processSummaries() throws Exception {
        // TODO: The better approach would be to create zip file and then encrypt it.
        // And then Content of the zip file:
        // * Zipped file of summary files and sql file
        // * Sample file to be used for decryption testing
        String zipFilename = StringUtils.join(Arrays.asList(filename, TaskConstants.FILE_TYPE_ZIP), ".");
        File zipFile = new File(TaskUtils.getZippedOutputPath(), zipFilename);
        ZipOutputStream zipOutStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        Date cutOffDate = null;
        if (BooleanUtils.isTrue(partial)) {
            cutOffDate = calendar.getTime();
        }

        File inputPath = TaskUtils.getSummaryOutputPath();
        File[] files = inputPath.listFiles();
        if (files != null) {
            for (File file : files) {
                processStream(zipOutStream, inputPath.getAbsolutePath(), file, cutOffDate);
            }
        }
        zipOutStream.close();

        String encryptedFilename = StringUtils.join(Arrays.asList(filename, TaskConstants.FILE_TYPE_ENCRYPTED), ".");
        File encryptedOutFile = new File(TaskUtils.getEncryptedOutputPath(), encryptedFilename);
        ZipOutputStream encryptedZipOutStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(encryptedOutFile)));

        int count;
        byte[] data;
        // add the 16 bytes init vector for the cipher into the output stream
        String secretFilename = StringUtils.join(Arrays.asList(filename, TaskConstants.FILE_TYPE_SECRET), ".");
        ZipEntry ivZipEntry = new ZipEntry(secretFilename);
        encryptedZipOutStream.putNextEntry(ivZipEntry);
        // write the 16 bytes init vector for the cipher into the output stream
        AlgorithmParameters params = cipher.getParameters();
        byte[] initVector = params.getParameterSpec(IvParameterSpec.class).getIV();
        encryptedZipOutStream.write(initVector);
        // add the sample file entry
        String sampleFilename = StringUtils.join(Arrays.asList(filename, TaskConstants.FILE_TYPE_SAMPLE), ".");
        ZipEntry sampleZipEntry = new ZipEntry(sampleFilename);
        encryptedZipOutStream.putNextEntry(sampleZipEntry);
        // write the sample file
        data = new byte[TaskConstants.BUFFER_SIZE];
        String sampleText = "This is sample text inside encrypted document. " +
                "If you see this text, that means your decryption parameters is correct";
        InputStream inStream = new ByteArrayInputStream(sampleText.getBytes());
        CipherInputStream sampleCipherInStream = new CipherInputStream(inStream, cipher);
        while ((count = sampleCipherInStream.read(data, 0, TaskConstants.BUFFER_SIZE)) != -1) {
            encryptedZipOutStream.write(data, 0, count);
        }
        sampleCipherInStream.close();
        // add the zipped summaries
        ZipEntry zipEntry = new ZipEntry(zipFile.getName());
        encryptedZipOutStream.putNextEntry(zipEntry);
        // write the zipped summaries
        data = new byte[TaskConstants.BUFFER_SIZE];
        InputStream zipInStream = new BufferedInputStream(new FileInputStream(zipFile));
        CipherInputStream zipCipherInStream = new CipherInputStream(zipInStream, cipher);
        while ((count = zipCipherInStream.read(data, 0, TaskConstants.BUFFER_SIZE)) != -1) {
            encryptedZipOutStream.write(data, 0, count);
        }
        zipCipherInStream.close();
        encryptedZipOutStream.close();
    }

    private void processStream(ZipOutputStream zipOutputStream, String basePath, File currentFile, Date cutOffDate) throws Exception {
        byte data[] = new byte[TaskConstants.BUFFER_SIZE];
        if (currentFile.isDirectory()) {
            FileFilter fileFilter = new WildcardFileFilter(StringUtils.join(Arrays.asList("*", Evaluator.FILE_TYPE_XML), "."));
            File[] files = currentFile.listFiles(fileFilter);
            for (File file : files) {
                processStream(zipOutputStream, basePath, file, cutOffDate);
            }
        } else {
            if (cutOffDate == null || FileUtils.isFileNewer(currentFile, cutOffDate)) {
                processedFilename = currentFile.getName();
                // add the zip entry
                String zipEntryName = StringUtils.remove(currentFile.getAbsolutePath(), basePath);
                ZipEntry entry = new ZipEntry(zipEntryName);
                zipOutputStream.putNextEntry(entry);
                // write the entry
                int count;
                InputStream inStream = new BufferedInputStream(new FileInputStream(currentFile));
                while ((count = inStream.read(data, 0, TaskConstants.BUFFER_SIZE)) != -1) {
                    zipOutputStream.write(data, 0, count);
                }
                inStream.close();
            }
        }
    }

    /**
     * In order to correctly perform the encryption - decryption process, user must store their init vector table. This init vector will be given to the
     * user as a small file and it is required to perform the decryption process.
     *
     * @throws Exception
     */
    protected final void processInitVector() throws Exception {
        String secretFilename = StringUtils.join(Arrays.asList(filename, TaskConstants.FILE_TYPE_SECRET), ".");
        String encryptedFilename = StringUtils.join(Arrays.asList(filename, TaskConstants.FILE_TYPE_ENCRYPTED), ".");
        // get the zip file
        File encryptedFile = new File(TaskUtils.getEncryptedOutputPath(), encryptedFilename);
        ZipOutputStream encryptedOutStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(encryptedFile)));
        // add the 16 bytes init vector for the cipher into the output stream
        ZipEntry ivZipEntry = new ZipEntry(secretFilename);
        encryptedOutStream.putNextEntry(ivZipEntry);
        // write the 16 bytes init vector for the cipher into the output stream
        AlgorithmParameters params = cipher.getParameters();
        byte[] initVector = params.getParameterSpec(IvParameterSpec.class).getIV();
        encryptedOutStream.write(initVector);
        encryptedOutStream.close();
    }

    /**
     * @see SummariesTask#process()
     */
    @Override
    protected void process() throws Exception {
        setStatus(TaskStatus.TASK_RUNNING_DOWNLOAD);
        prepareDatabaseProperties();
        initializeCipher();
        processIndex();
        processSummaries();
    }
}
