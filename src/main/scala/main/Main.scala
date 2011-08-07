/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package main

import sshd.SshDaemon
import com.twitter.querulous.evaluator.QueryEvaluator
import com.twitter.util.Eval
import com.twitter.querulous.config.Connection
import java.io.File


object Main extends Application {
 /* val evaluator = QueryEvaluator("org.h2.Driver", "jdbc:h2:~/test", "sa", "")
  //evaluator.execute("create TABLE tb_test (i INT)");

  //evaluator.insert("insert into tb_test(i) values (1)");

  println("Data from table " + i)*/
  SshDaemon.start()
}