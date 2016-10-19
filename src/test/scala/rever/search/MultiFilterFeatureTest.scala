package rever.search

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.twitter.finagle.http.Status
import com.twitter.finatra.http.EmbeddedHttpServer
import com.twitter.finatra.thrift.ThriftClient
import com.twitter.inject.server.FeatureTest
import rever.search.domain.{IndexRequest, RegisterTemplateRequest, SearchRequest}

/**
 * Created by phuonglam on 10/15/16.
 **/
class MultiFilterFeatureTest extends FeatureTest {
  val mapper: ObjectMapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)
  val writer = mapper.writerWithDefaultPrettyPrinter()

  override protected def server = new EmbeddedHttpServer(twitterServer = new Server) with ThriftClient

  "[HTTP] Put & Search Multiple Filter Template " should {
    val typ = "tweet-complex"
    val id = "2"

    "put successful" in {
      val temp = IndexRequest(typ, id, mapper.writeValueAsString(
        Map(
          "key" -> "foo",
          "string_value" -> "hello, this is a string",
          "int_value" -> 4
        )))
      server.httpPut(path = "/index",
        putBody = writer.writeValueAsString(temp),
        andExpect = Status.Created)
    }

    "search successful" in {
      val searchRequest = SearchRequest("search-json", Array(typ),
        Map(
          "filters" -> Array(
            Map(
              "term" -> Map("string_value" -> "this")
            ),
            Map(
              "range" -> Map(
                "int_value" -> Map(
                  "gte" -> 1,
                  "lte" -> 4
                )
              )
            )
          )
        )
      )
      println(writer.writeValueAsString(searchRequest))
      server.httpPost("/search",
        postBody = writer.writeValueAsString(searchRequest),
        andExpect = Status.Ok)
    }

    "delete successful" in {
      server.httpDelete(
        path = "/" + typ + "/" +id,
        andExpect = Status.Ok
      )
    }
  }
}

