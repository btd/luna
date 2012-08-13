define(["jquery", "underscore", "backbone", "model/state", "luna", "view/root-wiki", "view/user-repo-list", "collection/repository"], 
  function($, _, Backbone, state, Luna, RootWikiView, RepoListView, RepositoryList) {

  var mainView = Backbone.View.extend({
    el: "#main",

    initialize: function() {
      
    },

    render: function() {
      
      return this;
    },

    showWiki: function(subViewCallback) {
      var self = this;
      subViewCallback(function(content) {
        self.clean();
        self.subView = new RootWikiView({ content: content });
      });
      return self;
    },

    showUserPage: function(userName, subViewCallback) {
      var self = this;
      subViewCallback(function(content) {
        self.clean();
        self.subView = new RepoListView({ collection: new RepositoryList(content), userName: userName});
      });
      return self;
    },

    clean: function() {
      this.subView && this.subView.clean();
      return this;
    }
  });
  return mainView;
});