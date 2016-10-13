package rever.search

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.twitter.finagle.http.Status
import com.twitter.finatra.http.EmbeddedHttpServer
import com.twitter.finatra.thrift.ThriftClient
import com.twitter.inject.server.FeatureTest
import com.twitter.util.Future
import rever.rever.search.service.CommonSearch
import rever.search.domain.{IndexRequest, RegisterTemplateRequest, SearchRequest}

/**
 * Created by zkidkid on 10/11/16.
 */
class SearchFeatureTest extends FeatureTest {

  val mapper: ObjectMapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)
  val writer = mapper.writerWithDefaultPrettyPrinter()

  override protected def server = new EmbeddedHttpServer(twitterServer = new Server) with ThriftClient

  "[HTTP] SearchService " should {
    "Auto create index & type & template" in {
      server.httpGet(path = "/template/search-tweet",
        andExpect = Status.Ok,
        withJsonBody =
          """
            {
              "code" : "succeed",
              "data" : {
                "template" : {
                  "query_string" : {
                    "default_field" : "message",
                    "query" : "{{query_string}}"
                  }
                }
              }
            }
          """)
    }
    "return correct data with pre-index template" in {
      val temp = IndexRequest("tweet", "1", mapper.writeValueAsString(Map("message" -> "elon", "uuid" -> "1")))
      server.httpPut(path = "/index",
        putBody = writer.writeValueAsString(temp),
        andExpect = Status.Created)

      val searchRequest = SearchRequest("search-tweet", Array("tweet"), Map("query_string" -> "elon"))
      server.httpPost("/search",
        postBody = writer.writeValueAsString(searchRequest),
        andExpect = Status.Ok)
    }
  }

  "[HTTP] Put & Search Template " should {
    "put successful " in {
      val tplSource =
        """
          {
            "template":
            {
              "match":
              {
                "user": "{{query_string}}"
              }
            }
          }
        """
      val registerTemplateRequest = RegisterTemplateRequest("test", tplSource)
      server.httpPut("/template",
        putBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(registerTemplateRequest),
        headers = Map("token" -> "b07c7deb733fd52df20fc26cda23e1a0"),
        andExpect = Status.Created,
        withJsonBody =
          """
            {
              "code":"succeed"
            }
          """)
    }
  }

  "[Thrift] Client " should {
    lazy val client = server.thriftClient[CommonSearch[Future]](clientId = "thrift-test")
    "index data successful" in {
      val temp = IndexRequest("tweet", "2", mapper.writeValueAsString(Map("message" -> "bill", "uuid" -> "2")))
      import rever.search.domain.ThriftImplicit._
      assert(client.index(temp).value)

      val searchRequest = SearchRequest("search-tweet", Array("tweet"), Map("query_string" -> "bill"))
      server.httpPost("/search",
        postBody = writer.writeValueAsString(searchRequest),
        andExpect = Status.Ok)
    }
  }
}
