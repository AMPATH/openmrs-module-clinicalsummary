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
package org.openmrs.module.clinicalsummary;

import java.util.HashSet;
import java.util.Set;

import org.openmrs.BaseOpenmrsMetadata;
import org.openmrs.EncounterType;
import org.openmrs.util.OpenmrsUtil;

/**
 * An object that will hold a template to generate a summary for a patient. There are two main
 * components of a template.
 * <ul>
 * <li>template, is a velocity template string to generate the xml metadata of a summary.</li>
 * <li>xslt, is an xslt string to generate the summary file. To generate the pdf file, we use <a
 * href="@linkplain http://xmlgraphics.apache.org/fop/">Apache FOP</a>. So, the xslt string is not
 * just regular xslt, but also xslt with fop in it.</li>
 * </ul>
 * By default, the summary module will pass the logic service to the velocity evaluation context. In
 * the template, user can call the logic service using:
 * 
 * <pre>
 * ${functions.eval(String)}
 * </pre>
 * 
 * <pre>
 * ${functions.parse(String)}
 * </pre>
 * 
 * TODO: User can also register custom class that contains custom functions to the velocity
 * evaluation context.
 */
public class SummaryTemplate extends BaseOpenmrsMetadata {
	
	private Integer templateId;
	
	private String name;
	
	private String description;
	
	private String template;
	
	private String xslt;
	
	private Boolean preferred = false;
	
	private Set<EncounterType> encounterTypes;
	
	private MappingPosition position;
	
	private Integer revision;
	
	/**
	 * @return the name
	 */
	@Override
	public String getName() {
		return name;
	}
	
	/**
	 * @param name the name to set
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the description
	 */
	@Override
	public String getDescription() {
		return description;
	}
	
	/**
	 * @param description the description to set
	 */
	@Override
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * @return the templateId
	 */
	public Integer getTemplateId() {
		return templateId;
	}
	
	/**
	 * @param templateId the templateId to set
	 */
	public void setTemplateId(Integer templateId) {
		this.templateId = templateId;
	}
	
	public Boolean isPreferred() {
		return getPreferred();
	}
	
	/**
	 * @return the preferred
	 */
	public Boolean getPreferred() {
		return preferred;
	}
	
	/**
	 * @param preferred the preferred to set
	 */
	public void setPreferred(Boolean preferred) {
		this.preferred = preferred;
	}
	
	/**
	 * @return the template
	 */
	public String getTemplate() {
		return template;
	}
	
	/**
	 * @param template the template to set
	 */
	public void setTemplate(String template) {
		this.template = template;
	}
	
	/**
	 * @return the xslt
	 */
	public String getXslt() {
		return xslt;
	}
	
	/**
	 * @param xslt the xslt to set
	 */
	public void setXslt(String xslt) {
		this.xslt = xslt;
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		return getTemplateId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setTemplateId(id);
	}
	
	/**
	 * @return the encounterTypes
	 */
	public Set<EncounterType> getEncounterTypes() {
		if (encounterTypes == null)
			encounterTypes = new HashSet<EncounterType>();
		return encounterTypes;
	}
	
	/**
	 * @param encounterTypes the encounterTypes to set
	 */
	public void setEncounterTypes(Set<EncounterType> encounterTypes) {
		this.encounterTypes = encounterTypes;
	}
	
	/**
	 * @param encounterType
	 */
	public void addEncounterType(EncounterType encounterType) {
		
		if (encounterType != null) {
			if (encounterTypes == null)
				encounterTypes = new HashSet<EncounterType>();
			if (!OpenmrsUtil.collectionContains(encounterTypes, encounterType))
				encounterTypes.add(encounterType);
		}
	}
	
	/**
	 * @param encounterType
	 */
	public void removeEncounterType(EncounterType encounterType) {
		
		if (encounterType != null)
			encounterTypes.remove(encounterType);
	}
	
	/**
	 * Return the value of the position
	 * 
	 * @return the position
	 */
	public MappingPosition getPosition() {
		return position;
	}
	
	/**
	 * Set the position with the position value
	 * 
	 * @param position the position to set
	 */
	public void setPosition(MappingPosition position) {
		this.position = position;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj instanceof SummaryTemplate) {
	        SummaryTemplate template = (SummaryTemplate) obj;
	        if (getTemplateId().equals(template.getTemplateId()))
	        	return true;
        }
		return super.equals(obj);
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = 8;
		hash = 31 * templateId.hashCode() + hash;
		return hash;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SummaryTemplate: " + name;
	}
	
	/**
	 * Return the value of the revision
	 * 
	 * @return the revision
	 */
	public Integer getRevision() {
		return revision;
	}
	
	/**
	 * Set the revision with the revision value
	 * 
	 * @param revision the revision to set
	 */
	public void setRevision(Integer revision) {
		this.revision = revision;
	}
	
}
