package rever.search.service

import java.net.InetAddress

import com.twitter.util.Future
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
class SearchService {
  private val clusterName = ZConfig.getString("elasticsearch.clusterName", "elasticsearch")
  private val indexName = ZConfig.getString("elasticsearch.indexName", "default")
  private val servers = ZConfig.getStringList("elasticsearch.servers", List("127.0.0.1:9300"))

  private val scriptIndex = ".scripts"
  private val scriptType = "mustache"

  private val settings = Settings.builder()
    .put("cluster.name", clusterName)
    .put("client.transport.sniff", ZConfig.getBoolean("elasticsearch.sniff", true))
    .build()
  private val client = TransportClient.builder()
    .settings(settings)
    .build()
  servers map (x => x.split(':')) filter (_.size != 2) foreach (hostAndPort => {
    client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(hostAndPort(0)), hostAndPort(1).toInt))
  })

  def registerTemplate(registerTpl: RegisterTemplateRequest): Future[PutIndexedScriptResponse] = {
    client.preparePutIndexedScript(scriptType, registerTpl.tplName, registerTpl.tplSource).asyncGet()
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
