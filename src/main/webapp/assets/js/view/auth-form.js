define(["jquery", "underscore", "backbone", "model/user", "text!template/auth-form-not-loggedin.js", "text!template/auth-form-loggedin.js", "model/state", "luna", "bootstrap", "lib/serializeObject.jquery"], 
  function($, _, Backbone, User, tplAuthNotLoggedIn, tplAuthLoggedIn, state, Luna) {
  var TplAuthFormNotLoggedIn = _.template(tplAuthNotLoggedIn);
  var TplAuthFormLoggedIn = _.template(tplAuthLoggedIn);


  var authForm = Backbone.View.extend({
    el: "#authorization-form",

    loginEvents: {
      "click .login_btn" : "tryAuthorize"
    },

    initialize: function() {
      state.on("login", function(user) {
        this.clean().render(user);
      }, this);

      this.render();
    },

    tryAuthorize: function() {
      Luna.authorize(this.$(".auth-dropdown-form").serializeObject(), function(data) {
        state.logUserIn(new User(data.user));
      });

      return false;
    },

    render: function(user) {
      var self = this;
      if(state.isLoggedIn()) {
        this.$el.append(TplAuthFormLoggedIn({"user": user}));
      } else {
        this.$el.append(TplAuthFormNotLoggedIn());
        this.$el.find(".dropdown-toggle").dropdown();
        this.delegateEvents(this.loginEvents);
      }
      return self;
    },

    clean: function() {
      this.undelegateEvents();
      this.$el.empty();
      return this;
    }
  });
  return authForm;
});