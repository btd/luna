require.config({
  baseUrl: "/assets/js",
  shim: {
    'backbone': {
      deps: ['underscore', 'jquery'],
      exports: 'Backbone'
    },
    'underscore': {
      exports: '_'
    },
    'bootstrap': ['jquery'],
    'lib/serializeObject.jquery': ['jquery']
  },
  paths: {
    "jquery": "lib/jquery",
    "underscore": "lib/underscore",
    "backbone": "lib/backbone",
    "text": "lib/text",
    "bootstrap": "lib/bootstrap",
    "luna": "lib/luna"
  }
});

require( ["app", "jquery"],
  function(App, $) {
    $(function(){
      App.init();
    });
  }
);