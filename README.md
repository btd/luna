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

Now use `./target/scala-2.9.1.final/luna-tool_2.9.1-{VERSION}.war`. Default settings help you to start an app.

Configuration
======================

All configuration are optional. If one setting depends from others it will be written. 
*Next `/` in filepath means root of war package. `web.xml` means `/WEB-INF/web.xml`. and `props` means `/WEB-INF/classes/props/default.props`*.

*Check that props file uses ASCII encoding*

Lift
----------------

Props:

`run.mode` (`development`) - set it to production.

Session timeout
----------------------

To change default session timeout you need to add to (or change) web.xml:

``` xml
<session-config>
	<session-timeout>!!!time in minutes!!!</session-timeout>
</session-config>
```

MongoDB
-------------------------

In development i use version 2.0, but at 1.8 it works too.

Props settings:

`db.host` (`localhost`) - hostname of server where mongo located;

`dp.port` (`27017`) - port where mongo listen connections;

`db.name` (`grt`) - database name;

`db.user` - user for `db.name`. Depends on `db.password`;

`db.password` - password of `db.user`. Depends on `db.user`.


Stored paths
--------------------------

This is path in host fs where luna store own things.

Props settings:

`repository.dir` (`./repo/`) - where is user repositories is located. Make sure that this ends with `/`.

Git transport
---------------------------

Settings for luna supported transports:

`daemon.sshd.cert.path` (`./`) - where is sshd store it keys. Make sure that this ends with `/`.

`daemon.sshd.port` (`22`) - sshd port

`daemon.gitd.port` (`9418`) - gitd port

For http transport it uses web server port. If you want to use http make sure use HTTPS.

Notification
---------------------------

For mail notifications luna uses additional services.

`notification.url` - this is where luna-services listen connections. E.g. `http://localhost:8081/luna/services`

For notification service need to install other web app (*luna-services*). And you must have SMTP server.
In `luna-services/WEB-INF/applicationContext.xml` see 2 places:

``` xml
<route>
    <from uri="jetty:http://0.0.0.0:8081/luna/services/push"/>
    <unmarshal ref="gsonPushDataFormat"/>
    <bean ref="recipientListBean"/>
    <to uri="seda:pushEvents"/>
</route>
```

You need to set `uri` attr of `from` tag to be relevant that use set for `notification.url`.

Second is a email settings for gmail. You can do this for your own smtp.

``` xml
<route>
    <from uri="seda:emailOutput"/>
    <to uri="smtp:smtp.gmail.com:587?password=PASSWORD&amp;username=USERNAME&amp;mail.smtp.starttls.enable=true&amp;mail.smtp.auth=true&amp;mapMailMessage=false"/>
</route>
```

Fs repo deleter
--------------------------

This daemon delete repositories one time at day from filesystem of server.

It store trigger settings in `/WEB-INF/classes/quartz/job.xml`.

``` xml
<repeat-interval>There is time in millis</repeat-interval>
```

OS FAQ
=================================

Luna tested on windows and linux. There is my recomendations.

Windows:

 * Dont use in user repos not ASCII filenames (i mean local characters) will be crash
 * Install Mongo and Jetty as windows services

Linux (for jetty):

 * If u use jetty dont use in repos (it is very outdated). Use 7 or 8. Install it by hands, add as daemon.
 * If you want to use port numbers < 1024. Read this (http://wiki.eclipse.org/Jetty/Howto/Port80) how to do this.
 * Use something for reverse proxy (i use nginx)

Both (for jetty):

 * Optimaze it (http://wiki.eclipse.org/Jetty/Howto/Garbage_Collection, http://wiki.eclipse.org/Jetty/Howto/High_Load)
 * Use resourceBase for context config and extract war in webapps

Contributing
=================================
 
* Check out the latest master to make sure the feature hasn't been implemented or the bug hasn't been fixed yet
* Check out the issue tracker to make sure someone already hasn't requested it and/or contributed it
* Fork the project
* Start a feature/bugfix branch
* Commit and push until you are happy with your contribution (make sensitive changes)
* Discuss with me, if your changes are big
* Please try not to mess with the build files, version, or history. If you want to have your own version, or is otherwise necessary, that is fine, but please isolate to its own commit so I can cherry-pick around it.

License
===========

Copyright (c) 2011 Bardadym Denis. See LICENSE.txt for further details.