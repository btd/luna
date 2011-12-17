/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package sshd

import java.security.PublicKey
import org.apache.commons.codec.binary.Base64
import org.eclipse.jgit.lib.Constants
import org.apache.sshd.common.util.Buffer
import code.model.SshKeyBase

/**
 * Created by IntelliJ IDEA.
 * User: den
 * Date: 07.08.11
 * Time: 17:35
 * To change this template use File | Settings | File Templates.
 */

object SshUtil {

  def parse(key:SshKeyBase[_]): PublicKey = {
    val bin = Base64.decodeBase64(Constants.encodeASCII(key.encodedKey))
    new Buffer(bin).getRawPublicKey();
  }
}