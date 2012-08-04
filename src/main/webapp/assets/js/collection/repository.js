define(["jquery", "underscore", "backbone", "model/repository"], function($, _, Backbone, Repository) {
  var RepositoryList = Backbone.Collection.extend({
    model: Repository
  });
  return RepositoryList;
});