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

    private static final UUID UUID_VALUE = UUID.fromString("01234567-89ab-cdef-0123-456789abcdef");

    @Test
    public void fromHexConvertsLowerAndUppercaseValues() throws Exception {
        assertEquals(UUID_VALUE, UUIDUtil.fromHex("0123456789abcdef0123456789abcdef"));
        assertEquals(UUID_VALUE, UUIDUtil.fromHex("0123456789ABCDEF0123456789ABCDEF"));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void fromHexRejectsTooShortValue() throws Exception {
        UUIDUtil.fromHex("0123456789abcdef0123456789abcd");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void fromHexRejectsOverlongValue() throws Exception {
        UUIDUtil.fromHex("0123456789abcdef0123456789abcdef00");
    }

    @Test(expected = DecoderException.class)
    public void fromHexRejectsOddLengthValue() throws Exception {
        UUIDUtil.fromHex("0123456789abcdef0123456789abcde");
    }

    @Test(expected = DecoderException.class)
    public void fromHexRejectsInvalidHexCharacters() throws Exception {
        UUIDUtil.fromHex("0123456789abcdef0123456789abcdeg");
    }
}
