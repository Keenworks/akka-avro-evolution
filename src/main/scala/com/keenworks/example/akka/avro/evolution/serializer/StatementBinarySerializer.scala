package com.keenworks.example.akka.avro.evolution.serializer

import java.io.{ByteArrayOutputStream, InputStream}

import akka.serialization.SerializerWithStringManifest
import com.keenworks.example.akka.avro.evolution.actor.Statement
import com.sksamuel.avro4s.{AvroInputStream, AvroOutputStream}
import com.typesafe.scalalogging.StrictLogging
import org.apache.commons.codec.digest.DigestUtils

class StatementBinarySerializer extends SerializerWithStringManifest with StrictLogging {
  override def identifier: Int = 2001

  val stream: InputStream = getClass.getResourceAsStream("/avro/StatementV1.avsc")
  final val SchemaFingerprint: String = DigestUtils.md5Hex(stream)
  override def manifest(o: AnyRef): String = SchemaFingerprint

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case s: Statement =>
      val baos = new ByteArrayOutputStream()
      val output = AvroOutputStream.binary[Statement](baos)
      output.write(s)
      output.close()
      val bytes = baos.toByteArray
      logger.info(s"Serialization length: {} bytes", bytes.length)
      bytes
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = {
    manifest match {
      case SchemaFingerprint =>
        logger.info(s"De-serializing Statement of length {}", bytes.length)
        val in = AvroInputStream.binary[Statement](bytes)
        in.iterator.toList.head
    }
  }

}
