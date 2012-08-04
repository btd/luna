define(["jquery", "underscore", "backbone", "model/state", "luna", "text!template/repository-block.js", "text!template/repository-list.js", "model/repository"], 
  function($, _, Backbone, state, Luna, tplRepoBlock, tplRepoList, Repository) {

  var TplRepoBlock = _.template(tplRepoBlock);
  var TplRepoList = _.template(tplRepoList);

  var repoListView = Backbone.View.extend({
    el: "#content",

    commonEvents: {
      "click .create-repository": "createNewRepository"
    },

    initialize: function(options) {
      this.collection = options.collection;
      this.userName = options.userName;
      this.render();
    },

    createNewRepository: function() {
      val newRepo = new Repository({
        name: $("#new-repository-name").val(),
        isPublic: $("#new-repository-public").prop("checked")
      });
      if(state.get("user").get("name") === this.userName && newRepo.isValid()) {
        Luna.createRepository(newRepo.toJSON(), function(data) {
          newRepo.set(data);
          this.collection.add(newRepo);
        });
      }
    },

    render: function() {
      var self = this;
      this.$el.append(TplRepoList());
      var $list = this.$el.find(".repo-list");
      _(self.collection.models).each(function(repo) {
        $list.append(TplRepoBlock({repo: repo}));
      });
      this.delegateEvents(this.commonEvents);
      return this;
    },

    clean: function() {
      this.$el.empty();
      return this;
    }
  });
  return repoListView;
});