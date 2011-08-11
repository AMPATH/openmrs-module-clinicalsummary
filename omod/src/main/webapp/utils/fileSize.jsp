<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Summaries" otherwise="/login.htm" redirect="/module/clinicalsummary/utils/uploadSize.form"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="../localHeader.jsp" %>

<openmrs:htmlInclude file="/moduleResources/clinicalsummary/css/form.css"/>

<div id="container">
	<h3 id="header"><spring:message code="clinicalsummary.filesize.header"/></h3>

	<div id="main">
		<div id="leftcontent">
			<form method="post" action="">
				<fieldset>
					<ol>
						<li>
							<label for="size"><spring:message code="clinicalsummary.filesize.size" /></label>
							<input type="text" name="size" size="10" id="size"/>
						</li>
						<li>
							<input type="submit" value="<spring:message code="clinicalsummary.filesize.save"/>"/>
						</li>
					</ol>
				</fieldset>
			</form>
		</div>
		<div id="clear"></div>
	</div>
</div>
<%@ include file="/WEB-INF/template/footer.jsp" %>
