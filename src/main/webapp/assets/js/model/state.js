define(["jquery", "underscore", "backbone"], function($, _, Backbone) {
  var State = Backbone.Model.extend({
    defaults: {
      
    },

    isLoggedIn: function() {
      return this.has("user");
    },
    logUserIn: function(user) {
      this.set("user", user);
      this.trigger("login", user);
    }
  });
  _(State).extend(Backbone.Events);
  var state = new State;
  return state;
});