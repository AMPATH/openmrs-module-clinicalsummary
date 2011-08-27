<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Summaries" otherwise="/login.htm" redirect="/module/clinicalsummary/response/responseList.list"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="../localHeader.jsp" %>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js"/>
<openmrs:htmlInclude file="/moduleResources/clinicalsummary/css/form.css"/>


<script>
	$j = jQuery.noConflict();

	function accept(id) {
		var data = "comment=" + $j("#comment_" + id).attr("value") + "&id=" + id;
		$j.ajax({
			url: "acceptResponse.form",
			type: "POST",
			dataType: 'json',
			data: data,
			success: function(data) {
				if (data) {
					var parent = $j("#operation_" + id).parent();
					parent.append("<td style='text-align: right;' colspan='3'>Accepted</td>")
					$j(".operation_" + id).remove();
				}
			}
		});
	}

	function ignore(id) {
		var data = "comment=" + $j("#comment_" + id).attr("value") + "&id=" + id;
		$j.ajax({
			url: "ignoreResponse.form",
			type: "POST",
			dataType: 'json',
			data: data,
			success: function(data) {
				if (data) {
					var parent = $j("#operation_" + id).parent();
					parent.append("<td style='text-align: right;' colspan='3'>Ignored</td>")
					$j(".operation_" + id).remove();
				}
			}
		});
	}

	$j(function() {

		$j("#search").click(function() {
			var data = $j("#form").serialize();
			$j.ajax({
				url: "responseSearch.form",
				type: "POST",
				dataType: 'json',
				data: data,
				success: function(server) {

					if (jQuery.isEmptyObject(server))
						$j("#searchcontainer").hide();

					if (!jQuery.isEmptyObject(server)) {
						$j("#searchcontainer").show();
						$j("#result tr").remove();

						jQuery.each(server, function(key, responses) {
							var header = null;
							var counter = 1;
							jQuery.each(responses, function() {
								if (this.status == 1 || this.status == -1) {
									if (header == null) {
										header = "<tr><td colspan='3'><span style='font-weight: bold'>" + this.patientName + " ( Requested by " + this.providerName + " )</span></td></tr>";
										$j("#result").append(header);
									}

									var description = "<tr><td>" + counter++ + ".</td>";
									if (this.status == -1)
										description += "<td>Please remove " + this.medicationName + " from encounter on " + this.datetime + "</td>";
									else if (this.status == 1)
										description += "<td>Please add " + this.medicationName + " to encounter on " + this.datetime + "</td>";

									description +=  "<td><a href='#' onclick='view(" + this.id + ")'>View Encounter</a> |</td>";

									if (this.action == undefined) {
										var comment = '';
										if (this.comment != undefined)
											comment = this.comment;
										description +=  "<td class='operation_" + this.id + "'><spring:message code='clinicalsummary.response.comment'/></td>";
										description +=  "<td class='operation_" + this.id + "'><input id='comment_" + this.id + "' type='text' name='comment_" + this.id + "' value='" + comment + "'/></td>";
										description +=  "<td class='operation_" + this.id + "' style='text-align: right;' id='operation_" + this.id + "'>" +
															"<a href='#' onclick='accept(" + this.id + ")'>Accept</a> | " +
															"<a href='#' onclick='ignore(" + this.id + ")'>Ignore</a>" +
														"</td></tr>";
									} else if (this.action == 'Ignore')
										description +=  "<td style='text-align: right;' colspan='3'>Ignored</td>";
									else if (this.action == 'Accept')
										description +=  "<td style='text-align: right;' colspan='3'>Accepted</td>";

									$j("#result").append(description);
								}
							});
						});

					}
				}
			});
		});

	});
</script>

<style type="text/css">

	td, th {
		color: #333333;
		padding-top: 3px;
		padding-right: 5px;
	}

	td {
		font-weight: normal;
	}

</style>

<div id="container">
	<h3 id="header"><spring:message code="clinicalsummary.response.header"/></h3>

	<div id="main">
		<div id="leftcontent" style="width: 25%">
			<form method="post" id="form" action="">
				<fieldset>
					<ol>
						<li>
							<label for="locationId"><spring:message code="clinicalsummary.response.location"/></label>
							<openmrs:fieldGen type="org.openmrs.Location" formFieldName="locationId" val="" parameters="optionHeader=[blank]"/>
						</li>
						<li>
							<label for="displayType"><spring:message code="clinicalsummary.response.displayType"/></label>
							<select name="displayType" id="displayType">
								<c:forEach var="displayType" items="${displayTypes}" varStatus="varStatus">
									<option value="${displayType}">${displayType.value}</option>
								</c:forEach>
							</select>
						</li>
						<li />
						<li>
							<input type="button" id="search" value="<spring:message code="clinicalsummary.response.search"/>"/>
						</li>
					</ol>
				</fieldset>
			</form>
		</div>
		<div id="maincontent">
			<fieldset id="searchcontainer" style="display: none">
				<ol>
					<li>
						<table cellpadding="0" cellspacing="0" border="0" class="display">
							<thead>
								<tr>
									<th colspan="3"></th>
									<th colspan="3" style="text-align: center;"><spring:message code="clinicalsummary.response.operation"/></th>
								</tr>
							</thead>
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
