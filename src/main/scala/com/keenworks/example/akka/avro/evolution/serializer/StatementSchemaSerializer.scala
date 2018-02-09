package com.keenworks.example.akka.avro.evolution.serializer

import java.io.ByteArrayOutputStream

import akka.serialization.Serializer
import com.keenworks.example.akka.avro.evolution.actor.Statement
import com.sksamuel.avro4s.{AvroInputStream, AvroOutputStream}
import com.typesafe.scalalogging.StrictLogging

class StatementSchemaSerializer extends Serializer with StrictLogging {
  override def identifier: Int = 1001

  override def includeManifest: Boolean = false

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case s: Statement =>
      logger.info("Serializing Statement!")
      val baos = new ByteArrayOutputStream()
      val output = AvroOutputStream.data[Statement](baos)
      output.write(s)
      output.close()
      val bytes = baos.toByteArray
      logger.info(s"Serialization length: {} bytes", bytes.length)
      bytes
  }

  override def fromBinary(bytes: Array[Byte], manifest: Option[Class[_]]): AnyRef = {
    logger.info(s"De-serializing Statement of length {}", bytes.length)
    val in = AvroInputStream.data[Statement](bytes)
    in.iterator.toList.head
  }
}
