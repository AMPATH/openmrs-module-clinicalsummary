<%@ include file="/WEB-INF/template/include.jsp" %>
<openmrs:require privilege="Manage Summaries" otherwise="/login.htm" redirect="/module/clinicalsummary/response/reminderResponseList.list"/>


<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="../localHeader.jsp" %>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js"/>
<openmrs:htmlInclude file="/moduleResources/clinicalsummary/css/form.css"/>

<div id="container">
	<h3 id="header"><spring:message code="clinicalsummary.response.title"/></h3>

	<div id="main">
		<div id="sidebar">
			<form id="form" method="post" action="">
				<fieldset>
					<ol>
						<li>
							<label for="patientIdentifier"><spring:message code="clinicalsummary.response.reminder.patients"/></label>
							<textarea id="patientIdentifier" name="locations" row="40" cols="35"></textarea>
						</li>
						<li/>
						<li>
							<input type="button" id="search" value="<spring:message code="clinicalsummary.response.reminder.search"/>"/>
						</li>
					</ol>
				</fieldset>
			</form>
		</div>
		<div id="maincontent">
			<table cellpadding="0" cellspacing="0" border="0" class="display" id="result" width="100%">
				<tbody id="searchResult"></tbody>
			</table>
		</div>
		<div id="clear">
		</div>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>
