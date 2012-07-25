define(["jquery", "underscore", "backbone"], function($, _, Backbone) {
  var User = Backbone.Model.extend({
    idAttribute: "_id"
  });
  return User;
});