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
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.clinicalsummary.enumeration.TaskStatus;
import org.openmrs.module.clinicalsummary.io.utils.TaskConstants;
import org.openmrs.module.clinicalsummary.io.utils.TaskUtils;
import org.springframework.util.FileCopyUtils;

/**
 *
 */
class UploadSummariesTask extends SummariesTask {

    private static final int IV_SIZE = 16;

    private static final Log log = LogFactory.getLog(UploadSummariesTask.class);

    private byte[] initVector;

    public UploadSummariesTask(final String password, final String filename) {
        super(password, filename);
    }

    /**
     * In order to correctly perform the encryption - decryption process, user must store their init vector table. This init vector will be given to the
     * user as a small file and it is required to perform the decryption process.
     *
     * @throws Exception
     */
    protected void processInitVector() throws Exception {
        String encryptedFilename = StringUtils.join(Arrays.asList(filename, TaskConstants.FILE_TYPE_ENCRYPTED), ".");
        ZipFile encryptedFile = new ZipFile(new File(TaskUtils.getEncryptedOutputPath(), encryptedFilename));

        Enumeration<? extends ZipEntry> entries = encryptedFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            if (zipEntry.getName().endsWith(TaskConstants.FILE_TYPE_SECRET)) {
                InputStream inputStream = encryptedFile.getInputStream(zipEntry);
                initVector = FileCopyUtils.copyToByteArray(inputStream);
                if (initVector.length != IV_SIZE) {
                    throw new Exception("Secret file is corrupted or invalid secret file are being used.");
                }
            }
        }
    }

    /**
     * Method to initialize the cipher object with the correct encryption algorithm.
     *
     * @throws Exception
     */
    protected void initializeCipher() throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(TaskConstants.SECRET_KEY_FACTORY);
        KeySpec spec = new PBEKeySpec(password.toCharArray(), password.getBytes(), 1024, 128);
        SecretKey tmp = factory.generateSecret(spec);

        if (log.isDebugEnabled())
            log.debug("Secret Key Length: " + tmp.getEncoded().length);

        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), TaskConstants.KEY_SPEC);

        cipher = Cipher.getInstance(TaskConstants.CIPHER_CONFIGURATION);
        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(initVector));
    }

    /**
     * Method that will be called to process the summary collection file. The upload process will unpack the zipped collection of summary files and then
     * decrypt them.
     *
     * @throws Exception
     */
    protected void processSummaries() throws Exception {
        String zipFilename = StringUtils.join(Arrays.asList(filename, TaskConstants.FILE_TYPE_ZIP), ".");
        String encryptedFilename = StringUtils.join(Arrays.asList(filename, TaskConstants.FILE_TYPE_ENCRYPTED), ".");

        File file = new File(TaskUtils.getZippedOutputPath(), zipFilename);
        OutputStream outStream = new BufferedOutputStream(new FileOutputStream(file));
        ZipFile encryptedFile = new ZipFile(new File(TaskUtils.getEncryptedOutputPath(), encryptedFilename));

        Enumeration<? extends ZipEntry> entries = encryptedFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            String zipEntryName = zipEntry.getName();
            if (!zipEntryName.endsWith(TaskConstants.FILE_TYPE_SAMPLE)
                    && !zipEntryName.endsWith(TaskConstants.FILE_TYPE_SECRET)) {
                int count;
                byte[] data = new byte[TaskConstants.BUFFER_SIZE];
                CipherInputStream zipCipherInputStream = new CipherInputStream(encryptedFile.getInputStream(zipEntry), cipher);
                while ((count = zipCipherInputStream.read(data, 0, TaskConstants.BUFFER_SIZE)) != -1) {
                    outStream.write(data, 0, count);
                }
                zipCipherInputStream.close();
            }
        }
        outStream.close();

        File outputPath = TaskUtils.getSummaryOutputPath();
        ZipFile zipFile = new ZipFile(file);
        Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
        while (zipEntries.hasMoreElements()) {
            ZipEntry zipEntry = zipEntries.nextElement();
            processedFilename = zipEntry.getName();
            File f = new File(outputPath, zipEntry.getName());
            // ensure that the parent path exists
            File parent = f.getParentFile();
            if (parent.exists() || parent.mkdirs()) {
                FileCopyUtils.copy(zipFile.getInputStream(zipEntry), new BufferedOutputStream(new FileOutputStream(f)));
            }
        }
    }

    /**
     * Method that will be called to process the index file. Index file contains sql statement for the summary sheet index table in the database. The
     * upload process will execute mysql to load back into the database. To execute mysql correctly, the database user must have RELOAD privilege
     *
     * @throws Exception
     */
    protected void processIndex() throws Exception {
        File indexFile = new File(TaskUtils.getSummaryOutputPath(), TaskConstants.INDEX_CONFIGURATION_SQL_FILE);

        String path = indexFile.getAbsolutePath();
        path = path.replace("\\", "/");

        String[] commands = {"mysql", "-e", "source " + path, "-f", "-u" + databaseUser, "-p" + databasePassword, "-h" + databaseHost,
                "-P" + databasePort, "-D" + databaseName};

        TaskUtils.executeCommand(TaskUtils.getSummaryOutputPath(), commands);
    }

    /**
     * @see SummariesTask#process()
     */
    @Override
    protected void process() throws Exception {
        setStatus(TaskStatus.TASK_RUNNING_UPLOAD);
        prepareDatabaseProperties();
        processInitVector();
        initializeCipher();
        processSummaries();
        processIndex();
    }
}
