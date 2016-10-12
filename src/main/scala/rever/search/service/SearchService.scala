package rever.search.service

import java.net.InetAddress
import javax.inject.Singleton

import com.google.inject.Provides
import com.twitter.inject.Logging
import com.twitter.util.Future
import org.elasticsearch.action.ActionResponse
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse
import org.elasticsearch.action.bulk.BulkResponse
import org.elasticsearch.action.delete.DeleteResponse
import org.elasticsearch.action.get.GetResponse
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.action.indexedscripts.put.PutIndexedScriptResponse
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.index.query.TemplateQueryBuilder
import org.elasticsearch.script.ScriptService.ScriptType
import org.elasticsearch.script.Template
import rever.client4s.Elasticsearch._
import rever.search.domain.{RegisterTemplateRequest, SearchRequest}
import rever.search.util.ZConfig

import scala.collection.JavaConversions._

/**
 * Created by zkidkid on 10/11/16.
 */
trait SearchService[I] {
  def registerTemplate(registerTpl: RegisterTemplateRequest): Future[I]

  def search(searchTemplate: SearchRequest): Future[I]
}

@Singleton
@Provides
class SearchServiceImpl extends SearchService[ActionResponse] with Logging {
  private val clusterName = ZConfig.getString("elasticsearch.clusterName", "elasticsearch")
  private val indexName = ZConfig.getString("elasticsearch.indexName", "default")
  private val servers = ZConfig.getStringList("elasticsearch.servers", List("0.0.0.0:9300"))

  private val scriptIndex = ".scripts"
  private val scriptType = "mustache"

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
    val templateQueryBuilder = new TemplateQueryBuilder(new Template(searchTemplate.tplName, ScriptType.INDEXED, "mustache", null, searchTemplate.tplParams))
    client.prepareSearch()
      .setIndices(indexName)
      .setTypes(searchTemplate.types: _*)
      .setQuery(templateQueryBuilder)
      .asyncGet()
  }


}
