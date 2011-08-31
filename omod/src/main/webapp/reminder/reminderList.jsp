<%@ include file="/WEB-INF/template/include.jsp" %>
<openmrs:require privilege="Manage Summaries" otherwise="/login.htm" redirect="/module/clinicalsummary/reminder/reminderList.list"/>


<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="../localHeader.jsp" %>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js"/>
<openmrs:htmlInclude file="/scripts/jquery/dataTables/js/jquery.dataTables.min.js" />
<openmrs:htmlInclude file="/scripts/jquery/dataTables/css/dataTables.css" />
<openmrs:htmlInclude file="/scripts/jquery/dataTables/css/dataTables_jui.css" />

<openmrs:htmlInclude file="/moduleResources/clinicalsummary/css/form.css"/>

<script>
	$j = jQuery.noConflict();

	$j(function() {
		function split(expression) {
			var terms = new Array();
			var counter = 0;
			var processedExpression = expression;
			if (expression.indexOf('"') != -1) {
				for (i = 0; i < expression.length; i++) {
					var s = expression.substring(i, i + 1);
					if (s == '"') {
						// we already make sure that (i + 1) is a valid character, now check the next one after (i + 1)
						var j = i + 1;
						var found = false;
						while (j < expression.length && !found) {
							s = expression.substring(j, j + 1);
							if (s == '"')
								found = true;
							j++;
						}
						// get the actual string value
						var term = expression.substring(i, j);
						terms[counter ++] = term;
						// skip until the end of the param name
						i += term.length;
						processedExpression = expression.substring(i);
					}
				}
			}

			var split = processedExpression.split(/\s*,\s*/);
			for (j = 0; j < split.length; j ++)
				if (split[j] != "")
					terms[counter ++] = split[j];

			return terms;
		}

		function extractLast(term) {
			var expression = split(term).pop();
			return expression.replace('"', "");
		}

		var searchFragment;
		var baseFragment = "${pageContext.request.contextPath}/module/clinicalsummary/search/";
		$j(".autocomplete")
				.bind("keydown",
								function(event) {
									if (event.keyCode === $j.ui.keyCode.TAB && $j(this).data("autocomplete").menu.active) {
										event.preventDefault();
									}
								})
				.autocomplete({
				                  source: function(request, response) {
					                  $j.getJSON(baseFragment + searchFragment, {
						                  term: extractLast(request.term)
					                  }, response);
				                  },
				                  search: function() {
					                  searchFragment = $j(this).attr("source");
					                  // custom minLength
					                  var term = extractLast(this.value);
				                  },
				                  focus: function() {
					                  // prevent value inserted on focus
					                  return false;
				                  },
				                  select: function(event, ui) {
					                  var terms = split(this.value);
					                  // remove the current input
					                  terms.pop();
					                  // add the selected item
					                  terms.push('"' + ui.item.value + '"');
					                  // add placeholder to get the comma-and-space at the end
					                  terms.push("");
					                  this.value = terms.join(", ");
					                  return false;
				                  }
		                       });

		var datatable = $j('#result').dataTable({
			"fnDrawCallback": function (oSettings) {
				if (oSettings.aiDisplay.length == 0) {
					return;
				}

				var nTrs = $j('#result tbody tr');
				var iColspan = nTrs[0].getElementsByTagName('td').length;
				var sLastGroup = "";
				for (var i = 0; i < nTrs.length; i++) {
					var iDisplayIndex = oSettings._iDisplayStart + i;
					var sGroup = oSettings.aoData[ oSettings.aiDisplay[iDisplayIndex] ]._aData[0];
					if (sGroup != sLastGroup) {
						var nGroup = document.createElement('tr');
						var nCell = document.createElement('td');
						nCell.colSpan = iColspan;
						nCell.className = "group";
						nCell.innerHTML = sGroup;
						nGroup.appendChild(nCell);
						nTrs[i].parentNode.insertBefore(nGroup, nTrs[i]);
						sLastGroup = sGroup;
					}
				}
			},
			"aoColumnDefs": [
				{ "bVisible": false, "aTargets": [ 0 ] }
			],
			"aaSortingFixed": [
				[ 0, 'asc' ]
			],
			"aaSorting": [
				[ 1, 'asc' ]
			],
			"sDom": 'lfr<"giveHeight"t>ip'
		});

		$j("#search").click(function() {
			var data = $j("#form").serialize();
			$j.ajax({
				url: "reminderList.list",
				type: "POST",
				dataType: 'json',
				data: data,
				success: function(data) {
					datatable.fnClearTable();
					datatable.fnAddData(data);
				}
			});
		});
	});
</script>

<style type="text/css">
	.header {
		width: 30%;
	}

	.dataTables_filter {
		padding: 1em;
	}

	.dataTables_length {
		padding: 1em;
	}

	td, th {
		color: #333333;
	}

	td {
		font-weight: normal;
	}
</style>

<div id="container">
	<h3 id="header"><spring:message code="clinicalsummary.reminder.title"/></h3>

	<div id="main">
		<div id="sidebar">
			<form id="form" method="post" action="">
				<fieldset>
					<legend><spring:message code="clinicalsummary.reminder.parameter"/></legend>
					<ol>
						<li>
							<label for="locations"><spring:message code="clinicalsummary.reminder.location"/></label>
							<textarea id="locations" name="locations" row="40" cols="35" source="autocompleteLocation.form"
							          class="autocomplete"></textarea>
						</li>
						<li>
							<label for="token"><spring:message code="clinicalsummary.reminder.token"/></label>
							<textarea id="token" name="token" row="40" cols="35" source="autocompleteToken.form"
							          class="autocomplete"></textarea>
						</li>
						<li>
							<label for="displayType"><spring:message code="clinicalsummary.reminder.displayType"/></label>
							<select name="displayType" id="displayType">
								<c:forEach var="displayType" items="${displayTypes}" varStatus="varStatus">
									<option value="${displayType}">${displayType.value}</option>
								</c:forEach>
							</select>
						</li>
						<li>
							<label for="startTime"><spring:message code="clinicalsummary.reminder.startTime"/></label>
							<input type="text" name="startTime" size="10" id="startTime" onClick="showCalendar(this)"/>
						</li>
						<li>
							<label for="endTime"><spring:message code="clinicalsummary.reminder.endTime"/></label>
							<input type="text" name="endTime" size="10" id="endTime" onClick="showCalendar(this)"/>
						</li>
						<li/>
						<li>
							<input type="button" id="search" value="<spring:message code="clinicalsummary.reminder.search"/>"/>
						</li>
					</ol>
				</fieldset>
			</form>
		</div>
		<div id="maincontent">
			<table cellpadding="0" cellspacing="0" border="0" class="display" id="result" width="100%">
				<thead>
					<tr>
						<th class="header"><spring:message code="clinicalsummary.reminder.report.grouping"/></th>
						<th class="header"><spring:message code="clinicalsummary.reminder.report.id"/></th>
						<th class="header"><spring:message code="clinicalsummary.reminder.report.token"/></th>
						<th class="header"><spring:message code="clinicalsummary.reminder.report.count"/></th>
					</tr>
				</thead>
			</table>
		</div>
		<div id="clear">
		</div>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>
