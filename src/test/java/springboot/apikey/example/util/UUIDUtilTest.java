/**
 * Copyright 2019 Greg Whitaker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package springboot.apikey.example.util;

import org.apache.commons.codec.DecoderException;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class UUIDUtilTest {

    @Test
    public void toHexConvertsAllZeroBits() {
        assertEquals("00000000000000000000000000000000", UUIDUtil.toHex(new UUID(0L, 0L)));
    }

    @Test
    public void toHexConvertsHighBitSignedHalves() {
        UUID uuid = new UUID(Long.MIN_VALUE, Long.MIN_VALUE);

        assertEquals("80000000000000008000000000000000", UUIDUtil.toHex(uuid));
    }

    @Test
    public void fromHexConvertsMixedCaseInput() throws Exception {
        UUID uuid = UUIDUtil.fromHex("0123456789aBcDeFfEdCbA9876543210");

        assertEquals(UUID.fromString("01234567-89ab-cdef-fedc-ba9876543210"), uuid);
    }

    @Test(expected = DecoderException.class)
    public void fromHexRejectsOddLengthInput() throws Exception {
        UUIDUtil.fromHex("0123456789abcdef0123456789abcde");
    }

    @Test(expected = DecoderException.class)
    public void fromHexRejectsNonHexCharacter() throws Exception {
        UUIDUtil.fromHex("0123456789abcdef0123456789abcdeg");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void fromHexRejectsOverlongInput() throws Exception {
        UUIDUtil.fromHex("0123456789abcdef0123456789abcdef00");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void fromHexRejectsShortEvenLengthInput() throws Exception {
        UUIDUtil.fromHex("0123456789abcdef0123456789abcd");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void fromHexRejectsEmptyInput() throws Exception {
        UUIDUtil.fromHex("");
    }

    @Test
    public void toHexReturnsLowercase32CharacterOutput() {
        String hex = UUIDUtil.toHex(UUID.fromString("ABCDEF01-2345-6789-ABCD-EF0123456789"));

        assertEquals("abcdef0123456789abcdef0123456789", hex);
        assertEquals(32, hex.length());
    }

    @Test
    public void uuidHexRoundTripsAreDeterministic() throws Exception {
        UUID[] uuids = {
                new UUID(0L, 0L),
                new UUID(Long.MIN_VALUE, Long.MIN_VALUE),
                UUID.fromString("01234567-89ab-cdef-fedc-ba9876543210"),
                new UUID(Long.MAX_VALUE, Long.MAX_VALUE)
        };

        for (UUID uuid : uuids) {
            assertEquals(uuid, UUIDUtil.fromHex(UUIDUtil.toHex(uuid)));
        }
    }
}
