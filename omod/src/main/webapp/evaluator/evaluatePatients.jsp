<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Generate Summaries" otherwise="/login.htm" redirect="/module/clinicalsummary/evaluator/evaluatePatients.form"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="../localHeader.jsp" %>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js"/>

<openmrs:htmlInclude file="/moduleResources/clinicalsummary/css/form.css"/>

<openmrs:htmlInclude file="/moduleResources/clinicalsummary/scripts/jquery/jquery-1.5.1.min.js" />

<openmrs:htmlInclude file="/moduleResources/clinicalsummary/scripts/jquery-ui/jquery-ui-1.8.12.custom.min.js" />
<openmrs:htmlInclude file="/moduleResources/clinicalsummary/scripts/jquery-ui/jquery-ui-1.8.12.custom.css" />

<script>
	var timeout;

	$j = jQuery.noConflict();
	$j(function() {
		$j("#progressbar").progressbar({
			value: 100
		});
		startWatcherIfNeeded();
	});

	function startWatcherIfNeeded() {
		var json = "${pageContext.request.contextPath}/module/clinicalsummary/watcher/watchCohortEvaluation.form";
		jQuery.getJSON(json, function(data) {
			// start processing the json data
			if (data.running) {
				$j("#filename").html(data.filename);
				$j("#task").html(data.task);
				$j("#size").html(data.size);
				$j("#counter").html(data.counter);
				$j("#bottomcontent").show();
				timeout = setTimeout("startWatcherIfNeeded()", 3000);
			} else {
				// hide the progress bar
				$j("#bottomcontent").hide();
				clearTimeout(timeout);
			}
		});
	}
</script>

<style type="text/css">
	.ui-widget-header {
		background: url("images/ui-bg_highlight-soft_50_1aad9b_1x100.png") repeat-x scroll 50% 50% #E78F08;
		border: 1px solid #E78F08;
	}

	.ui-widget-content {
		border: none;
	}

	.ui-progressbar-value {
		background-image: url(${pageContext.request.contextPath}/moduleResources/clinicalsummary/images/progress.gif);
	}

	fieldset#progress {
		padding: 1em 1em 0 1em;
	}

	#bottomcontent {
		margin: 0 1em 1em 1em;
		width: 40%;
	}
</style>

<spring:message code="clinicalsummary.generate.instructions"/>

<div id="container">

	<h3 id="header"><spring:message code="clinicalsummary.generate.generating"/></h3>

	<div id="main">
		<div id="leftcontent">
			<form method="post" action="">
				<fieldset>
					<legend><spring:message code="clinicalsummary.generate.byCohort"/></legend>
					<ol>
						<li>
							<label for="summaryId"><spring:message code="clinicalsummary.generate.summary"/></label>
							<select name="summaryId" id="summaryId">
								<c:forEach var="summary" items="${summaries}" varStatus="varStatus">
									<option value="${summary.id}">${summary.name}</option>
								</c:forEach>
							</select>
						</li>
						<li>
							<label for="cohort"><spring:message code="clinicalsummary.generate.cohort"/></label>
							<select name="cohort" id="cohort">
								<c:forEach var="cohort" items="${cohorts}" varStatus="varStatus">
									<option value="${cohort.id}">${cohort.name}</option>
								</c:forEach>
							</select>
						</li>
						<li />
						<li>
							<input type="submit" value="<spring:message code="clinicalsummary.generate"/>"/>
						</li>
					</ol>
				</fieldset>
			</form>
		</div>

		<div id="rightcontent"></div>
		<div id="clear"></div>
		<div id="bottomcontent">
			<fieldset id="progress">
				<div style="padding-bottom: 0.5em;">
					Evaluator Status: <span id="task"></span>
				</div>
				<div id="progressbar"></div>
				<div class="padded-div" style="padding-bottom: 1em; padding-top:0.5em">
					Processing patient with internal id <span id="filename"></span> ( <span id="counter"></span> out of <span id="size"></span> )
				</div>
			</fieldset>
		</div>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>
