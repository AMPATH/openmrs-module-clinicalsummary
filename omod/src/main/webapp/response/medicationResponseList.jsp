<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Summaries" otherwise="/login.htm" redirect="/module/clinicalsummary/response/medicationResponseList.list"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="../localHeader.jsp" %>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js"/>
<openmrs:htmlInclude file="/moduleResources/clinicalsummary/css/form.css"/>


<script language="javascript">
	$j = jQuery.noConflict();

	var default_border = "1px solid cadetblue";

	function clearMessage(id) {
		console.log("Clearing error message");
		$j("#comment_" + id).css("border", default_border);
	}

	function view(id, title) {
		console.log("Viewing encounter");
		var data = "id=" + id;
		$j.ajax({
			url: "viewEncounter.form",
			type: "POST",
			dataType: 'json',
			data: data,
			success: function(data) {
				$j("#status").hide();
				var url = "${pageContext.request.contextPath}/admin/encounters/encounterDisplay.list?encounterId=" + data;
				$j("#displayEncounterPopupIframe").attr("src", url);
				$j('#displayEncounterPopup')
					.dialog('option', 'title', title)
					.dialog('option', 'height', $j(window).height() - 50)
					.dialog('open');
			}
		});
	}

	function accept(id) {
		console.log("Accepting drug changes");
		var data = "comment=" + $j("#comment_" + id).attr("value") + "&id=" + id;
		$j.ajax({
			url: "acceptMedication.form",
			type: "POST",
			dataType: 'json',
			data: data,
			success: function(data, status, jqXHR) {
				var parent = $j("#operation_" + id).parent();
				parent.append("<td style='text-align: right;' colspan='3'>Accepted</td>");
				$j(".operation_" + id).remove();
			},
			error: function(jqXHR, status, error) {
				$j("#comment_" + id).css("border", "1px solid red");
			}
		});
	}

	function ignore(id) {
		console.log("Ignore drug changes");
		var data = "comment=" + $j("#comment_" + id).attr("value") + "&id=" + id;
		$j.ajax({
			url: "ignoreMedication.form",
			type: "POST",
			dataType: 'json',
			data: data,
			success: function(data, status, jqXHR) {
				var parent = $j("#operation_" + id).parent();
				parent.append("<td style='text-align: right;' colspan='3'>Ignored</td>");
				$j(".operation_" + id).remove();
			},
			error: function(jqXHR, status, error) {
				$j("#comment_" + id).css("border", "1px solid red");
			}
		});
	}

	$j(function() {
		
		default_border = $j("input").css("border");

		$j('#displayEncounterPopup').dialog({
			title: 'dynamic',
			autoOpen: false,
			draggable: false,
			resizable: false,
			width: '80%',
			modal: true,
			open: function(a, b) { $j('#displayEncounterPopupLoading').show();}
		});

		$j("#search").click(function() {
			var data = $j("#form").serialize();
			$j.ajax({
				url: "medicationResponseSearch.form",
				type: "POST",
				dataType: 'json',
				data: data,
				beforeSend: function(jqXHR, settings) {
					$j("#result tr").remove();
					$j("#searchcontainer").show();
					$j("#result").append("<tr><td style='font-weight: bold;'>Loading ...</td></tr>");
				},
				success: function(data, status, jqXHR) {
					$j("#result tr").remove();
					$j("#searchcontainer").show();
					if (jQuery.isEmptyObject(data)) {
						// write and empty status in the page
						$j("#result").append("<tr><td>No drug changes found</td></tr>");
					} else {
						jQuery.each(data, function(key, responses) {
							var counter = 1;
							jQuery.each(responses, function() {
								if (jQuery.isEmptyObject(data)) {
									// write and empty status in the page
									$j("#result").append("<tr><td>No drug changes found</td></tr>");
								} else {
									if (this.status == 1 || this.status == -1) {
										if (counter == 1) {
											header =    "<tr>" +
															"<td colspan='5' style='padding-top: 10px;'>" +
																"<span style='font-weight: bold'>" +
																	this.patientName + " ( Requested by " + this.providerName + " )" +
																"</span>" +
															"</td>" +
														"</tr>";
											$j("#result").append(header);
										}

										var colored = "<tr>";
										if (counter % 2 == 1)
											colored = "<tr style='background-color: #F3F3F3;'>";

										var description = colored + "<td>" + counter++ + ".&nbsp;</td>";
										if (this.status == -1)
											description += "<td colspan='4'>Please remove " + this.medicationName + " from encounter on " + this.datetime + "</td>";
										else if (this.status == 1)
											description += "<td colspan='4'>Please add " + this.medicationName + " to encounter on " + this.datetime + "</td>";
										description += "</tr>";

										var operation =  colored + "<td></td><td><a href='#' onclick='view(" + this.id + ", \"Encounter with " + this.providerName + " on " + this.datetime + "\")'>View Encounter</a></td>";

										if (this.action == undefined) {
											var comment = '';
											if (this.comment != undefined)
												comment = this.comment;
											operation +=    "<td style='text-align: right; padding-right: 3px;' class='operation_" + this.id + "' colspan='2'>";
											operation +=        "<span style='margin-right: 3px'><spring:message code='clinicalsummary.response.comment'/></span>" +
																"<input id='comment_" + this.id + "' type='text' name='comment_" + this.id + "' value='" + comment + "' onclick='clearMessage(" + this.id + ");'/>|" +
																"<a href='#' onclick='accept(" + this.id + ")'>Accept</a> | " +
																"<a href='#' onclick='ignore(" + this.id + ")'>Ignore</a>" +
															"</td>";
										} else if (this.action == 'Ignore')
											operation +=  "<td style='text-align: right;' colspan='3'>Ignored</td>";
										else if (this.action == 'Accept')
											operation +=  "<td style='text-align: right;' colspan='3'>Accepted</td>";
										operation += "</tr>";

										$j("#result").append(description);
										$j("#result").append(operation);
									}
								}
							});
						});
					}
				},
				error: function(jqXHR, status, error) {
					$j("#result tr").remove();
					$j("#result").append("<tr><td style='font-weight: bold;'>Error searching for medication responses. Please try again later!</td></tr>");
					$j("#searchcontainer").show();
				}
			});
		});

		$j("#displayEncounterPopupIframe").load(function() { $j('#displayEncounterPopupLoading').hide(); });
	});
</script>

<div id="displayEncounterPopup">
	<div id="displayEncounterPopupLoading"><spring:message code="general.loading"/></div>
	<iframe id="displayEncounterPopupIframe" width="100%" height="100%" marginWidth="0" marginHeight="0" frameBorder="0" scrolling="auto"></iframe>
</div>

<style type="text/css">

	td, th {
		color: #333333;
		padding-top: 3px;
	}

	td {
		font-weight: normal;
	}

</style>

<div id="container">
	<h3 id="header"><spring:message code="clinicalsummary.response.medication.header"/></h3>

	<div id="main">
		<div id="leftcontent" style="width: 25%">
			<form method="post" id="form" action="">
				<fieldset>
					<ol>
						<li>
							<label for="locationId"><spring:message code="clinicalsummary.response.medication.location"/></label>
							<openmrs:fieldGen type="org.openmrs.Location" formFieldName="locationId" val="" parameters="optionHeader=[blank]"/>
						</li>
						<li>
							<label for="displayType"><spring:message code="clinicalsummary.response.medication.displayType"/></label>
							<select name="displayType" id="displayType">
								<c:forEach var="displayType" items="${displayTypes}" varStatus="varStatus">
									<option value="${displayType}">${displayType.value}</option>
								</c:forEach>
							</select>
						</li>
						<li />
						<li>
							<input type="button" id="search" value="<spring:message code="clinicalsummary.response.medication.search"/>"/>
						</li>
					</ol>
				</fieldset>
			</form>
		</div>
		<div id="maincontent">
			<fieldset id="searchcontainer" style="display: none; padding-right: 8px;">
				<ol>
					<li>
						<table cellpadding="0" cellspacing="0" border="0" class="display" width="100%">
							<tbody id="result"></tbody>
						</table>
					</li>
				</ol>
			</fieldset>
		</div>
		<div id="clear"></div>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>
