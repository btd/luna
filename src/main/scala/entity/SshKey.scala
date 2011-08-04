/*
 * Copyright (c) 2011 Denis Bardadym
 * This project like github.
 * Distributed under Apache Licence.
 */

package entity

/**
 * Created by IntelliJ IDEA.
 * User: denis.bardadym
 * Date: 8/4/11
 * Time: 3:53 PM
 * To change this template use File | Settings | File Templates.
 */

class SshKey(
              val ownerId: Int,
              var value: String
              )

//TODO добавить companion object для доступа