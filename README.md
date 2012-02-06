Luna
=========

If you want to have something like github but on your own server (and you have no 5000$) you can use this application.

What it can
---------------------

 + Manage users. User registration, collaborators on repos
 + Manage ssh public keys. You can add any number of keys for yourself or only for one repo
 + Repositories. Can be public or private. You can add collaborator to repository.
 + Forking. You can fork any visible for you repository and make pull requests for owner
 + Ssh read+write access, git protocol anonimous read access
 + Mail notification about push (in practice it is not difficult to add other)

Todo
---------------------

 + SubGit integration (tmate guys said that help)
 + Smart HTTP protocol support

Installation
---------------------

Assume you already have a server with any java web server (i use jetty) and mongodb 2.0 (yes as storage it uses mongo). 

Source building

``` bash
$ git clone git://github.com/btd/luna.git
$ cd luna
$ ./sbt package
```

Now use ./target/scala-2.9.1.final/luna-tool_2.9.1-{VERSION}.war.

License
---------------------

Copyright 2011 Bardadym Denis

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0