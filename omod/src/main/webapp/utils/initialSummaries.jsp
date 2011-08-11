<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Summaries" otherwise="/login.htm" redirect="/module/clinicalsummary/utils/initialSummaries.form"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="../localHeader.jsp" %>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js"/>
<openmrs:htmlInclude file="/moduleResources/clinicalsummary/css/form.css"/>
<script type="text/javascript">

	$j = jQuery.noConflict();

	$j(document).ready(function() {
		$j('#locationId').change(function() {
			jQuery.ajax({
					url: "initialSummariesSearch.form",
					type: "GET",
					data: ({"locationId" : $j('#locationId').attr("value")}),
					success: function(data) {
								$j('#initialDate').attr("value", data);
					}});
		});
	});
</script>

<spring:message code="clinicalsummary.initial.instruction"/>

<div id="container">
	<h3 id="header"><spring:message code="clinicalsummary.initial.header"/></h3>

	<div id="main">
		<div id="leftcontent">
			<form method="post" action="">
				<fieldset>
					<ol>
						<li>
							<label for="locationId"><spring:message code="clinicalsummary.initial.location"/></label>
							<openmrs:fieldGen type="org.openmrs.Location" formFieldName="locationId" val="" parameters="optionHeader=[blank]"/>
						</li>
						<li>
							<label for="initialDate"><spring:message code="clinicalsummary.initial.date"/></label>
							<input type="text" name="initialDate" size="10" id="initialDate" onClick="showCalendar(this)"/>
							<br/>
							<i><spring:message code="clinicalsummary.initial.default"/></i>
						</li>
						<li>
							<input type="submit" value="<spring:message code="clinicalsummary.initial.save"/>"/>
						</li>
					</ol>
				</fieldset>
			</form>
		</div>
		<div id="clear"></div>
	</div>
</div>
<%@ include file="/WEB-INF/template/footer.jsp" %>
