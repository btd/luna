define(["jquery", "underscore", "backbone"], function($, _, Backbone) {
  var Repository = Backbone.Model.extend({
    idAttribute: "_id"
  });
  return Repository;
});