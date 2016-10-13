package rever.search.domain

import rever.rever.search.service.TIndexRequest

/**
 * Created by zkidkid on 10/13/16.
 */
object ThriftImplicit {
  implicit def t2IndexRequest(tIndexRequest: TIndexRequest): IndexRequest = {
    IndexRequest(tIndexRequest.`type`, tIndexRequest.id.get, tIndexRequest.source)
  }
  implicit def indexRequest2T(indexRequest: IndexRequest): TIndexRequest = {
    TIndexRequest(indexRequest.types,indexRequest.source,Some(indexRequest.id))
  }
}
