package rever.search.service

import java.net.InetAddress
import javax.inject.Singleton

import com.google.inject.Provides
import com.twitter.inject.Logging
import com.twitter.util.Future
import com.typesafe.config.ConfigRenderOptions
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse
import org.elasticsearch.action.bulk.BulkResponse
import org.elasticsearch.action.delete.DeleteResponse
import org.elasticsearch.action.get.GetResponse
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.action.indexedscripts.put.PutIndexedScriptResponse
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.suggest.SuggestResponse
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.index.query.TemplateQueryBuilder
import org.elasticsearch.script.ScriptService.ScriptType
import org.elasticsearch.script.Template
import org.elasticsearch.search.suggest.SuggestBuilders
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder
import rever.client4s.Elasticsearch._
import rever.search.domain.{RegisterTemplateRequest, SearchRequest, SuggestRequest}
import rever.search.util.ZConfig

import scala.collection.JavaConversions._
import scala.collection.mutable

/**
 * Created by zkidkid on 10/11/16.
 */

@Singleton
@Provides
class SearchService extends Logging {


  private val clusterName = ZConfig.getString("elasticsearch.cluster", "elasticsearch")
  private lazy val indexName = ZConfig.getString("elasticsearch.indexName", "common-search")
  private val servers = ZConfig.getStringList("elasticsearch.servers", List("0.0.0.0:9300"))

  private val scriptIndex = ".scripts"
  private val scriptType = "mustache"
  private val mapCompletion = new mutable.HashMap[String, CompletionSuggestionBuilder]()
  private val settings = Settings.builder()
    .put("cluster.name", clusterName)
    .put("client.transport.sniff", ZConfig.getBoolean("elasticsearch.sniff", false))
    .build()
  private val client = TransportClient.builder()
    .settings(settings)
    .build()

  servers map (x => x.split(':')) filter (_.size == 2) foreach (hostAndPort => {
    info(s"Add ${hostAndPort.mkString(":")} to transport address")
    client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(hostAndPort(0)), hostAndPort(1).toInt))

  })
  val isIndexExist = client.admin().indices().prepareExists(indexName).get().isExists
  val deleteIfExist = ZConfig.getBoolean("elasticsearch.deleteIfExist", false)
  if (isIndexExist && deleteIfExist) {
    client.admin().indices().prepareDelete(indexName).get()
    Thread.sleep(1000)
  }

  if (deleteIfExist || !isIndexExist) {
    prepareIndex()
    prepareTemplate()
    prepareCompletion()
  }


  private[this] def prepareIndex(): Unit = {
    info(s"Prepare Create Index ${indexName}")
    val prepareIndex = client.admin().indices().prepareCreate(indexName)
    val indexConfig = ZConfig.config.getConfig("elasticsearch.index")
    val indexSettings = indexConfig.getObject("settings").render(ConfigRenderOptions.concise())
    info(s"--> Index Settings $indexSettings")
    prepareIndex.setSettings(indexSettings)
    for (indexType <- indexConfig.getObject("mappings").keySet()) {
      val mapping = indexConfig.getObject(s"mappings.${indexType}").render(ConfigRenderOptions.concise())
      info(s"--> Add type ${indexType}")
      info(s"--> With mapping ${mapping}")
      prepareIndex.addMapping(indexType, mapping)
    }
    if (prepareIndex.get().isAcknowledged == false) {
      throw new Exception("prepare index environment failed")
    }
  }

  private[this] def prepareTemplate(): Unit = {
    info("Prepare Init Template")

    val templateConfig = ZConfig.config.getObject("elasticsearch.templates")

    for (tplName <- templateConfig.keySet()) {
      val tplSource = templateConfig.get(tplName).render(ConfigRenderOptions.concise())
      val ret = client.preparePutIndexedScript(scriptType, tplName, tplSource).get
      info(s"--> Added ${tplName} created: ${ret.isCreated}")
    }
  }

  private[this] def prepareCompletion(): Unit = {
    info("Prepare Completion")
    val completionConfig = ZConfig.config.getObject("elasticsearch.autocompletion")
    for (completionName <- completionConfig.keySet()) {
      info(s"--> Add ${completionName} template")
      val field = ZConfig.getString(s"elasticsearch.autocompletion.${completionName}.field")
      val builder = SuggestBuilders.completionSuggestion(completionName).field(field)
      mapCompletion.add(completionName, builder)
    }
  }


  def registerTemplate(registerTpl: RegisterTemplateRequest): Future[PutIndexedScriptResponse] = {
    client.preparePutIndexedScript(scriptType, registerTpl.tplName, registerTpl.tplSource).asyncGet()
  }

  def getTemplate(tplName: String): Future[GetResponse] = {
    client.prepareGet().setIndex(scriptIndex).setType(scriptType).setId(tplName).asyncGet()
  }

  def registerMapping(types: String, mapping: String): Future[PutMappingResponse] = {
    client.admin().indices().preparePutMapping(indexName).setType(types).setSource(mapping).asyncGet()
  }

  def registerMapping(mapping: String): Future[PutMappingResponse] = {
    client.admin().indices().preparePutMapping(indexName).setSource(mapping).asyncGet()
  }

  def index(req: rever.search.domain.IndexRequest): Future[IndexResponse] = {
    index(req.types, req.id, req.source)
  }

  def index(types: String, id: String, source: String): Future[IndexResponse] = {
    client.prepareIndex(indexName, types, id).setSource(source).asyncGet()
  }

  def index(types: String, sources: Array[String]): Future[BulkResponse] = {
    val bulk = client.prepareBulk()
    sources foreach (source => {
      bulk.add(client.prepareIndex(indexName, types).setSource(source))
    })
    bulk.asyncGet()
  }

  def delete(types: String, id: String): Future[DeleteResponse] = {
    client.prepareDelete(indexName, types, id).asyncGet()
  }

  def search(searchTemplate: SearchRequest): Future[SearchResponse] = {
    val templateQueryBuilder = new TemplateQueryBuilder(new Template(searchTemplate.name, ScriptType.INDEXED, "mustache", null, searchTemplate.params))
    client.prepareSearch()
      .setIndices(indexName)
      .setTypes(searchTemplate.types: _*)
      .setQuery(templateQueryBuilder)
      .asyncGet()
  }

  def autocomplete(suggestRequest: SuggestRequest): Future[SuggestResponse] = {
    val builder = client.prepareSuggest(indexName)
    for (suggest <- suggestRequest.suggests) {
      mapCompletion.get(suggest.name) match {
        case None => throw new UnsupportedOperationException(s"Unsupported ${completionName}")
        case Some(tpl) => {
          builder.addSuggestion(tpl.text(suggest.text))
        }
      }
    }
    builder.asyncGet()
  }


}
