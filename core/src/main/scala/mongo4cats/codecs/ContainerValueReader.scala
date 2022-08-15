/*
 * Copyright 2020 Kirill5k
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mongo4cats.codecs

import mongo4cats.bson.Document
import org.bson.{BsonReader, BsonType, BsonWriter, Transformer, UuidRepresentation}
import org.bson.codecs.{BsonTypeCodecMap, DecoderContext, Encoder, EncoderContext}

import java.time.Instant
import java.util.UUID

private[codecs] object ContainerValueReader {

  private[codecs] def write(
      writer: BsonWriter,
      context: EncoderContext,
      maybeValue: Option[Any],
      registry: CodecRegistry
  ): Unit =
    maybeValue match {
      case Some(value) => context.encodeWithChildContext(registry.get(value.getClass).asInstanceOf[Encoder[Any]], writer, value)
      case None        => writer.writeNull()
    }

  private[codecs] def read(
      reader: BsonReader,
      context: DecoderContext,
      bsonTypeCodecMap: BsonTypeCodecMap,
      uuidRepresentation: UuidRepresentation,
      registry: CodecRegistry,
      valueTransformer: Transformer
  ): AnyRef =
    reader.getCurrentBsonType match {
      case BsonType.ARRAY     => valueTransformer.transform(registry.get(classOf[Iterable[Any]]).decode(reader, context))
      case BsonType.DOCUMENT  => valueTransformer.transform(registry.get(classOf[Document]).decode(reader, context))
      case BsonType.DATE_TIME => valueTransformer.transform(registry.get(classOf[Instant]).decode(reader, context))
      case BsonType.BINARY if isUuid(reader, uuidRepresentation) =>
        valueTransformer.transform(registry.get(classOf[UUID]).decode(reader, context))
      case BsonType.NULL =>
        reader.readNull()
        null
      case BsonType.UNDEFINED =>
        reader.readUndefined()
        null
      case bsonType => valueTransformer.transform(bsonTypeCodecMap.get(bsonType).decode(reader, context))
    }

  private def isUuid(reader: BsonReader, uuidRepresentation: UuidRepresentation): Boolean =
    isLegacyUuid(reader, uuidRepresentation) || isStandardUuid(reader, uuidRepresentation)

  private def isLegacyUuid(reader: BsonReader, uuidRepresentation: UuidRepresentation): Boolean =
    reader.peekBinarySubType == 3 &&
      reader.peekBinarySize() == 16 &&
      (uuidRepresentation == UuidRepresentation.JAVA_LEGACY ||
        uuidRepresentation == UuidRepresentation.C_SHARP_LEGACY ||
        uuidRepresentation == UuidRepresentation.PYTHON_LEGACY)

  private def isStandardUuid(reader: BsonReader, uuidRepresentation: UuidRepresentation): Boolean =
    reader.peekBinarySubType == 4 &&
      reader.peekBinarySize() == 16 &&
      uuidRepresentation == UuidRepresentation.STANDARD
}