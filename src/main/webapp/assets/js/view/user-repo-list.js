define(["jquery", "underscore", "backbone", "model/state", "luna", "text!template/repo-block"], 
  function($, _, Backbone, state, Luna, tplRepoBlock) {

  var TplRepoBlock = _.template(tplRepoBlock);

  var repoListView = Backbone.View.extend({

    initialize: function() {
      
    },

    render: function() {
      
      return this;
    },

    clean: function() {
      this.$el.empty();
      return this;
    }
  });
  return repoListView;
});