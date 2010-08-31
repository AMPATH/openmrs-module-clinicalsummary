package org.openmrs.module.clinicalsummary.deprecated;

import java.util.Date;

import org.openmrs.User;

public class ClinicalSummary {
	
	private Integer clinicalSummaryId;
	private String name;
	private String description;
	private String template;
	private String xslt;
	private Boolean preferred = false;
	private User creator;
	private Date dateCreated;
	private User changedBy;
	private Date dateChanged;
	
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the clinicalSummaryId
	 */
	public Integer getClinicalSummaryId() {
		return clinicalSummaryId;
	}
	/**
	 * @param clinicalSummaryId the clinicalSummaryId to set
	 */
	public void setClinicalSummaryId(Integer clinicalSummaryId) {
		this.clinicalSummaryId = clinicalSummaryId;
	}
	/**
	 * @return the creator
	 */
	public User getCreator() {
		return creator;
	}
	/**
	 * @param creator the creator to set
	 */
	public void setCreator(User creator) {
		this.creator = creator;
	}
	/**
	 * @return the changedBy
	 */
	public User getChangedBy() {
		return changedBy;
	}
	/**
	 * @param changedBy the changedBy to set
	 */
	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}
	/**
	 * @return the dateCreated
	 */
	public Date getDateCreated() {
		return dateCreated;
	}
	/**
	 * @param dateCreated the dateCreated to set
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	/**
	 * @return the dateChanged
	 */
	public Date getDateChanged() {
		return dateChanged;
	}
	/**
	 * @param dateChanged the dateChanged to set
	 */
	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
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
	
	
}
