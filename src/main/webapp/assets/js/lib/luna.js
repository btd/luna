define(['jquery'], function($) {
  var Luna = function() {
    var version = 1;
  
    return {
      setupFail: function(jqXHR, textStatus, errorThrown) {
        console.log(jqXHR.status + ": " + jqXHR.responseText);
      },
      authorize: function(data, success, error) {
        $.ajax({
          dataType: 'json',
          url: '/api/' + version + "/auth/token",
          data: data,
          type: 'GET'
        }).done(function(data) {
          this.access_token = data.access_token;
          success(data);
        }).fail(error || this.setupFail);
      },
      mainWiki: function(success) {
        $.ajax({
          dataType: 'json',
          url: '/api/' + version + "/wiki/root",
          type: 'GET'
        }).done(function(data) {
          success(data);
        })
      }
    }
  }();
  return Luna;
});
