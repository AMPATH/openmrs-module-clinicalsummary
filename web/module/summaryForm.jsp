<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Manage Clinical Summaries" otherwise="/login.htm" redirect="/module/clinicalsummary/summary.form" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp" %>

<style>
	th { text-align: left; }
</style>

<h2>
	<spring:message code="clinicalsummary.editing" />
</h2>

<spring:hasBindErrors name="summary">
	<spring:message code="fix.error" />
	<br />
	<!-- ${errors} -->
</spring:hasBindErrors>
<form method="post" action="">

	<table>
		<spring:bind path="summary.name">
			<tr>
				<th><spring:message code="general.name"/></th>
				<td><input type="text" name="${status.expression}" value="${status.value}" size="43" /></td>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</tr>
		</spring:bind>
		<spring:bind path="summary.description">
			<tr>
				<th valign="top"><spring:message code="general.description"/></th>
				<td><textarea name="${status.expression}" cols="41" rows="3">${status.value}</textarea></td>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</tr>
		</spring:bind>
		<spring:bind path="summary.preferred">
			<tr>
				<th><spring:message code="general.preferred" /></th>
				<td>
					<input type="hidden" name="_${status.expression}">
					<input type="checkbox" name="${status.expression}" id="${status.expression}" value="on" 
						<c:if test="${status.value == true}">checked</c:if> />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
					<i><spring:message code="clinicalsummary.preferred.help"/></i>
				</td>
			</tr>
		</spring:bind>
		<spring:bind path="summary.template">
			<tr>
				<th valign="top"><spring:message code="clinicalsummary.template"/></th>
				<td><textarea name="${status.expression}" cols="115" rows="15"><c:out value="${status.value}" escapeXml="true" /></textarea></td>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</tr>
		</spring:bind>
		<spring:bind path="summary.xslt">
			<tr>
				<th valign="top"><spring:message code="clinicalsummary.xslt"/></th>
				<td><textarea name="${status.expression}" cols="115" rows="15"><c:out value="${status.value}" escapeXml="true" /></textarea></td>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</tr>
		</spring:bind>
		<c:if test="${summary.creator != null}">
			<tr>
				<td><spring:message code="general.createdBy" /></td>
				<td>
					${summary.creator.firstName} ${summary.creator.lastName} -
					<openmrs:formatDate date="${summary.dateCreated}" type="long" />
				</td>
			</tr>
		</c:if>
		<c:if test="${summary.changedBy != null}">
			<tr>
				<td><spring:message code="general.changedBy" /></td>
				<td>
					${summary.changedBy.firstName} ${summary.changedBy.lastName} -
					<openmrs:formatDate date="${summary.dateChanged}" type="long" />
				</td>
			</tr>
		</c:if>
	</table>

	<br />
	<input type="submit" value='<spring:message code="clinicalsummary.save"/>'>
</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
