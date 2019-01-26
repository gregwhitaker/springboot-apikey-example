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

import org.apache.commons.codec.binary.Hex;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Utility class for working with UUIDs.
 */
public final class UUIDUtil {

    private UUIDUtil() {
        // Noop
    }

    /**
     * Creates a {@link UUID} from its hex representation.
     *
     * @param uuid hex representation of uuid
     * @return {@link UUID} converted from the hex string
     * @throws org.apache.commons.codec.DecoderException thrown if the hex string cannot be decoded
     * @throws IndexOutOfBoundsException thrown if the string is not the proper length for a uuid
     */
    public static UUID fromHex(String uuid) throws Exception {
        byte[] data = Hex.decodeHex(uuid.toCharArray());
        return new UUID(ByteBuffer.wrap(data, 0, 8).getLong(), ByteBuffer.wrap(data, 8, 8).getLong());
    }

    /**
     * Gets the hex representation of the supplied {@link UUID}
     *
     * @param uuid uuid to convert to hex
     * @return hex representation of the uuid
     */
    public static String toHex(UUID uuid) {
        ByteBuffer bytes = ByteBuffer.wrap(new byte[16]);
        bytes.putLong(uuid.getMostSignificantBits());
        bytes.putLong(uuid.getLeastSignificantBits());
        return new String(Hex.encodeHex(bytes.array()));
    }
}
