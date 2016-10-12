package rever.search

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.twitter.finagle.http.Status
import com.twitter.finatra.http.EmbeddedHttpServer
import com.twitter.inject.server.FeatureTest
import rever.search.domain.RegisterTemplateRequest
import rever.search.service.SearchService

/**
 * Created by zkidkid on 10/11/16.
 */
class SearchFeatureTest extends FeatureTest {

  val mapper: ObjectMapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  override protected def server = new EmbeddedHttpServer(twitterServer = new Server)


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
        headers = Map("token" ->"b07c7deb733fd52df20fc26cda23e1a0"),
        andExpect = Status.Ok,
        withJsonBody =
          """
            {
              "code":"succeed"
            }
          """.stripMargin)
    }
  }
}
