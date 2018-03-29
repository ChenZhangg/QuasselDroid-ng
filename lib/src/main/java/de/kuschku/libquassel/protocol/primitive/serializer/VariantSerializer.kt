package de.kuschku.libquassel.protocol.primitive.serializer

import de.kuschku.libquassel.protocol.*
import de.kuschku.libquassel.quassel.QuasselFeatures
import de.kuschku.libquassel.util.nio.ChainedByteBuffer
import java.nio.ByteBuffer

object VariantSerializer : Serializer<QVariant_> {
  override fun serialize(buffer: ChainedByteBuffer, data: QVariant_, features: QuasselFeatures) {
    IntSerializer.serialize(buffer, data.type.type.id, features)
    BoolSerializer.serialize(buffer, false, features)
    if (data.type.type == Type.UserType) {
      StringSerializer.C.serialize(buffer, data.type.name, features)
    }
    if (data.type.serializer == null) {
      throw IllegalArgumentException("Unknown type: ${data.type.name}")
    }
    data.type.serializer.serialize(buffer, data.data, features)
  }

  override fun deserialize(buffer: ByteBuffer, features: QuasselFeatures): QVariant_ {
    val rawType = IntSerializer.deserialize(buffer, features)
    val type = Type.of(rawType)

    val isNull = BoolSerializer.deserialize(buffer, features)

    val metaType: MetaType<All_> = if (type == Type.UserType) {
      val deserialize = StringSerializer.C.deserialize(buffer, features)
      MetaType.get(deserialize)
    } else {
      MetaType.get(type)
    }
    if (metaType.serializer == null) {
      throw IllegalArgumentException("Unknown type: ${metaType.name}")
    }

    val result = metaType.serializer.deserialize(buffer, features)
    return QVariant(result, metaType)
  }
}
