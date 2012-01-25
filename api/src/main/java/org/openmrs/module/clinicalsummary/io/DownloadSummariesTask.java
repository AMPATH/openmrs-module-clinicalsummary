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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import org.springframework.util.FileCopyUtils;

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
        String zippedFilename = StringUtils.join(Arrays.asList(filename, TaskConstants.FILE_TYPE_ZIP), ".");
        FileOutputStream outputStream = new FileOutputStream(new File(TaskUtils.getZippedOutputPath(), zippedFilename));
        ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(outputStream));
        
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        Date cutOffDate = null;
        if (BooleanUtils.isTrue(partial))
            cutOffDate = calendar.getTime();

        File inputPath = TaskUtils.getSummaryOutputPath();
        File[] files = inputPath.listFiles();
        for (File file : files)
            processStream(zipOutputStream, inputPath.getAbsolutePath(), file, cutOffDate);

        zipOutputStream.close();
    }

    private void processStream(ZipOutputStream zipOutputStream, String basePath, File currentFile, Date cutOffDate) throws Exception {
        byte data[] = new byte[TaskConstants.BUFFER_SIZE];
        if (currentFile.isDirectory()) {
            FileFilter fileFilter = new WildcardFileFilter(StringUtils.join(Arrays.asList("*", Evaluator.FILE_TYPE_PDF), "."));
            File[] files = currentFile.listFiles(fileFilter);
            for (File file : files)
                processStream(zipOutputStream, basePath, file, cutOffDate);
        } else {
            if (cutOffDate == null || FileUtils.isFileNewer(currentFile, cutOffDate)) {
                processedFilename = currentFile.getName();

                FileInputStream inputStream = new FileInputStream(currentFile);
                CipherInputStream origin = new CipherInputStream(inputStream, cipher);

                String parentPath = currentFile.getParent();
                String zipParentPath = StringUtils.remove(parentPath, basePath);
                String zipEntryName = currentFile.getName();
                if (StringUtils.isNotEmpty(zipParentPath))
                    zipEntryName = StringUtils.join(Arrays.asList(zipParentPath, currentFile.getName()), File.separator);

                ZipEntry entry = new ZipEntry(zipEntryName);
                zipOutputStream.putNextEntry(entry);

                int count;
                while ((count = origin.read(data, 0, TaskConstants.BUFFER_SIZE)) != -1)
                    zipOutputStream.write(data, 0, count);

                origin.close();
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
        AlgorithmParameters params = cipher.getParameters();
        byte[] initVector = params.getParameterSpec(IvParameterSpec.class).getIV();
        String secretFilename = StringUtils.join(Arrays.asList(filename, TaskConstants.FILE_TYPE_SECRET), ".");
        FileCopyUtils.copy(initVector, new File(TaskUtils.getSecretOutputPath(), secretFilename));
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
        processInitVector();
    }
}
