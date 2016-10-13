package com.twitter.finatra

import com.twitter.finatra.http.internal.marshalling.mustache.MustacheMessageBodyWriter
import com.twitter.finatra.http.internal.marshalling.{FinatraDefaultMessageBodyReader, MessageBodyManager}
import com.twitter.finatra.http.marshalling.mustache.MustacheBodyComponent
import com.twitter.finatra.http.marshalling.{DefaultMessageBodyReader, DefaultMessageBodyWriter}
import com.twitter.finatra.response.Mustache
import com.twitter.inject.{Injector, InjectorModule, TwitterModule}
import rever.search.module.SearchResponseWriter

/**
 * Created by zkidkid on 10/13/16.
 */
object SearchMessageBodyModule extends TwitterModule {

  override val modules = Seq(InjectorModule)

  protected override def configure(): Unit = {
    super.configure()
    bindSingleton[DefaultMessageBodyReader].to[FinatraDefaultMessageBodyReader]
    bindSingleton[DefaultMessageBodyWriter].to[SearchResponseWriter]
  }

  override def singletonStartup(injector: Injector) {
    debug("Configuring MessageBodyManager")
    val manager = injector.instance[MessageBodyManager]
    manager.addByAnnotation[Mustache, MustacheMessageBodyWriter]
    manager.addByComponentType[MustacheBodyComponent, MustacheMessageBodyWriter]
  }
}
