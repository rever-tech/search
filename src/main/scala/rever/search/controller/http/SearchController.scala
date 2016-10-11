package rever.search.controller.http

import com.google.inject.Inject
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import rever.search.domain.{RegisterTemplateRequest, SearchRequest}
import rever.search.service.SearchService

/**
 * Created by zkidkid on 10/11/16.
 */
class SearchController @Inject()(searchService: SearchService) extends Controller {

  put("/template") {
    req: RegisterTemplateRequest => {
      response.ok(searchService.registerTemplate(req))
    }

  }

  get("/template") {
    req: Request => {

    }
  }

  get("/search") {
    req: SearchRequest => {
      response.ok(searchService.search(req))
    }
  }


}
