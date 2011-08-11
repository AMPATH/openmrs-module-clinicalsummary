<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Summaries" otherwise="/login.htm" redirect="/module/clinicalsummary/summary/summaryList.list"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="../localHeader.jsp" %>

<div id="container">
	<b class="boxHeader">
		<spring:message code="clinicalsummary.summary"/>
	</b>

	<div class="box">
		<table width="90%">
			<tr>
				<th><spring:message code="general.name"/></th>
				<th><spring:message code="clinicalsummary.summary.autogenerate"/></th>
				<th><spring:message code="general.createdBy"/></th>

			</tr>
			<c:forEach var="summary" items="${summaries}" varStatus="varStatus">
				<tr>
					<td><a href="summaryForm.form?id=${summary.id}">${summary.name}</a></td>
					<td><c:if test="${summary.autoGenerate}"><spring:message code="general.yes"/></c:if></td>
					<td>${summary.creator.personName} - <openmrs:formatDate date="${summary.dateCreated}" type="medium"/></td>
				</tr>
			</c:forEach>
		</table>
	</div>
</div>

<br/>
<a href="summaryForm.form"><spring:message code="clinicalsummary.summary.add"/></a>

<%@ include file="/WEB-INF/template/footer.jsp" %>
