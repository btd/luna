/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package entity

/**
 * Created by IntelliJ IDEA.
 * User: denis.bardadym
 * Date: 8/4/11
 * Time: 3:50 PM
 * To change this template use File | Settings | File Templates.
 */

class Account(
               val id: Int,
               val email: String,
               val name: String,
               val passwd: String //TODO в будущем это будет хешированный пароль
               )

//TODO добавить companion object для доступа