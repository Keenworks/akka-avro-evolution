import com.sksamuel.avro4s.AvroSchema

//auto generated code by avro4s
case class TruthMatrix(
                        accuracy: Double,
                        popularity: Double
                      )

//auto generated code by avro4s
case class Statement(
                      statement: String,
                      truthMatrix: TruthMatrix,
                      truth: Option[Boolean] = Some(false)
                    )

AvroSchema[Statement].toString(true)
