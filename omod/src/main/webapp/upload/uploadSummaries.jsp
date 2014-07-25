<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Summaries" otherwise="/login.htm" redirect="/module/clinicalsummary/upload/uploadSummaries.form"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="../localHeader.jsp" %>

<openmrs:htmlInclude file="/moduleResources/clinicalsummary/css/form.css" />

<script>
	var timeout;

	$j = jQuery.noConflict();
	$j(function() {
        $j("#rightcontent").hide();
		$j("#progressbar").progressbar({
			value: 100
		});
        // start watcher
        startWatcherIfNeeded();
	});

	function startWatcherIfNeeded() {
		var json = "${pageContext.request.contextPath}/module/clinicalsummary/watcher/watchDownloadUpload.form";
		jQuery.getJSON(json, function(data) {
			// start processing the json data
			if (data.running) {
				$j("#filename").html(data.filename);
				$j("#task").html(data.task);
				$j("#rightcontent").show();
				timeout = setTimeout("startWatcherIfNeeded()", 3000);
			} else {
                if (data.task != "") {
                    if (data.task != "Failed") {
                        $j("#processor-message").html("Summaries uploaded successfully!");
                        $j("#processor-message").show();
                    } else {
                        $j("#processor-error").html("Unable to process uploaded summaries!");
                        $j("#processor-error").show();
                    }
                }
				// hide the progress bar
				$j("#rightcontent").hide();
				clearTimeout(timeout);
			}
		});
	}

    function uploadSummaries() {
        var form = document.getElementById("upload");
        form.submit();
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

    .processor-error {
        display: none;
        background-color: #ffb6c1;
        margin-bottom: 5px;
    }

    .processor-message {
        display: none;
        background-color: #f5f5dc;
        margin-bottom: 5px;
    }
</style>

<spring:message code="clinicalsummary.upload.instructions"/>
<div id="container">
	<div id="main">
		<h3 id="header"><spring:message code="clinicalsummary.upload.header"/></h3>

		<div id="sidebar">
			<form id="upload" method="post" enctype="multipart/form-data" action="">
				<fieldset>
					<ol>
						<li>
							<label for="password"><spring:message code="clinicalsummary.upload.password"/></label>
							<input type="password" id="password" name="password"/>
						</li>
						<li>
							<label for="summaries"><spring:message code="clinicalsummary.upload.summaries"/></label>
							<input type="file" id="summaries" name="summaries"/>
						</li>
						<li>
							<input type="hidden" id="uploadAction" name="action"/>
						</li>
						<li>
							<input type="button" onclick="uploadSummaries()" value="<spring:message code="clinicalsummary.upload"/>" />
						</li>
					</ol>
				</fieldset>
			</form>
            <span id="processor-error" class="processor-error"></span>
            <span id="processor-message" class="processor-message"></span>
		</div>
		<div id="rightcontent">
			<fieldset id="progress">
				<div style="padding-bottom: 0.5em;">
					Status: <span id="task"></span>
				</div>
				<div id="progressbar"></div>
				<div class="padded-div" style="padding-bottom: 1em; padding-top:0.5em">
					Processing: <span id="filename"></span>
				</div>
			</fieldset>
		</div>
		<div id="clear"></div>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>
