define(["model/state", "view/auth-form", "router/main", "backbone", "view/main"], 
function(state, AuthForm, MainRouter, Backbone, MainView) {

  return {
    init: function() {
      state.set({
        authForm: new AuthForm, //move forms outside of state
        mainView: new MainView
      });

      var router = new MainRouter;
      
      Backbone.history.start({pushState: true});
    }
  }
});