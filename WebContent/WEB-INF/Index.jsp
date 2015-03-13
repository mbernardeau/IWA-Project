<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<jsp:include page="Header.jsp" />
	<!DOCTYPE html>
<html>
<head>
  <title>History Mashup</title>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="stylesheet" href="http://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.min.css">
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
  <script src="http://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/js/bootstrap.min.js"></script>
  <script src="http://maps.googleapis.com/maps/api/js"></script>
<script>
	//Globals: Geocoder, Map are for Google API
	//short_info is a small description in the marker
	//more_info is a button that calls the draw_info_window function for the window under the map
		var geocoder;
		var level = 4;
		var map;
		var results;
		
		//for the infowindow under the marker
		var infowindow = new google.maps.InfoWindow();
		var short_info = "Battle of Lorem Ipsum";
		var more_info_button = "<input type=\"button\" value=\"more info\" onclick=\"draw_info_window()\">";
		
		/*
		Here I will place some global endpoints that can be used to change and interface with DB's
		note: These can be changed at any time to extend or diminish the amount of info it displays
		the names should elaborate what they are for. 
		The following function fills these values with test values, however could be used to change on search.
		*/
		var Commander_1;
		var Commander_2;
		var Battle_Start;
		var Battle_End;
		var Win;
		var Condition;
		var locations = [
			['London', 51.508742,-0.120850, 4],
			//['Coogee Beach', -33.923036, 151.259052, 5],
			//['Cronulla Beach', -34.028249, 151.157507, 3],
			//['Manly Beach', -33.80010128657071, 151.28747820854187, 2],
			['unknown', -52.950198, -0.259302, 1]
		];
		
		
		function test_draw(){
			Commander_1 = "Vasilii";
			Commander_2 = "Sjoerd";
			Battle_Start = 1805;
			Battle_End = 1875;
			Win = "Vasilii won";
			Condition = "good";
		}
		
		//inits the map
		function initialize() {
  			var mapProp = {
    				center:new google.maps.LatLng(51.508742,-0.120850),
    				zoom:level,
    				mapTypeId:google.maps.MapTypeId.ROADMAP
  			};
			geocoder = new google.maps.Geocoder();
  			map=new google.maps.Map(document.getElementById("googleMap"),mapProp);
			document.getElementById("rangeVal").value = 1900;
			document.getElementById("Year").value = 1900;
		}
		google.maps.event.addDomListener(window, 'load', initialize);
		
		//code addr for now stupidly finds the place and places a marker there. 
		function codeAddress() {
			var address = document.getElementById("address").value;
			//test value
			address = "London";
			var Commander = document.getElementById("Commander").value;
			//feed geocode information directly into a prototype function to place marker and display infowindow
			geocoder.geocode( { 'address': address}, function(results, status) {
			if (status == google.maps.GeocoderStatus.OK) {
				map.setCenter(results[0].geometry.location);
				draw_markers();
			}
		else {
        alert("Geocode was not successful for the following reason: " + status);
      }
    });
	}
	
	function draw_markers(){
		var marker, i;

		for (i = 0; i < locations.length; i++) {  
			marker = new google.maps.Marker({
			position: new google.maps.LatLng(locations[i][1], locations[i][2]),
			map: map
		});

		google.maps.event.addListener(marker, 'click', (function(marker, i) {
			return function() {
				infowindow.setContent(short_info);
				infowindow.open(map, marker);
				draw_info_window();
			}
		})(marker, i));
		}
	}
	
	//draws more information about battle under the map
	var img_test = "http://riverboatsmusic.com.au/wp-content/uploads/2014/09/1shuu4q3.wizardchan.test_.png"
	//draws more information about battle under the map
	function draw_info_window(){
		var head = "<h2>Battle of Lorem Ipsum</h2>"
		var sub = "<h3>Year:"+document.getElementById("Year").value+"</h3>"
		var text = "This where the information text can go. It even features overflow. Although I doubt I could write such an interesting and long bit of information to try this feature out. This is an elaboration on text_summary which should be inserted in the marker window.";
		var img = "<img src=\""+img_test+"\" class=\"img-responsive\" alt=\"test\">";
		document.getElementById("information_window").innerHTML = head+sub+text+text+text+img;
	}
	
	//small function to print the value of the slider
	function printValue(val1, val2){
		var temp = document.getElementById(val1);
		var goal = document.getElementById(val2);
		goal.value = temp.value;
	}

</script>
</head>

<body>

<div class="container">
  <div class="jumbotron">
    <h1>History Mashup</h1>
    <p>Where history gets inferred</p> 
  </div>
  <div class="row">
    <div class="col-sm-2">
      <h3>Input</h3>
      <input id="address" type="textbox" value="Search Place">
		<input id="Commander" type="textbox" value="Search Commander">
		<input id="Year" type="range" min="-1000" max="2015" step="1" onchange="printValue('Year', 'rangeVal')"> <input id="rangeVal" type="text" size="2" onchange="printValue('rangeVal', 'Year')">
		<input type="button" value="Encode" onclick="codeAddress()">
    </div>
    <div class="col-sm-6">
      <h3>Map</h3>
      <div id="googleMap" style="width:relative;height:380px;right:0px;padding:20px;border:2px solid;" ></div>
    </div>
    <div class="col-sm-3">
      <h3>Information</h3>        
      <div style="width:relative; height:380px; overflow: auto;right:0px;padding:20px;" >
			<div id="information_window"></div>
		</div>
    </div>
  </div>
</div>

</body>
</html>

<jsp:include page="Footer.jsp" />