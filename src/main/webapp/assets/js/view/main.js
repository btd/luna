define(["jquery", "underscore", "backbone", "model/state", "luna", "view/root-wiki"], 
  function($, _, Backbone, state, Luna, RootWiki) {

  var mainView = Backbone.View.extend({
    el: "#main",

    initialize: function() {
      
    },

    render: function() {
      
      return this;
    },

    showWiki: function(getContent) {
      var self = this;
      getContent(function(content) {
        self.subView = new RootWiki({ content: content });
      });
    
      return self;
    },

    showUserPage: function() {
      var self = this;
      console.log("user page");
      return self;
    },

    clean: function() {
      this.subView && this.subView.clean();
      return this;
    }
  });
  return mainView;
});