package rever.search

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.twitter.finagle.http.Status
import com.twitter.finatra.http.EmbeddedHttpServer
import com.twitter.finatra.thrift.ThriftClient
import com.twitter.inject.server.FeatureTest
import rever.search.domain.{IndexRequest, SuggestParam, SuggestRequest}

/**
 * Created by zkidkid on 10/21/16.
 */
class SuggestFeatureTest extends FeatureTest {
  val mapper: ObjectMapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)
  val writer = mapper.writerWithDefaultPrettyPrinter()

  override protected val server = new EmbeddedHttpServer(twitterServer = new Server) with ThriftClient

  "[HTTP] Suggestion " should {
    "index data " in {
      val temp = IndexRequest("tweet", "100", mapper.writeValueAsString(
        Map(
          "message" -> "elon",
          "uuid" -> "100",
          "message_completion" -> Map(
            "input" -> List("solar city", "space x"),
            "output" -> "elon musk",
            "payload" -> Map("id" -> 1),
            "weight" -> 100
          ),
          "uuid_completion" -> Map(
            "input" -> List("1234", "spacex"),
            "output" -> "elon musk id",
            "payload" -> Map("id" -> "uuid_1"),
            "weight" -> 50
          )
        )
      ))
      val resp = server.httpPut(path = "/index",
        putBody = writer.writeValueAsString(temp),
        headers = Map("token" -> "b07c7deb733fd52df20fc26cda23e1a0"),
        andExpect = Status.Created)
      println(resp)

    }
    "refresh data" in {
      server.httpPost(path = "/refresh",
        headers = Map("token" -> "b07c7deb733fd52df20fc26cda23e1a0"),
        postBody = "",
        andExpect = Status.Ok
      )
    }

    "suggest data" in {
      val temp = SuggestRequest(List(SuggestParam("message", "solar")))
      server.httpPost("/suggest",
        postBody = writer.writeValueAsString(temp),
        withJsonBody =
          """
             {
              "code" : "succeed",
              "data" : {
                "message": [
                  {
                    "text": "solar",
                    "offset": 0,
                    "length": 5,
                    "options": [
                      {
                        "text": "elon musk",
                        "score": 100.0,
                        "payload": {
                          "id": 1
                        }
                      }
                    ]
                  }
                ]
              }
            }
          """
      )
    }
  }


}
