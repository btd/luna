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

    var makeRequest = function(params, context) {
      var opts = $.extend({
        data : {},
        method : 'GET',
        error: context.setupFail
      }, params);

      if(opts.method === 'GET') {
        var params = $.param(addToken(opts.data, context.getToken()));
        opts.url = opts.url + (params ? '?' + params : "");
        opts.data = "";
      } else if (opts.method === 'POST') {
        var params = $.param({access_token: context.getToken()});
        opts.url = opts.url + (params ? '?' + params : "");
        opts.data = JSON.stringify(opts.data);
      }

      $.ajax({
        dataType: 'json',
        url: '/api/' + version + opts.url,
        data: opts.data,
        type: opts.method,
        contentType: 'application/json',
        processData: false
      }).done(function(data) {
        opts.success(data);
      }).fail(function(jqXHR, textStatus, errorThrown) {
        opts.error(jqXHR, textStatus, errorThrown);
      });
    };
  
    return {
      setupFail: function(jqXHR, textStatus, errorThrown) {
        console.log(jqXHR.status + ": " + jqXHR.responseText);
      },

      getToken: function() {
        return this.access_token || (localStorage && localStorage.getItem("access_token"));
      },

      authorize: function(data, callback, error) {
        var self = this;
        makeRequest({
          url: "/auth/token",
          data: data,
          success: function(data) {
            self.access_token = data.access_token;
            localStorage && localStorage.setItem("access_token", self.access_token);
            callback(data);
          },
          error: function(jqXHR, textStatus, errorThrown) {
            localStorage && localStorage.removeItem("access_token", self.access_token);
          }
        }, this);
      },
      mainWiki: function(callback) {
        makeRequest({
          success: callback,
          url: "/wiki/root"
        }, this);
      },
      userRepositories: function(userName, callback) {
        makeRequest({
          success: callback,
          url: "/user/" + userName + "/repositories"
        }, this);
      },
      createRepository: function(userName, data, callback) {
        makeRequest({
          success: callback,
          method: 'POST',
          data: data,
          url: "/user/" + userName + "/repositories"
        }, this);
      }
    }
  }();
  return Luna;
});
