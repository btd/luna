Luna
=========

If you want to have something like github but on your own server (and you have no 5000$) you can use this application.

What it can
==================

 + Manage users. User registration, collaborators on repos
 + Manage ssh public keys. You can add any number of keys for yourself or only for one repo
 + Repositories. Can be public or private. You can add collaborator to repository.
 + Forking. You can fork any visible for you repository and make pull requests for owner
 + Ssh read+write access, http read+write access (use HTTPS!!!), git protocol read access
 + Mail notification about push (in practice it is not difficult to add other)

Installation
====================

Assume you already have a server with any java web server (i use jetty) and mongodb 2.0 (yes as storage it uses mongo). 

Easiest way

``` bash
$ git clone git://github.com/btd/luna.git
$ cd luna
$ ./sbt package
```

Now use ./target/scala-2.9.1.final/luna-tool_2.9.1-{VERSION}.war. All default settings help yout to start an app.

Configuration
======================

All configuration are optional. If one setting depends from others it will be written. 
*Next / in filepath means root of war package. web.xml means /WEB-INF/web.xml. and props means /WEB-INF/classes/props/default.props*

Session timeout
----------------------

To change default session timeout you need to add to (or change) web.xml
``` xml
<session-config>
	<session-timeout>!!!time in minutes!!!</session-timeout>
</session-config>
```

MongoDB
-------------------------

In development i use version 2.0, but at 1.8 it works too.


Contributing
=================================
 
* Check out the latest master to make sure the feature hasn't been implemented or the bug hasn't been fixed yet
* Check out the issue tracker to make sure someone already hasn't requested it and/or contributed it
* Fork the project
* Start a feature/bugfix branch
* Commit and push until you are happy with your contribution
* Discuss with me, if your changes are big
* Please try not to mess with the build files, version, or history. If you want to have your own version, or is otherwise necessary, that is fine, but please isolate to its own commit so I can cherry-pick around it.

License
===========

Copyright (c) 2011 Bardadym Denis. See LICENSE.txt for further details.