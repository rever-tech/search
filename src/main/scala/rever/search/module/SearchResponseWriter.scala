package rever.search.module

import javax.inject.Inject

import com.google.common.net.MediaType
import com.twitter.finatra.http.marshalling.{MessageBodyWriter, WriterResponse}
import com.twitter.finatra.json.FinatraObjectMapper
import org.elasticsearch.action.indexedscripts.put.PutIndexedScriptResponse

/**
 * Created by zkidkid on 10/11/16.
 */
case class Response(code: String)

object SUCCESS extends Response("succeed")

object FAILED extends Response("failed")

class PutIndexedScriptResponseWriter @Inject()(mapper: FinatraObjectMapper) extends MessageBodyWriter[PutIndexedScriptResponse] {
  override def write(obj: PutIndexedScriptResponse): WriterResponse = {
    WriterResponse(MediaType.JSON_UTF_8,SUCCESS)
  }
}
