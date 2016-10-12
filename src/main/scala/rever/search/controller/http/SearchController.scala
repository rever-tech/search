package rever.search.controller.http

import com.google.inject.Inject
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import org.elasticsearch.action.ActionResponse
import rever.search.domain.{RegisterTemplateRequest, SearchRequest}
import rever.search.service.SearchService

/**
 * Created by zkidkid on 10/11/16.
 */
class SearchController @Inject()(searchServiceImpl: SearchService[ActionResponse]) extends Controller {

  put("/template") {
    req: RegisterTemplateRequest => {
      for {
        ret <- searchServiceImpl.registerTemplate(req)
      } yield response.ok(ret)
    }

  }

  get("/template") {
    req: Request => {

    }
  }

  get("/search") {
    req: SearchRequest => {
      response.ok(searchServiceImpl.search(req))
    }
  }


}
