package housingfinder.algebras

import housingfinder.domain.auth._

trait Crypto {
  def encrypt(value: Password): EncryptedPassword
  def decrypt(value: EncryptedPassword): Password
}

final class LiveCrypto private (
    eCipher: EncryptCipher,
    dCipher: DecryptCipher
) extends Crypto {
  // Workaround for PostgreSQL ERROR: invalid byte sequence for encoding "UTF8": 0x00
  private val Key = "=DownInAHole="

  override def encrypt(password: Password): EncryptedPassword = {
    val bytes = password.value.getBytes("UTF-8")
    val result = new String(eCipher.value.doFinal(bytes), "UTF-8")
    val removeNull = result.replaceAll("\\u0000", Key)
    EncryptedPassword(removeNull)
  }

  override def decrypt(password: EncryptedPassword): Password = {
    val bytes = password.value.getBytes("UTF-8")
    val result = new String(dCipher.value.doFinal(bytes), "UTF-8")
    val insertNull = result.replaceAll(Key, "\\u0000")
    Password(insertNull)
  }
}
