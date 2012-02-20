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

import org.apache.sshd.common.Factory
import org.apache.sshd.server.{ExitCallback, Environment, Command => SshCommand}
import java.io.{InputStream, OutputStream}
import org.eclipse.jgit.lib.Constants

class NoShell extends Factory[SshCommand] {
  def create(): SshCommand = new SshCommand {
    private var in: InputStream = null
    private var out: OutputStream = null
    private var err: OutputStream = null

    private var callback: ExitCallback = null

    def setInputStream(in: InputStream) {
      this.in = in
    }

    def destroy() {}

    def setExitCallback(callback: ExitCallback) {
      this.callback = callback
    }

    def start(env: Environment) {
      val message = "Sorry but this server does not support ssh connections\n"
      err.write(Constants.encode(message));
      err.flush();

      in.close();
      out.close();
      err.close();
      callback.onExit(127);
    }

    def setErrorStream(err: OutputStream) {
      this.err = err
    }

    def setOutputStream(out: OutputStream) {
      this.out = out
    }
  }
}