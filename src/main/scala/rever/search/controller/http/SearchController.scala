package rever.search.controller.http

import com.google.inject.Inject
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import rever.search.domain.{IndexRequest, RegisterTemplateRequest, SearchRequest, SuggestRequest}
import rever.search.service.SearchService

/**
 * Created by zkidkid on 10/11/16.
 */
class SearchController @Inject()(searchService: SearchService) extends Controller {

  put("/template") {
    req: RegisterTemplateRequest => {
      for {
        ret <- searchService.registerTemplate(req)
      } yield response.created(ret)
    }
  }

  get("/template/:name") {
    req: Request => {
      for {
        getResponse <- searchService.getTemplate(req.params("name"))
      } yield response.ok(getResponse)
    }
  }

  post("/search") {
    req: SearchRequest => {
      for {
        searchResponse <- searchService.search(req)
      } yield response.ok(searchResponse)
    }
  }
  post("/suggest") {
    req: SuggestRequest => {
      for {
        suggestResponse <- searchService.suggest(req)
      } yield response.ok(suggestResponse)
    }
  }

  put("/index") {
    req: IndexRequest => {
      for {
        indexResponse <- searchService.index(req)
      } yield response.created(indexResponse)
    }
  }
  post("/refresh") {
    req: Request => {
      for {
        resp <- searchService.refresh()
      } yield response.ok()
    }
  }

}
