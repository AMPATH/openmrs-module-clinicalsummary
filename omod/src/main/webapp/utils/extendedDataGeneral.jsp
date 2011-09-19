<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Generate Summaries" otherwise="/login.htm" redirect="/module/clinicalsummary/utils/extendedDataGeneral.form"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="../localHeader.jsp" %>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js"/>

<openmrs:htmlInclude file="/moduleResources/clinicalsummary/css/form.css"/>

<div id="container">

	<h3 id="header">Creating Paediatric Study Data</h3>

	<div id="main">
		<div id="leftcontent">
			<form method="post" action="">
				<fieldset>
					<ol>
						<li>
							<input type="submit" value="Generate Extended Data"/>
						</li>
					</ol>
				</fieldset>
			</form>
		</div>

		<div id="rightcontent"></div>
		<div id="clear"></div>
		<div id="bottomcontent"></div>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>
