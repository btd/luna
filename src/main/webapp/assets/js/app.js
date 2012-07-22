define(["model/state", "view/auth-form"], function(state, AuthForm) {
  return {
    init: function() {
      state.set({
        authForm: new AuthForm
      });
    }
  }
});