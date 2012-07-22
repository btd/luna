define(["jquery", "underscore", "backbone"], function($, _, Backbone) {
  var State = Backbone.Model.extend({
    defaults: {
      access_token: ""
    },

    isLoggedIn: function() {
      return this.get("access_token") !== "";
    }
  });
  _(State).extend(Backbone.Events);
  var state = new State;
  return state;
});