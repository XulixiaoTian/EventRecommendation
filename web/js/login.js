function onSignIn(googleUser) {

  var profile = googleUser.getBasicProfile();

  $('#welcome-msg').text('Welcome, ' + profile.getName());

  var user = $('#user');
  user.empty();

  var userImg = $("<img></img>", {
    id: "user-img",
    src: profile.getImageUrl()
  });
  user.append(userImg);

  var id_token = googleUser.getAuthResponse().id_token;
  console.log("ID Token: " + id_token);
  Cookies.set('idToken', id_token);
  var xhr = new XMLHttpRequest();
  xhr.open('POST', './login');
  xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
  xhr.onload = function() {
    console.log('Signed in as: ' + xhr.responseText);
  };

  xhr.send();

  $("#signin").hide();
  $("#signout").show();

  $("#nearby-btn").click();
}

function signOut() {
  var auth2 = gapi.auth2.getAuthInstance();
  auth2.signOut().then(function () {
    console.log('User signed out.');
  });
  Cookies.remove('idToken');

  $("#signin").show();
  $("#signout").hide();

  $('#welcome-msg').text('');

  var eventList = $("#event-list");
  eventList.empty();

  var user = $('#user');
  user.empty();
  user.append($('<i id="avatar" class="avatar fa fa-user fa-2x"></i>'));
}