package rever.search.util

import com.typesafe.config.ConfigRenderOptions
import org.scalatest.FunSuite

/**
 * Created by zkidkid on 10/12/16.
 */
class ZConfigTest extends FunSuite {

  println(ZConfig.config.getObject("elasticsearch.index.mappings").render(ConfigRenderOptions.concise()))
  println(ZConfig.config.getObject("elasticsearch.index.mappings").keySet())
}
