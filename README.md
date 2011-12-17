WARNING Ssh keys 18 Dec 2011
-----------------------------

Ssh keys are splited on two collections. To migrate from prev versions move all keys for users to collection ssh_keys_user and ssh keys for repos move to ssh_keys_repo.

Using mongo client:

```javascript
db grt;
db.createCollection("ssh_keys_repo");
db.createCollection("ssh_keys_user");
db.ssh_keys.find({ownerRepoId:{$exists: false}}).forEach( function(x){db.ssh_keys_user.insert(x)} );
db.ssh_keys.find({ownerRepoId:{$exists: true}}).forEach( function(x){db.ssh_keys_repo.insert({_id:x._id, rawValue:x.rawValue, ownerId:x.ownerRepoId})} );
db.ssh_keys.drop();
```

LUNA-TOOL
=========

If you want to have something like github but on your own server (and you have no 5000$) you can use this application.

What it can
---------------------

 + Manage users. User registration, collaborators on repos
 + Manage ssh public keys. You can add any number of keys for yourself or only for one repo
 + Repositories. Can be public or private. You can add collaborator to repository.
 + Forking. You can fork any visible for you repository and make pull requests for owner
 + Ssh read+write access, git protocol anonimous read access

Installation
---------------------

Assume you already have a server with any java web server (i use jetty) and mongodb 2.0 (yes as storage it uses mongo). 

Source building

``` bash
$ git clone git://github.com/btd/gct.git
$ cd gct
$ ./sbt package-war
```

Now use ./target/scala-2.9.1.final/luna-tool_2.9.1-{VERSION}.war.

License
---------------------

Copyright 2011 Bardadym Denis

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0