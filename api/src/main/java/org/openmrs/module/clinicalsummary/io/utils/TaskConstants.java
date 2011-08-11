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

package org.openmrs.module.clinicalsummary.io.utils;

/**
 */
public interface TaskConstants {

	String FILE_TYPE_ZIP = "zip";

	String FILE_TYPE_SECRET = "secret";

	// encryption parameters
	String SECRET_KEY_FACTORY = "PBKDF2WithHmacSHA1";

	String CIPHER_CONFIGURATION = "AES/CBC/PKCS5Padding";

	String KEY_SPEC = "AES";

	int BUFFER_SIZE = 4096;

	String INDEX_CONFIGURATION_SQL_FILE = "summaryindex.sql";
}
