package rever.search.module

import com.twitter.finagle.http.{Request, Status, Response => TResponse}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future
import rever.search.util.ZConfig

/**
 * Created by zkidkid on 10/11/16.
 */
class AuthenticationFilter extends SimpleFilter[Request, TResponse] {
  private val TOKEN = "token"
  private val TOKEN_VALUE = ZConfig.getString("security.admin.secretKey", "b07c7deb733fd52df20fc26cda23e1a0")

  override def apply(request: Request, service: Service[Request, TResponse]): Future[TResponse] = {
    request.uri match {
      case "/template" | "/refresh" | "/index" => {
        request.headerMap.get(TOKEN) match {
          case Some(TOKEN_VALUE) => service.apply(request)
          case _ => Future {
            TResponse.apply(Status.Unauthorized)
          }
        }
      }
      case _ => service.apply(request)
    }
  }
}
