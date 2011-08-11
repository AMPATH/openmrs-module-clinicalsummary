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

package org.openmrs.module.clinicalsummary.web.controller.service.xstream;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonName;

/**
 */
public class PatientConverter implements Converter {

	private static final Log log = LogFactory.getLog(PatientConverter.class);

	/**
	 * Convert an object to textual data.
	 *
	 * @param source
	 * 		The object to be marshall-ed.
	 * @param writer
	 * 		A stream to write to.
	 * @param context
	 * 		A context that allows nested objects to be processed by XStream.
	 */
	public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
		Patient patient = (Patient) source;
		// write patient internal id
		writer.startNode("id");
		writer.setValue(patient.getId() != null ? String.valueOf(patient.getId()) : StringUtils.EMPTY);
		writer.endNode();

		// write identifier if it's not null
		writer.startNode("identifier");
		context.convertAnother(patient.getPatientIdentifier());
		writer.endNode();

		// write name if it's not null
		writer.startNode("name");
		context.convertAnother(patient.getPersonName());
		writer.endNode();
	}

	/**
	 * Convert textual data back into an object.
	 *
	 * @param reader
	 * 		The stream to read the text from.
	 * @param context
	 *
	 * @return The resulting object. This method will return partially complete Patient object
	 */
	public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
		Patient patient = new Patient();
		// read the internal id
		reader.moveDown();
		patient.setId(NumberUtils.toInt(reader.getValue()));
		reader.moveUp();

		// read the identifier
		reader.moveDown();
		PatientIdentifier patientIdentifier = (PatientIdentifier) context.convertAnother(patient, PatientIdentifier.class);
		patient.addIdentifier(patientIdentifier);
		reader.moveUp();

		// read the person name
		reader.moveDown();
		PersonName personName = (PersonName) context.convertAnother(patient, PersonName.class);
		patient.addName(personName);
		reader.moveUp();
		return patient;
	}

	/**
	 * Determines whether the converter can marshall a particular type.
	 *
	 * @param type
	 * 		the Class representing the object type to be converted
	 */
	public boolean canConvert(final Class type) {
		return type.equals(Patient.class);
	}
}
