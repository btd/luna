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
package code.snippet

import net.liftweb._
import http._
import common._
import util._
import Helpers._
import code.model._
import code.snippet.SnippetHelper._
import bootstrap.liftweb._
import xml._
import Utility._
import com.foursquare.rogue.Rogue._
import net.liftweb.http.rest._

object RawFileStreamingSnippet extends Loggable with RestHelper {


   serve {
    case Req(user :: repo :: "raw" :: ref :: path, //  path
             _, // suffix
             GetRequest) =>
      {
      	RepositoryDoc.byUserLoginAndRepoName(user, repo) match {
      		case Some(r) if r.canPull_?(UserDoc.currentUser) => {
			
   				SourceElement.find(r, ref, path) match {
   					
   					case Full(b @ Blob(_, _, _, size)) => 
                     //ugly way, but now ok
   						r.git.withSourceElementStream(path, ref) { in => 
   							StreamingResponse(in, 
                           () => {in.close()}, 
                           size, 
                           List("Content-Type" -> (if (b.image_?) "image/" + b.extname else if(b.binary_?) "application/octet-stream" else "text/plain"),
   						        "Content-Disposition" -> ("attachment; filename=" + b.name)), Nil, 200)
   						}
   					
						case _ => InMemoryResponse("".getBytes, List("Content-Type" -> "text/plain"), Nil, 404)
			      }
            }
      		
      		case _ => InMemoryResponse("".getBytes, List("Content-Type" -> "text/plain"), Nil, 404)
      	}
      
   	}
	}

}