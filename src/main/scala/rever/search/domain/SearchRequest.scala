package rever.search.domain

/**
 * Created by zkidkid on 10/11/16.
 */

case class SearchRequest(tplName:String,tplParams:Map[String,AnyRef],types:Array[String])
case class RegisterTemplateRequest(tplName: String, tplSource: String)

