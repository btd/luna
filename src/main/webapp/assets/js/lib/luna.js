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
      }
    }
  }();
  return Luna;
});
