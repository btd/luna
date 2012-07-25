define(["jquery", "underscore", "backbone", "model/state", "luna"], 
  function($, _, Backbone, state, Luna) {

  var mainView = Backbone.View.extend({
    el: "#main",

    initialize: function() {
      
    },

    render: function() {
      
      return this;
    },

    showWiki: function() {
      var self = this;
      Luna.mainWiki(function(data) {
        self.$el.append(data.content);
      });
      return self;
    },

    showUserPage: function() {
      var self = this;
      console.log("user page");
      return self;
    },

    clean: function() {
      this.$el.empty();
      return this;
    }
  });
  return mainView;
});