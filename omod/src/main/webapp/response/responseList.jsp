<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Summaries" otherwise="/login.htm" redirect="/module/clinicalsummary/response/responseList.list"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="../localHeader.jsp" %>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js"/>
<openmrs:htmlInclude file="/moduleResources/clinicalsummary/css/form.css"/>

<div id="container">
	<h3 id="header"><spring:message code="clinicalsummary.response.header"/></h3>

	<div id="main">
		<div id="sidebar">
			<form method="post" action="">
				<fieldset>
					<ol>
						<li>
							<label for="locationId"><spring:message code="clinicalsummary.response.location"/></label>
							<openmrs:fieldGen type="org.openmrs.Location" formFieldName="locationId" val="" parameters="optionHeader=[blank]"/>
						</li>
						<li>
							<label for="displayType"><spring:message code="clinicalsummary.response.displayType"/></label>
							<select name="displayType" id="displayType">
								<c:forEach var="displayType" items="${displayTypes}" varStatus="varStatus">
									<option value="${displayType}">${displayType.value}</option>
								</c:forEach>
							</select>
						</li>
						<li />
						<li>
							<input id="submit" type="submit" value="<spring:message code="clinicalsummary.response"/>"/>
						</li>
					</ol>
				</fieldset>
			</form>
		</div>
		<div id="rightcontent">
		</div>
		<div id="clear"></div>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>
