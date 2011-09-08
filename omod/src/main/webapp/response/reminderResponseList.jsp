<%@ include file="/WEB-INF/template/include.jsp" %>
<openmrs:require privilege="Manage Summaries" otherwise="/login.htm" redirect="/module/clinicalsummary/response/reminderResponseList.list"/>


<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="../localHeader.jsp" %>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js"/>
<openmrs:htmlInclude file="/moduleResources/clinicalsummary/css/form.css"/>

<script language="javascript">

	$j(function() {

		$j("#search").click(function() {
			var data = $j("#form").serialize();
			$j.ajax({
				url: "reminderResponseSearch.form",
				type: "POST",
				dataType: 'json',
				data: data,
				success: function(server) {

					$j("#status").hide();

					if (jQuery.isEmptyObject(server)) {
						$j("#searchcontainer").show();
						$j("#searchResult tr").remove();

						var empty = "<tr><td>No reminder response found</td></tr>";
						$j("#searchResult").append(empty);
					}

					if (!jQuery.isEmptyObject(server)) {
						$j("#searchcontainer").show();
						$j("#searchResult tr").remove();

						jQuery.each(server, function(key, responses) {
							var counter = 1;
							jQuery.each(responses, function() {
								if (counter == 1) {
									var header =    "<tr>" +
														"<th></th>" +
														"<th><spring:message code='clinicalsummary.response.reminder.token'/></th>" +
														"<th><spring:message code='clinicalsummary.response.reminder.response'/></th>" +
														"<th><spring:message code='clinicalsummary.response.reminder.comment'/></th>" +
														"<th><spring:message code='clinicalsummary.response.reminder.datetime'/></th>" +
													"</tr>";
									var patient =   "<tr>" +
														"<td colspan='5' style='padding-top: 10px; font-weight: bold;'>" +
															"<span>" + this.patientName + " ( Responded by " + this.providerName + " )</span>" +
														"</td>" +
													"</tr>";
									$j("#searchResult").append(patient);
									$j("#searchResult").append(header);
								}

								var colored = "<tr>";
								if (counter % 2 == 1)
									colored = "<tr style='background-color: #F3F3F3;'>";

								var description = colored + "<td>" + counter++ + ".&nbsp;</td>";
									description += "<td>" + this.token + "</td>";
									description += "<td style='text-align: center;'>" + this.response + "</td>";
									description += "<td>" + this.comment + "</td>";
									description += "<td style='text-align: center;'>" + this.datetime + "</td>";
									description += "</tr>";
								$j("#searchResult").append(description);
							});
						});

					}
				}
			});
		});

	});

</script>

<div id="container">
	<h3 id="header"><spring:message code="clinicalsummary.response.reminder.header"/></h3>

	<div id="main">
		<div id="sidebar">
			<form id="form" method="post" action="">
				<fieldset>
					<ol>
						<li>
							<label for="patientIdentifier"><spring:message code="clinicalsummary.response.reminder.patients"/></label>
							<textarea id="patientIdentifier" name="patientIdentifiers" row="40" cols="35"></textarea>
						</li>
						<li/>
						<li>
							<input type="button" id="search" value="<spring:message code="clinicalsummary.response.reminder.search"/>"/>
						</li>
					</ol>
				</fieldset>
			</form>
		</div>
		<div id="maincontent">
			<fieldset id="searchcontainer" style="display: none; padding-right: 8px;">
				<ol>
					<li>
						<table cellpadding="0" cellspacing="0" border="0" class="display" id="result" width="100%">
							<tbody id="searchResult"></tbody>
						</table>
					</li>
				</ol>
			</fieldset>
		</div>
		<div id="clear">
		</div>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>
