package rever.search.module

import javax.inject.Singleton

import com.google.inject.{Guice, Provides}
import com.twitter.finatra.http.marshalling.DefaultMessageBodyWriter
import com.twitter.inject.TwitterModule
import rever.search.service.SearchService


/**
 * Created by zkidkid on 10/11/16.
 */
object DependencyModule extends TwitterModule {

  @Singleton
  @Provides
  def providesSearchService(): SearchService = {
    new SearchService()
  }
//
//  override protected def configure(): Unit = {
//    super.configure()
//
//    bindSingleton[DefaultMessageBodyWriter].to[SearchResponseWriter]
//  }
}
