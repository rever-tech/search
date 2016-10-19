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
    val templateName = "search-template-multi-filter"

    "put successful " in {
      val tplSource =
        """
          {
            "template": "{\"query\":{\"bool\":{\"filter\":[{{#toJson}}filters{{/toJson}}]}}}"
          }
        """
      val registerTemplateRequest = RegisterTemplateRequest(templateName, tplSource)
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

    "search successful" in {
      val temp = IndexRequest("tweet-complex", "3", mapper.writeValueAsString(
        Map(
          "key" -> "foo",
          "string_value" -> "hello, this is a string",
          "int_value" -> 6
        )))
      server.httpPut(path = "/index",
        putBody = writer.writeValueAsString(temp),
        andExpect = Status.Created)

      val searchRequest = SearchRequest(templateName, Array("tweet"),
        Map(
          "filters" -> Array(
            Map(
              "term" -> Map("string_value" -> "this")
            ),
            Map(
              "range" -> Map(
                "int_value" -> Map(
                  "gte" -> 3,
                  "lte" -> 6
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
  }
}

