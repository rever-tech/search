package rever.search.domain

/**
 * Created by zkidkid on 10/11/16.
 */

case class SearchRequest(name:String,types:Array[String],params:Map[String,AnyRef])
case class RegisterTemplateRequest(tplName: String, tplSource: String)

