package rever.search.domain

/**
 * Created by zkidkid on 10/11/16.
 */

case class SearchRequest(name:String,types:List[String],params:Map[String,AnyRef])
case class RegisterTemplateRequest(tplName: String, tplSource: String)
case class SuggestParam(name:String,text:String)
case class SuggestRequest(suggests: List[SuggestParam])