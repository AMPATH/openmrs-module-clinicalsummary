<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Manage Clinical Summaries" otherwise="/login.htm" redirect="/module/clinicalsummary/summary.list" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="clinicalsummary.title" /></h2>

<a href="summary.form"><spring:message code="clinicalsummary.add" /></a>
<br />
<br />

<div id="summarylist">
	<b class="boxHeader">
		<spring:message code="clinicalsummary.choose" />
	</b>
	<div class="box">
		<table width="90%">
			<tr>
				<th><spring:message code="general.name" /></th>
				<th><spring:message code="general.description" /></th>
				<th><spring:message code="general.preferred" /></th>
				<th><spring:message code="general.createdBy" /></th>
				
			</tr>
		<c:forEach var="summary" items="${summaries}" varStatus="varStatus">
			<tr>
				<td><a href="summary.form?clinicalSummaryId=${summary.clinicalSummaryId}">${summary.name}</a></td>
				<td>${summary.description}</td>
				<td><c:if test="${summary.preferred}"><spring:message code="general.yes" /></c:if></td>
				<td>${summary.creator.firstName} ${summary.creator.lastName} - <openmrs:formatDate date="${summary.dateCreated}" type="medium" /></td>
			</tr>
		</c:forEach>
		</table>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>
