<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Summaries" otherwise="/login.htm" redirect="/module/clinicalsummary/summary/mappingList.list"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="../localHeader.jsp" %>

<div id="container">
	<b class="boxHeader">
		<spring:message code="clinicalsummary.mapping"/>
	</b>

	<div class="box">
		<table width="90%">
			<tr>
				<th><spring:message code="clinicalsummary.mapping.id"/></th>
				<th><spring:message code="clinicalsummary.mapping.summary"/></th>
				<th><spring:message code="clinicalsummary.mapping.encounterType"/></th>
				<th><spring:message code="clinicalsummary.mapping.mappingType"/></th>

			</tr>
			<c:forEach var="mapping" items="${mappings}" varStatus="varStatus">
				<tr>
					<td><a href="mappingForm.form?id=${mapping.id}">${mapping.id}</a></td>
					<td>${mapping.summary.name}</td>
					<td>${mapping.encounterType.name}</td>
					<td>${mapping.mappingType.value}</td>
				</tr>
			</c:forEach>
		</table>
	</div>
</div>

<br/>
<a href="mappingForm.form"><spring:message code="clinicalsummary.mapping.add"/></a>

<%@ include file="/WEB-INF/template/footer.jsp" %>
