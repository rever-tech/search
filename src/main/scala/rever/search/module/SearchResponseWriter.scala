package rever.search.module

import javax.inject.Inject

import com.google.common.net.MediaType._
import com.twitter.finatra.http.marshalling.{DefaultMessageBodyWriter, WriterResponse}
import com.twitter.finatra.json.FinatraObjectMapper
import com.twitter.inject.Logging
import org.apache.commons.lang.ClassUtils
import org.elasticsearch.action.get.GetResponse
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.action.indexedscripts.put.PutIndexedScriptResponse
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.suggest.SuggestResponse
import org.elasticsearch.common.xcontent.XContentFactory

/**
 * Created by zkidkid on 10/11/16.
 */

class SearchResponseWriter @Inject()(mapper: FinatraObjectMapper) extends DefaultMessageBodyWriter with Logging {
  val SUCCEED = "succeed"
  val FAILED = "failed"
  val UNKNOWN = "unknown"

  override def write(obj: Any): WriterResponse = {
    if (isPrimitiveOrWrapper(obj.getClass))
      WriterResponse(PLAIN_TEXT_UTF_8, obj.toString)
    else {
      obj match {
        case indexedResponse: PutIndexedScriptResponse => {
          WriterResponse(JSON_UTF_8, Map("code" -> SUCCEED))
        }
        case getResp: GetResponse => {
          WriterResponse(JSON_UTF_8, Map("code" -> SUCCEED, "data" -> getResp.getSourceAsMap))
        }
        case indexResp: IndexResponse => {
          WriterResponse(JSON_UTF_8, Map("code" -> SUCCEED, "data" -> Map("id" -> indexResp.getId, "created" -> indexResp.isCreated, "version" -> indexResp.getVersion)))
        }
        case searchResp: SearchResponse => {

          val data = scala.collection.mutable.HashMap[String, Any](
            "scroll_id" -> searchResp.getScrollId,
            "total_hit" -> searchResp.getHits.getTotalHits,
            "hits" -> searchResp.getHits.hits
          )
          if (searchResp.getAggregations != null) {
            data += ("aggregation" -> searchResp.getAggregations.asMap())
          }

          WriterResponse(JSON_UTF_8, Map("code" -> SUCCEED, "data" -> data))
        }
        case suggestResp: SuggestResponse => {
          val builder = XContentFactory.jsonBuilder
          builder.startObject
          suggestResp.getSuggest.toXContent(builder, org.elasticsearch.common.xcontent.ToXContent.EMPTY_PARAMS)
          builder.endObject

          WriterResponse(JSON_UTF_8, Map("code" -> SUCCEED, "data" -> mapper.parse[Map[String, Any]](builder.string())))
        }
        case ex: Exception => {
          error(ex)
          WriterResponse(JSON_UTF_8, Map("code" -> FAILED, "message" -> ex.getMessage))
        }
        case any: Any => {
          WriterResponse(JSON_UTF_8, mapper.writeValueAsString(any))
        }
      }

    }
  }

  /* Private */

  // Note: The following method is included in commons-lang 3.1+
  private def isPrimitiveOrWrapper(clazz: Class[_]): Boolean = {
    clazz.isPrimitive || ClassUtils.wrapperToPrimitive(clazz) != null
  }
}
