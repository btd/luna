define(["backbone", "model/state"], function(Backbone, state) {

  //main and alone...
  var MainRouter = Backbone.Router.extend({

    initialize: function() {
      this.on("all", function(eventName) {
        this.previousRouteEvent = eventName;
      }, this);
    },

    routes: {
      "":                 "root"
    },

    root: function() {
      state.get("mainView").clean().showWiki();
      state.get("authForm").render();
    }

  });
  return MainRouter;
});