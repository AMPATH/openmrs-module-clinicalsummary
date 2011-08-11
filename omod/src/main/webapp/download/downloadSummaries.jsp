<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Summaries" otherwise="/login.htm" redirect="/module/clinicalsummary/download/downloadSummaries.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="../localHeader.jsp" %>

<openmrs:htmlInclude file="/moduleResources/clinicalsummary/css/form.css" />

<script>
	var timeout;

	$j = jQuery.noConflict();
	$j(function() {
		$j("#progressbar").progressbar({
			value: 100
		});

		$j(".download").click(function() {
			var filename = $j(this).attr("filename");
			$j("input:hidden[id='filename']").attr("value", filename);
			var type = $j(this).attr("type");
			$j("input:hidden[id='type']").attr("value", type);
			$j("#downloadPhysical").submit();
		});

		startWatcherIfNeeded();
	});

	function startWatcherIfNeeded() {
		var json = "${pageContext.request.contextPath}/module/clinicalsummary/watcher/watchDownloadUpload.form";
		jQuery.getJSON(json, function(data) {
			// start processing the json data
			if (data.running) {
				$j("input").attr("disabled", "disabled");
				$j("#file").html(data.filename);
				$j("#task").html(data.task);
				$j("#rightcontent").show();
				timeout = setTimeout("startWatcherIfNeeded()", 3000);
			} else {
				if (typeof data.file  != "undefined") {
					$j(".file").show();
					$j("#zip").attr("filename", data.file + ".zip");
					$j("#zip").html(data.file + ".zip");
					$j("#secret").attr("filename", data.file + ".secret");
					$j("#secret").html(data.file + ".secret");
				}
				$j("input").removeAttr("disabled");
				// hide the progress bar
				$j("#rightcontent").hide();
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

	.padded-div {
		padding: 1em 1em 0 1em;
	}
</style>

<spring:message code="clinicalsummary.download.instructions"/>
<div id="container">
	<h3 id="header"><spring:message code="clinicalsummary.download.header"/></h3>

	<div id="main">
		<div id="sidebar">
			<form method="post" enctype="multipart/form-data" action="">
				<fieldset>
					<ol>
						<li>
							<label for="password"><spring:message code="clinicalsummary.download.password"/></label>
							<input type="password" id="password" name="password"/>
						</li>
						<li>
							<label for="passphrase"><spring:message code="clinicalsummary.download.passphrase"/></label>
							<input type="password" id="passphrase" name="passphrase"/>
						</li>
						<li />
						<li>
							<input id="submit" type="submit" value="<spring:message code="clinicalsummary.download"/>"/>
						</li>
					</ol>
				</fieldset>
			</form>
		</div>
		<div id="rightcontent">
			<fieldset id="progress">
				<div style="padding-bottom: 0.5em;">
					Status: <span id="task"></span>
				</div>
				<div id="progressbar"></div>
				<div class="padded-div" style="padding-bottom: 1em; padding-top:0.5em">
					Processing: <span id="file"></span>
				</div>
			</fieldset>
		</div>
		<div id="clear" style="padding: 1em 0 1em 0">
			<form id="downloadPhysical" action="downloadPhysical.form" method="post">
				<input id="filename" name="filename" type="hidden" />
				<input id="type" name="type" type="hidden" />
			</form>
			<c:if test="${not empty zipFile}">
				<div class="file">
					<spring:message code="clinicalsummary.download.summariesFile"/>:
					<div><a href="#" id="zip" type="zip" filename="${zipFile}" class="download">${zipFile}</a></div>
				</div>
			</c:if>
			<br /><br />
			<c:if test="${not empty secretFile}">
				<div class="file">
					<spring:message code="clinicalsummary.download.secretFile"/>:
					<div><a href="#" id="secret" type="secret" filename="${secretFile}" class="download">${secretFile}</a></div>
				</div>
			</c:if>
		</div>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>
