define(['jquery'], function($) {
  var Luna = function() {
    var version = 1;

    var addToken = function(data, token) {
      if(token) {
        $.extend(data, {
          access_token: token
        });
      }
      return data;
    };
  
    return {
      setupFail: function(jqXHR, textStatus, errorThrown) {
        console.log(jqXHR.status + ": " + jqXHR.responseText);
      },

      getToken: function() {
        return this.access_token || (localStorage && localStorage.getItem("access_token"));
      },

      authorize: function(data, success, error) {
        var self = this;
        try {
          $.ajax({
            dataType: 'json',
            url: '/api/' + version + "/auth/token",
            data: addToken(data || {}, self.getToken()),
            type: 'GET'
          }).done(function(data) {
            self.access_token = data.access_token;
            localStorage && localStorage.setItem("access_token", self.access_token);
            success(data);
          }).fail(function(jqXHR, textStatus, errorThrown) {
            localStorage && localStorage.removeItem("access_token", self.access_token);
            (error || self.setupFail).apply(this, arguments)
          });
        } catch(exception) {
          
        }
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
