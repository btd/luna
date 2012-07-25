define(["jquery", "underscore", "backbone", "model/state", "luna"], 
  function($, _, Backbone, state, Luna) {

  var mainView = Backbone.View.extend({
    el: "#main",

    initialize: function() {
      //this.render();
    },

    render: function() {
      
      return self;
    },

    showWiki: function() {
      var self = this;
      Luna.mainWiki(function(data) {
        self.$el.append(data.content);
      });
      return self;
    },

    clean: function() {
      this.$el.empty();
      return this;
    }
  });
  return mainView;
});