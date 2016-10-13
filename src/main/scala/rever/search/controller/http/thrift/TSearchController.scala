package rever.search.controller.http.thrift

import javax.inject.Inject

import com.twitter.finatra.thrift.Controller
import rever.rever.search.service.CommonSearch
import rever.rever.search.service.CommonSearch.Index
import rever.search.domain.ThriftImplicit.t2IndexRequest
import rever.search.service.SearchService

/**
 * Created by zkidkid on 10/13/16.
 */
class TSearchController @Inject()(searchService: SearchService) extends Controller with CommonSearch.BaseServiceIface {

  override val index = handle(Index) {
    args: Index.Args => {
      searchService.index(args.indexRequest).map(_ => true)
    }
  }


}
