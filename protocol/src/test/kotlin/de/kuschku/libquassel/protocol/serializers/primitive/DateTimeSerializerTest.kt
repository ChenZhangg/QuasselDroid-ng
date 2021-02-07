/*
 * Quasseldroid - Quassel client for Android
 *
 * Copyright (c) 2020 Janne Mareike Koschinski
 * Copyright (c) 2020 The Quassel Project
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3 as published
 * by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.kuschku.libquassel.protocol.serializers.primitive

import de.kuschku.libquassel.protocol.testutil.byteBufferOf
import de.kuschku.libquassel.protocol.testutil.matchers.TemporalMatcher
import de.kuschku.libquassel.protocol.testutil.qtSerializerTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.threeten.bp.*
import org.threeten.bp.chrono.JapaneseDate

class DateTimeSerializerTest {
  private val serializer = DateTimeSerializer

  @Test
  fun testEpoch() = qtSerializerTest(
    DateTimeSerializer,
    Instant.EPOCH,
    byteBufferOf(0, 37, 61, -116, 0, 0, 0, 0, 2),
    matcher = ::TemporalMatcher
  )

  @Test
  fun testEpochAtTimezone() = qtSerializerTest(
    DateTimeSerializer,
    Instant.EPOCH.atOffset(ZoneOffset.ofTotalSeconds(1234)),
    byteBufferOf(0x00u, 0x25u, 0x3Du, 0x8Cu, 0x00u, 0x12u, 0xD4u, 0x50u, 0x03u, 0x00u, 0x00u, 0x04u, 0xD2u),
    matcher = ::TemporalMatcher
  )

  @Test
  fun testEpochByCalendarAtTimezone() = qtSerializerTest(
    DateTimeSerializer,
    LocalDateTime
      .of(1970, 1, 1, 0, 0)
      .atZone(ZoneId.of("Europe/Berlin"))
      .toInstant(),
    byteBufferOf(0, 37, 61, -117, 4, -17, 109, -128, 2),
    matcher = ::TemporalMatcher
  )

  @Test
  fun testNormalCase() = qtSerializerTest(
    DateTimeSerializer,
    LocalDateTime
      .of(2019, Month.JANUARY, 15, 20, 25)
      .atZone(ZoneId.of("Europe/Berlin"))
      .toInstant(),
    byteBufferOf(0, 37, -125, -125, 4, 42, -106, -32, 2),
    matcher = ::TemporalMatcher
  )

  @Test
  fun testLocalDateTime() = qtSerializerTest(
    DateTimeSerializer,
    LocalDateTime
      .of(2019, Month.JANUARY, 15, 20, 25),
    byteBufferOf(0x00u, 0x25u, 0x83u, 0x83u, 0x04u, 0x61u, 0x85u, 0x60u, 0xFFu),
    matcher = ::TemporalMatcher
  )

  @Test
  fun testZonedDateTime() = qtSerializerTest(
    DateTimeSerializer,
    LocalDateTime
      .of(2019, Month.JANUARY, 15, 20, 25)
      .atZone(ZoneId.systemDefault()),
    matcher = ::TemporalMatcher
  )

  @Test
  fun testUnknownDateTime() = qtSerializerTest(
    DateTimeSerializer,
    LocalDateTime
      .of(2019, Month.JANUARY, 15, 20, 25),
    byteBufferOf(0x00u, 0x25u, 0x83u, 0x83u, 0x04u, 0x61u, 0x85u, 0x60u, 0xFFu),
    matcher = ::TemporalMatcher
  )

  @Test
  fun testOldJavaDate() {
    assertThrows<IllegalArgumentException>("Unsupported Format: org.threeten.bp.chrono.JapaneseDate") {
      qtSerializerTest(
        DateTimeSerializer,
        JapaneseDate.now(),
        matcher = ::TemporalMatcher
      )
    }
  }
}
