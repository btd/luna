/*
   Copyright 2012 Denis Bardadym

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package daemon.sshd

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