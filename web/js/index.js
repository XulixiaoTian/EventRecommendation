(function() {

var lon = 0;
var lat = 0;

function init() {
  // Register event listeners
  document.getElementById("nearby-btn").addEventListener('click', loadNearbyEvents);
  document.getElementById('fav-btn').addEventListener('click', loadFavouriteItems);
  document.getElementById('recommend-btn').addEventListener('click', loadRecommendedEvents);

  initGeoLocation();
}

function initGeoLocation() {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(onPositionUpdated,
        onLoadPositionFailed, {
          maximumAge: 60000
        });
    showLoadingMessage('Retrieving your location...');
  } else {
    onLoadPositionFailed();
  }
}

function onPositionUpdated(position) {
  lat = position.coords.latitude;
  lon = position.coords.longitude;

  loadNearbyEvents();
}

function onLoadPositionFailed() {
  console.warn('navigator.geolocation is not available');
  getLocationFromIP();
}

function getLocationFromIP() {
  // Get location from http://ipinfo.io/json
  var url = 'http://ipinfo.io/json'
  var data = null;
  sendRequest('GET', url, data, function(res) {
    var result = JSON.parse(res);
    if ('loc' in result) {
      var loc = result.loc.split(',');
      lat = loc[0];
      lon = loc[1];
    } else {
      console.warn('Getting location by IP failed.');
    }
    loadNearbyEvents();
  });
}

function loadNearbyEvents() {
  activeBtn('nearby-btn');

  var url = './search';
  var params = '&lat=' + lat + '&lon=' + lon;
  var data = JSON.stringify({});

  showLoadingMessage('Loading nearby events...');

  sendRequest('GET', url + '?' + params, data,
      // successful callback
      function(res) {
        var events = JSON.parse(res);
        if (!events || events.length === 0) {
          showWarningMessage('No nearby event.');
        } else {
          listEvents(events);
        }
      },
      // failed callback
      function() {
        showErrorMessage('Cannot load nearby events.');
      });
}

function loadFavouriteItems() {
  activeBtn('fav-btn');

  // The request parameters
  var url = './favourite';
  var req = JSON.stringify({});

  // display loading message
  showLoadingMessage('Loading favourite events...');

  sendRequest('GET', url, req, function(res) {
    var events = JSON.parse(res);
    if (!events || events.length === 0) {
      showWarningMessage('No favourite event.');
    } else {
      listEvents(events);
    }
  }, function() {
    showErrorMessage('Cannot load favourite events.');
  });
}

function changeFavouriteEvent(event_id) {
  // Check whether this event has been visited or not
  var li = $('#event-' + event_id);
  var favIcon = $('#fav-icon-' + event_id);
  console.log("favourite here " + li.data("favourite"));
  var isFavourite = li.data("favourite") !== true;
  console.log("isFavourite here " + isFavourite);

  // The request parameters
  var url = './favourite';
  var data = JSON.stringify({
    favourite: [event_id]
  });
  var method = isFavourite ? 'POST' : 'DELETE';

  sendRequest(method, url, data,
      // successful callback
      function(res) {
        li.data("favourite", isFavourite);
        favIcon.attr("class", isFavourite ? 'fa fa-heart' : 'fa fa-heart-o');
      });
}

  function loadRecommendedEvents() {
    activeBtn('recommend-btn');

    // The request parameters
    var url = './recommend';
    var params = '&lat=' + lat + '&lon=' + lon;

    var req = JSON.stringify({});

    // display loading message
    showLoadingMessage('Loading recommended items...');

    sendRequest(
        'GET',
        url + '?' + params,
        req,
        // successful callback
        function(res) {
          var events = JSON.parse(res);
          if (!events || events.length === 0) {
            showWarningMessage('No recommended event. Make sure you have favourites.');
          } else {
            listEvents(events);
          }
        },
        // failed callback
        function() {
          showErrorMessage('Cannot load recommended events.');
        });
  }

function listEvents(events) {
  // Clear the current results
  var eventList = $("#event-list");
  eventList.empty();

  for (var i = 0; i < events.length; i++) {
    addEvent(eventList, events[i]);
  }
}

function addEvent(eventList, event) {
  var event_id = event.event_id;

  var li = $("<li></li>", {
    id: 'event-' + event_id,
    class: "event"
  });

  // set the data attribute
  li.data("event_id", event_id);
  li.data("favourite", event.favourite);

  // event image
  if (event.image_url) {
    var img = $("<img></img>", {
      src: event.image_url
    });
    li.append(img);
  } else {
    var subImg = $("<img></img>", {
      src: 'https://assets-cdn.github.com/images/modules/logos_page/GitHub-Mark.png'
    });
    li.append(subImg);
  }
  // section
  var section = $("<div></div>");

  // title
  var title = $("<a></a>", {
    href: event.url,
    target: '_blank',
    class: 'event-name'
  });
  title.text(event.name);
  section.append(title);

  // genre
  var genre = $("<p></p>", {
    class: 'event-genre'
  });
  genre.text('Genre: ' + event.genres.join(', '));
  section.append(genre);

  // date
  var date = $("<p></p>", {
    class: 'event-date'
  });
  date.text(event.date);
  section.append(date);

  li.append(section);

  // address
  var address = $("<p></p>", {
    class: 'event-address'
  });
  address.text(event.address.replace(/,/g, '<br/>').replace(/\"/g,
      ''));
  li.append(address);

  // favourite link
  var favLink = $("<p></p>", {
    id: 'fav-link-' + event_id,
    class: 'fav-link'
  });

  favLink.click(function() {
    changeFavouriteEvent(event_id);
  });

  var icon = $("<i></i>", {
    id: 'fav-icon-' + event_id,
    class: event.favourite ? 'fa fa-heart fa' : 'fa fa-heart-o fa'
  });

  favLink.append(icon);
  li.append(favLink);

  eventList.append(li);
}

function sendRequest(method, url, data, callback, errorHandler) {
  var xhr = new XMLHttpRequest();

  xhr.open(method, url, true);

  xhr.onload = function() {
    if (xhr.status === 200) {
      callback(xhr.responseText);
    } else {
      errorHandler();
    }
  };

  xhr.onerror = function() {
    console.error("The request couldn't be completed.");
    errorHandler();
  };

  if (data === null) {
    xhr.send();
  } else {
    xhr.setRequestHeader("Content-Type", "application/json;charset=utf-8");
    xhr.send(data);
  }
}

  function activeBtn(btnId) {
    var btns = document.getElementsByClassName('main-nav-btn');

    // deactivate all navigation buttons
    for (var i = 0; i < btns.length; i++) {
      btns[i].className = btns[i].className.replace(/\bactive\b/, '');
    }

    // active the one that has id = btnId
    var btn = document.getElementById(btnId);
    btn.className += ' active';
  }

function showLoadingMessage(msg) {
  var eventList = document.getElementById('event-list');
  eventList.innerHTML = '<p class="notice"><i class="fa fa-spinner fa-spin"></i> ' +
      msg + '</p>';
}

function showWarningMessage(msg) {
  var eventList = document.getElementById('event-list');
  eventList.innerHTML = '<p class="notice"><i class="fa fa-exclamation-triangle"></i> ' +
      msg + '</p>';
}

function showErrorMessage(msg) {
  var eventList = document.getElementById('event-list');
  eventList.innerHTML = '<p class="notice"><i class="fa fa-exclamation-circle"></i> ' +
      msg + '</p>';
}

init();

})();