/*
 * Copyright (c) 2011 Denis Bardadym
 * This project like github.
 * Distributed under Apache Licence.
 */

package main

import sshd.SshDaemon


object Main extends Application {
  SshDaemon.start()

}