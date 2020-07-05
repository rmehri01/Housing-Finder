package housingfinder.algebras

import housingfinder.domain.auth.{EncryptedPassword, Password}

trait Crypto {
  def encrypt(value: Password): EncryptedPassword
  def decrypt(value: EncryptedPassword): Password
}
