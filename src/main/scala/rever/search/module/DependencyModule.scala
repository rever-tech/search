package rever.search.module

import javax.inject.Singleton

import com.google.inject.Provides
import com.twitter.inject.TwitterModule
import org.elasticsearch.action.ActionResponse
import org.elasticsearch.action.indexedscripts.put.{PutIndexedScriptRequest, PutIndexedScriptResponse}
import org.elasticsearch.action.search.SearchResponse
import rever.search.service.{SearchService, SearchServiceImpl}

/**
 * Created by zkidkid on 10/11/16.
 */
object DependencyModule extends TwitterModule{

  @Singleton
  @Provides
  def providesSearchService(): SearchService[ActionResponse]={
    new SearchServiceImpl
  }

}
