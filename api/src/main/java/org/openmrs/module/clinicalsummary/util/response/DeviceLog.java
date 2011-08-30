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

package org.openmrs.module.clinicalsummary.util.response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Person;

public class DeviceLog extends BaseOpenmrsData {

	private static final Log log = LogFactory.getLog(DeviceLog.class);

	private Integer id;

	private String deviceId;

	private String key;

	private String value;

	private String timestamp;

	private Person user;

	/**
	 * @return id - The unique Identifier for the object
	 */
	@Override
	public Integer getId() {
		return id;
	}

	/**
	 * @param id - The unique Identifier for the object
	 */
	@Override
	public void setId(final Integer id) {
		this.id = id;
	}

	/**
	 * @return
	 */
	public String getDeviceId() {
		return deviceId;
	}

	/**
	 * @param deviceId
	 */

	public void setDeviceId(final String deviceId) {
		this.deviceId = deviceId;
	}

	/**
	 * @return
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key
	 */
	public void setKey(final String key) {
		this.key = key;
	}

	/**
	 * @return
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value
	 */
	public void setValue(final String value) {
		this.value = value;
	}

	/**
	 * @return
	 */
	public String getTimestamp() {
		return timestamp;
	}

	/**
	 * @param timestamp
	 */
	public void setTimestamp(final String timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * @return
	 */
	public Person getUser() {
		return user;
	}

	/**
	 * @param user
	 */
	public void setUser(final Person user) {
		this.user = user;
	}
}
