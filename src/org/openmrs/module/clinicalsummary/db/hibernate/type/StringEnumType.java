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
package org.openmrs.module.clinicalsummary.db.hibernate.type;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;

/**
 * Class for custom EnumerationType. Based on Dave Thomas in the Dev mailing list and
 * http://community.jboss.org/wiki/Java5StringValuedEnumUserType
 */
public class StringEnumType<T extends Enum<T> & StringEnum> implements UserType, ParameterizedType {
	
	private Class<T> enumClass;
	
	private String defaultValue;
	
	/**
	 * Return the value of the defaultValue
	 * 
	 * @return the defaultValue
	 */
	private final String getDefaultValue() {
		return defaultValue;
	}
	
	/**
	 * Set the defaultValue with the defaultValue value
	 * 
	 * @param defaultValue the defaultValue to set
	 */
	private final void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public Class<?> returnedClass() {
		return enumClass;
	}
	
	public int[] sqlTypes() {
		return new int[] { Types.VARCHAR };
	}
	
	public boolean isMutable() {
		return false;
	}
	
	public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
		String value = rs.getString(names[0]);
		if (StringUtils.isEmpty(value))
			value = getDefaultValue();
		String name = StringEnumReflector.getNameFromValue(enumClass, value);
		return rs.wasNull() ? null : Enum.valueOf(enumClass, name);
	}
	
	public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
		if (value == null)
			st.setNull(index, Types.VARCHAR);
		else
			st.setString(index, ((StringEnum) value).getValue());
	}
	
	public Object assemble(Serializable cached, Object owner) throws HibernateException {
		return cached;
	}
	
	public Serializable disassemble(Object value) throws HibernateException {
		return (Serializable) value;
	}
	
	public Object deepCopy(Object value) throws HibernateException {
		return value;
	}
	
	public boolean equals(Object x, Object y) throws HibernateException {
		return x == y;
	}
	
	public int hashCode(Object x) throws HibernateException {
		return x.hashCode();
	}
	
	public Object replace(Object original, Object target, Object owner) throws HibernateException {
		return original;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setParameterValues(Properties parameters) {
		String enumClassName = parameters.getProperty("enum");
		if (enumClassName == null) {
			throw new MappingException("enum class parameter not specified ...");
		}
		
		try {
			this.enumClass = (Class<T>) Class.forName(enumClassName);
		}
		catch (ClassNotFoundException e) {
			throw new MappingException("enumClass " + enumClassName + " not found", e);
		}
		
		setDefaultValue(parameters.getProperty("defaultValue"));
	}
	
}
