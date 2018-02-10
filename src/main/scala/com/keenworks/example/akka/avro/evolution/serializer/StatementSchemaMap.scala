package com.keenworks.example.akka.avro.evolution.serializer

import java.net.URL

import com.typesafe.scalalogging.StrictLogging
import org.apache.avro.Schema
import org.apache.commons.codec.digest.DigestUtils

import scala.io.Source

object StatementSchemaMap extends StrictLogging {
  final val activeSchema: URL = getClass.getResource("/avro/StatementV2.avsc")
  final val schemaHistory = List(
    "/avroHistory/StatementV1.avsc"
  )

  final val activeSchemaFingerprint: String = getMD5FromUrl(activeSchema)

  final val schemaMap: Map[String, Schema] = Map(
    activeSchemaFingerprint -> getSchemaFromUrl(activeSchema)
  ) ++ schemaHistory.map(schemaVersion => {
    val oldSchema = getClass.getResource(schemaVersion)
    (getMD5FromUrl(oldSchema), getSchemaFromUrl(oldSchema))
  })

  def getMD5FromUrl(url: URL): String = DigestUtils.md5Hex(url.openStream())
  def getSchemaFromUrl(url: URL): Schema =
    new Schema.Parser().parse(Source.fromInputStream(url.openStream()).getLines().mkString)

  // return the fingerprint for the current schema, and the map for all schemas
  def apply(): (String, Map[String, Schema]) = (activeSchemaFingerprint, schemaMap)
}
