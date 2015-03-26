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
		var min_year = 1900;
		//for the infowindow under the marker
		var infowindow = new google.maps.InfoWindow();
		var short_info;
		var more_info_button = "<input type=\"button\" value=\"more info\" onclick=\"draw_info_window()\">";
		//rain or sun icons to display battle weather
		var b_weather_rainy = "http://icons.iconarchive.com/icons/icons8/android/64/Weather-Little-Rain-icon.png";
		var b_weather_sunny = "http://uxrepo.com/static/icon-sets/meteo/png32/64/000000/sun-64-000000.png";
		//test global
		var match;
		var result_query = [];
		
		//parameters to be filled:
		var b_title = [];
		var year = [];
		var b_summary = [];
		var b_img = img_test;
		var b_commander = [];
		
		/*
		Here I will place some global endpoints that can be used to change and interface with DB's
		note: These can be changed at any time to extend or diminish the amount of info it displays
		the names should elaborate what they are for. 
		The following function fills these values with test values, however could be used to change on search.
		*/
		var b_test='';
		var locations = [
		];
		//52	22 N	4	53
		
		
		
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
			
			for (i = 0; i < locations.length; i++){
				console.log(locations[i][1][2]);
			}
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
				information_stash();
				//draw_info_window();
				
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
		
		var mark = new google.maps.LatLng(locations[0][1], locations[0][2]);
		
		map.setOptions({
        center: mark,
        zoom: level
		});

		google.maps.event.addListener(marker, 'click', (function(marker, i) {
			return function() {
				infowindow.setContent(locations[i][0]);
				infowindow.open(map, marker);
				draw_info_window(i);
			}
		})(marker, i));
		}
	}
	
	//test function JSON parse
	function parse_match(o, matchy, lim1, lim2){
    var str='';
	var result_matches = ["test"];
    
	for(var p in o){

        if(typeof o[p] == 'string'){
            str+= p + ': ' + o[p]+'; </br>';
			if(p == matchy){
				//console.log(p+"\n");
				b_test = o[p]+"\n";
				if(lim1 == "commander"){
					b_test = b_test.substring(b_test.lastIndexOf("\/")+1,b_test.length);
					b_test = b_test.split('_').join(' ');
					result_query.push(b_test);
					console.log(result_query);
				}
				else{
					b_test = b_test.substring(b_test.indexOf(lim1)+1,b_test.lastIndexOf(lim2));
					//console.log(b_test);
					result_query.push(b_test);
					console.log(result_query);
				}
			}
        }else{
			if(p == matchy){
				console.log(p+"\n");
				b_test = o[p]+"\n";
				console.log(b_test);
				//result_matches.push(b_test);
			}
            str+= p + ': { </br>' + parse_match(o[p], matchy, lim1, lim2) + '}';
        }
		
    }

    return result_query;
}
	
	
	//draws more information about battle under the map
	var img_test = "http://riverboatsmusic.com.au/wp-content/uploads/2014/09/1shuu4q3.wizardchan.test_.png"
	//draws more information about battle under the map
	function information_stash(){
		
		var b_weather = "<img src=\""+b_weather_rainy+"\" class=\"img-responsive\" alt=\"test\">";
		//Call with query parameters
		//should be range
		min_year= document.getElementById("rangeVal").value;
		
		var commander = document.getElementById("Commander").value;
		var tmp = query_build(commander, "Search Place", min_year, "off");
		$.getJSON(tmp, function(data){
			//quick and dirty fix for reading in JSON with URL
			//use match for the data to be matched to a certain URL
			//test to check if JSON Data
			//console.log(printy(data));
			//Match to battle title:
			//match = "http://xmlns.com/foaf/0.1/name";
			//parse_match(data, "http://xmlns.com/foaf/0.1/name");
			b_title = parse_match(data, "http://xmlns.com/foaf/0.1/name", "\"", "\"");
			result_query = [];
			console.log(b_title);
			
			year = parse_match(data, "http://dbpedia.org/ontology/date","\"" , "+" );
			result_query = [];
			
			b_commander = parse_match(data,"http://dbpedia.org/ontology/commander", "commander", "+");
			result_query = [];
			b_summary = parse_match(data, "http://dbpedia.org/ontology/abstract", "\"", "\"");
			result_query = [];
			
			
			var lat = parse_match(data,"http://www.w3.org/2003/01/geo/wgs84_pos#lat", "\"", "\"");
			console.log(lat);
			result_query = [];
			
			var lng = parse_match(data,"http://www.w3.org/2003/01/geo/wgs84_pos#long", "\"", "\"");
			result_query = [];
			//short_info = b_title + ": "+year;
			for (i = 0; i < b_title.length; i++){
				console.log(lat[i]);
				locations.push([b_title[i],lat[i], lng[i]]);
			
			}
			console.log(locations);
			draw_markers();
			/*
			//parse_match(data, match);
			b_commander = b_test.substring(b_test.lastIndexOf("\/")+1,b_test.length);
			var cond = "Commanding Officer:"+b_commander+"<br>"; 
			match = "http://dbpedia.org/ontology/abstract";
			//parse_match(data, match);
			var text = b_test;
			text.replace(/^(http?:\/\/)?$/g,'');
			match = "http://www.w3.org/2003/01/geo/wgs84_pos#lat";
			//parse_match(data, match);
			var lat = b_test.substring(b_test.indexOf("\"")+1,b_test.lastIndexOf("\""));
			console.log(lat);
			match = "http://www.w3.org/2003/01/geo/wgs84_pos#long";
			//parse_match(data, match);
			var lng = b_test.substring(b_test.indexOf("\"")+1,b_test.lastIndexOf("\""));
				
			
			b_img = mediawiki(b_title);
			var img = "<img src=\""+b_img+"\" class=\"img-responsive\" alt=\"test\" >";
			var link = "<a href=\"" + tmp + "\"> source </a>";
			document.getElementById("information_window").innerHTML = head+sub+cond+text+img+link;
			short_info = b_title + ": "+year;
			locations.push([short_info,lat,lng, 1]);
			level = 3;
			draw_markers();
			*/
		});
				
		/*
		//real output
		var head = "<h2>"+b_title+"</h2>";
		var sub = "<h3>Year:"+document.getElementById("Year").value+"</h3>";
		var cond = "<br>Weather Condition: " +b_weather+"<br> Commanding Officer:"+b_commander+"<br>"; 
		var text = b_test;
		var img = "<img src=\""+b_img+"\" class=\"img-responsive\" alt=\"test\" >";
		document.getElementById("information_window").innerHTML = head+sub+cond+text+img+link;
		*/
		
		
	}
	
	function draw_info_window(selection){
		
		var head = "<h2>"+b_title[selection]+"</h2>";
		var sub = "<h3>Year:"+year[selection]+"</h3>";
		var cond = "<br> Commanding Officer:"+b_commander[selection]+"<br>"; 
		var text = b_summary[selection];
		
		//comment me out to work:
		//test code here:
		var tmp;
		/*globals MediaWikiJS*/
		var mwjs = new MediaWikiJS('https://en.wikipedia.org', {action: 'query', prop: 'images', titles: b_title[i]}, function (data) {
			'use strict';
		   var pages = data.query.pages;
		   console.log(pages[Object.keys(pages)[0]].images[0]);
		   tmp = (pages[Object.keys(pages)[0]].images[0].title);
		   console.log(tmp);
		   var query = "imageinfo&&iiprop=url";
		   var mwjs = new MediaWikiJS('https://en.wikipedia.org', {action: 'query', prop: query, titles: tmp}, function (data) {
				//'use strict';
				console.log(tmp);
				var pages = data.query.pages;
				console.log(pages[Object.keys(pages)[0]].imageinfo[0].url);
				b_img = (pages[Object.keys(pages)[0]].imageinfo[0].url);
			});
		});
		
		var img = "<img src=\""+b_img+"\" class=\"img-responsive\" alt=\"test\" >";
		//and stop commenting here
		document.getElementById("information_window").innerHTML = head+sub+cond+text+img;
		//console.log(b_img);
	}
	
	//small function to print the value of the slider
	function printValue(val1, val2){
		var temp = document.getElementById(val1);
		var goal = document.getElementById(val2);
		goal.value = temp.value;
	}
	
	//builds the query vip func
	function query_build(val_com, val_place, val_minyear, val_short){
		//basic string url
		var base = "http://localhost:8080/IWA-Project/Battle?short="
		//change parameters based on what is filled out
		
		//check for short window
		if(val_short == "on"){
				base = base + "on&limit=50000";
			}
		//these limits seem high, however due to later filtering on our part these numbers usually turn up lower and we are running locally :)
		else{
			base = base + "off&limit=1000";
		}
		
		
		//check for commander
		if(val_com !="Search Commander"){
			base = base +"&commander="+val_com;
		}
		else{
			base = base;
		}
		
		//check for place
		if(val_place !="Search Place"){
			base = base + val_place;
		}
		else{
			base = base;
		}
		
		//This is a stupid limit, but it is the easiest to enforce atm
		if(val_minyear != 2015){
			base = base +"&year=";
		}
		else{
			base = base;
		}
		
		return base;		
		
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