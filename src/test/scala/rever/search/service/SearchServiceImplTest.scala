package rever.search.service

import com.twitter.util.Await
import org.scalatest.{BeforeAndAfterEach, FunSuite}
import rever.search.domain.RegisterTemplateRequest
import com.twitter.util.TimeConversions._


/**
 * Created by zkidkid on 10/12/16.
 */
class SearchServiceImplTest extends FunSuite with BeforeAndAfterEach {

  val searchService = new SearchService


  test("testSearch") {
  val registerTpl: RegisterTemplateRequest = RegisterTemplateRequest("test",
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
    """)
    val asyncResp = searchService.registerTemplate(registerTpl)
    val result=Await.result(asyncResp, 5 seconds)
    println(s"Put template successful ${result.getIndex}/${result.getScriptLang}/${result.getId}")

  }

}
