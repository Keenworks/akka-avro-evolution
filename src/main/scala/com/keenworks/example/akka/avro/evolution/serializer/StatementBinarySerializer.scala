package com.keenworks.example.akka.avro.evolution.serializer

import java.io.ByteArrayOutputStream

import akka.serialization.SerializerWithStringManifest
import com.keenworks.example.akka.avro.evolution.actor.Statement
import com.sksamuel.avro4s.{AvroInputStream, AvroOutputStream}
import com.typesafe.scalalogging.StrictLogging

class StatementBinarySerializer extends SerializerWithStringManifest with StrictLogging {
  override def identifier: Int = 2001
  val (activeSchemaFingerprint, schemaMap) = StatementSchemaMap()

  // Serializer always *writes* the most recent version of the schema
  override def manifest(o: AnyRef): String = activeSchemaFingerprint

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
    logger.info(s"De-serializing Statement of length {}", bytes.length)
    val in = AvroInputStream.builder[Statement](AvroInputStream.BinaryFormat)
      .from(bytes)
      // it was written using the schema in the message's identifier,
      // but we want to read it using the new schema
      .schema(schemaMap(manifest), schemaMap(activeSchemaFingerprint))
      .build()
    in.iterator.toList.head
  }

}
