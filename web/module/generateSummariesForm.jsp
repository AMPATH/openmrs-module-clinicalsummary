<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="View Clinical Summary" otherwise="/login.htm" redirect="/module/clinicalsummary/summary.list" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="clinicalsummary.title" /></h2>

<spring:message code="clinicalsummary.generateSummaries.instructions"/>

<form method="post" action="${pageContext.request.contextPath}/moduleServlet/clinicalsummary/generate">
	<table>
		<tr>
			<td><spring:message code="clinicalsummary.location"/></td>
			<td>
				<openmrs:fieldGen type="org.openmrs.Location" formFieldName="locationId" val="" parameters="optionHeader=[blank]" />
			</td>
		</tr>
		
		<tr>
			<td valign="top"><spring:message code="clinicalsummary.patientId"/></td>
			<td valign="top">
				<input type="text" name="patientId"/>
				<i><spring:message code="clinicalsummary.patientId.help"/></i>
			</td>
		</tr>
		
		<tr>
			<td valign="top"><spring:message code="clinicalsummary.patientIds"/></td>
			<td valign="top">
				<textarea name="patientIds">${patientIds}</textarea>
				<i><spring:message code="clinicalsummary.patientIds.help"/></i>
			</td>
		</tr>
		
		<tr>
			<td valign="top"><spring:message code="clinicalsummary.patientIdentifiers"/></td>
			<td valign="top">
				<textarea name="patientIdentifiers"></textarea>
				<i><spring:message code="clinicalsummary.patientIdentifiers.help"/></i>
			</td>
		</tr>
		<tr>
			<td><spring:message code="Cohort.title"/></td>
			<td>
				<select name="cohortId">
					<option value=""></option>
					<openmrs:forEachRecord name="cohort">
						<option value="${record.cohortId}">${record.name}</option>
					</openmrs:forEachRecord>
				</select>
			</td>
		</tr>
		<tr>
			<td colspan="2" align="center"><spring:message code="general.andOr"/></td>
		</tr>
		<tr>
			<td><spring:message code="CohortDefinition.title"/></td>
			<td>
				<select name="cohortDefinitionId">
					<option value=""></option>
					<openmrs:forEachRecord name="reportObject" reportObjectType="Patient Filter">
						<option value="${record.reportObjectId}">${record.name}</option>
					</openmrs:forEachRecord>
				</select>
			</td>
		</tr>
		
		<tr><td colspan="2"><br/></td></tr>
		
		<tr>
			<td valign="top"><spring:message code="clinicalsummary.choose"/></td>
			<td valign="top">
				<select name="clinicalSummaryId">
					<c:forEach var="summary" items="${summaries}" varStatus="varStatus">
						<option value="${summary.clinicalSummaryId}"
							<c:if test="${summary.clinicalSummaryId == preferredSummary.clinicalSummaryId}">selected='true'</c:if> >
							${summary.name}
						</option>
					</c:forEach>
				</select>
			</td>
		</tr>
		
	</table>
	
	<input type="submit" value="<spring:message code="clinicalsummary.generate"/>"/>
</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
