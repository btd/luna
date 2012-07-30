define(["jquery", "underscore", "backbone", "model/state", "luna"], 
  function($, _, Backbone, state, Luna) {

  var rootWiki = Backbone.View.extend({
    el: "#content",

    initialize: function() {
      this.render();
    },

    render: function() {
      this.$el.append(this.options.content);
      return this;
    },

    clean: function() {
      this.$el.empty();
      return this;
    }
  });
  return rootWiki;
});