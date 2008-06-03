<%@ include file="/WEB-INF/template/include.jsp"%>

<%@page import="org.apache.fop.area.Page"%>
<openmrs:require privilege="View Clinical Summary" otherwise="/login.htm" redirect="/module/clinicalsummary/queueDirectory.list" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp" %>
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<h2><spring:message code="clinicalsummary.directory.title" /></h2>

<form id="filterOptions" action="" method="post">
	<c:forEach var="itemList" items="${queueItems}" varStatus="listVarStatus">
	<b class="boxHeader">
		<c:if test="${listVarStatus.index == 0}"><spring:message code="clinicalsummary.directory.generated"/></c:if>
		<c:if test="${listVarStatus.index == 1}"><spring:message code="clinicalsummary.directory.printed"/></c:if>
	</b>
	<div class="box">
		<table width="90%" cellpadding="2" cellspacing="0">
			<c:forEach var="item" items="${itemList}" varStatus="itemVarStatus">
			<tr>
				<td>
					<c:if test="${item.directory == true}">${item}</c:if>
				</td>
			</tr>
			</c:forEach> 
		</table>
	</div>
	<br/>
	</c:forEach> 
</form>
<%@ include file="/WEB-INF/template/footer.jsp"%>

