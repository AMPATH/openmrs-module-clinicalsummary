<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Print Summaries" otherwise="/login.htm" redirect="/module/clinicalsummary/printer/printSummaries.form"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="../localHeader.jsp" %>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js"/>
<openmrs:htmlInclude file="/moduleResources/clinicalsummary/css/form.css"/>

<spring:message code="clinicalsummary.print.printInstructions"/>

<div id="container">

	<h3 id="header"><spring:message code="clinicalsummary.print.printing"/></h3>

	<div id="main">
		<div id="leftcontent">
			<form method="post" action="">
				<fieldset>
					<legend><spring:message code="clinicalsummary.print.byLocation" arguments="Print"/></legend>
					<ol>
						<li>
							<label for="summaryLocation"><spring:message code="clinicalsummary.print.summary"/></label>
							<select name="summaryLocation" id="summaryLocation">
								<c:forEach var="summary" items="${summaries}" varStatus="varStatus">
									<option value="${summary.id}">${summary.name}</option>
								</c:forEach>
							</select>
						</li>
						<li>
							<label for="locationId"><spring:message code="clinicalsummary.print.location"/></label>
							<openmrs:fieldGen type="org.openmrs.Location" formFieldName="locationId" val="" parameters="optionHeader=[blank]"/>
						</li>
						<li>
							<label for="startReturnDate"><spring:message code="clinicalsummary.print.startReturnDate"/></label>
							<input type="text" name="startReturnDate" size="10" id="startReturnDate" onClick="showCalendar(this)"/>
						</li>
						<li>
							<label for="endReturnDate"><spring:message code="clinicalsummary.print.endReturnDate"/></label>
							<input type="text" name="endReturnDate" size="10" id="endReturnDate" onClick="showCalendar(this)"/>
						</li>
						<li />
						<li>
							<input type="submit" value="<spring:message code="clinicalsummary.print"/>"/>
						</li>
					</ol>
				</fieldset>
			</form>
		</div>

		<div id="rightcontent">
			<form method="post" action="">
				<fieldset>
					<legend><spring:message code="clinicalsummary.print.byIdentifiers" arguments="Print"/></legend>
					<ol>
						<li>
							<label for="summaryIdentifier"><spring:message code="clinicalsummary.print.summary"/></label>
							<select name="summaryIdentifier" id="summaryIdentifier">
								<c:forEach var="summary" items="${summaries}" varStatus="varStatus">
									<option value="${summary.id}">${summary.name}</option>
								</c:forEach>
							</select>
						</li>
						<li>
							<label for="patientIdentifiers"><spring:message code="clinicalsummary.print.patientIdentifiers"/></label>
							<textarea id="patientIdentifiers" name="patientIdentifiers" row="24" cols="75"></textarea>
							<br />
							<i><spring:message code="clinicalsummary.patientIdentifiers.help"/></i>
						</li>
						<li />
						<li>
							<input type="submit" value="<spring:message code="clinicalsummary.print"/>"/>
						</li>
					</ol>
				</fieldset>
			</form>
		</div>
		<div id="clear"></div>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>
