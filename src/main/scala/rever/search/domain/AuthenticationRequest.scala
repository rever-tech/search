package rever.search.domain

import rever.search.domain.AuthenticationRequest.SecretKey

/**
 * Created by zkidkid on 10/11/16.
 */
object AuthenticationRequest {
  type Token = String
  type SecretKey = String

}

abstract class AdminRequest(key: SecretKey)

case class GetReadTokenRequest(secretKey: SecretKey) extends AdminRequest(secretKey)

case class GetWriteTokenRequest(secretKey: SecretKey) extends AdminRequest(secretKey)

case class GetDeleteTokenRequest(secretKey: SecretKey) extends AdminRequest(secretKey)
