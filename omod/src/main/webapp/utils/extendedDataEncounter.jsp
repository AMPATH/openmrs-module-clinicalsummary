<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Generate Summaries" otherwise="/login.htm" redirect="/module/clinicalsummary/utils/extendedDataEncounter.form"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="../localHeader.jsp" %>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js"/>

<openmrs:htmlInclude file="/moduleResources/clinicalsummary/css/form.css"/>

<div id="container">

	<h3 id="header">Creating Patient Study Data</h3>

	<div id="main">
		<div id="leftcontent">
			<form method="post" action="" enctype="multipart/form-data">
				<fieldset>
					<ol>
                        <li>
                            <label for="conceptNames">Concept Names (Optional)</label>
                            <input type="text" id="conceptNames" name="conceptNames"/>
                        </li>
						<li>
							<label for="data">Input Data</label>
							<input type="file" id="data" name="data"/>
						</li>
						<li />
						<li>
							<input type="submit" value="Generate Extended Encounter Data"/>
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
