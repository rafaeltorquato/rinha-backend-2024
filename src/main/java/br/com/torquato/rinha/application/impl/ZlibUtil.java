package br.com.torquato.rinha.application.impl;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ZlibUtil {

    static byte[] decompressZLib(final byte[] data) {

        try (final var in = new InflaterInputStream(new ByteArrayInputStream(data), new Inflater());
             final var out = new ByteArrayOutputStream()) {
            in.transferTo(out);
            return out.toByteArray();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    static String decompressMysqlCompression(final byte[] input) {
        final byte[] data = removeBytes(input);
        return new String(decompressZLib(data), StandardCharsets.UTF_8);
    }

    static String removeLenghtBytes(byte[] input) {
        return new String(removeBytes(input), StandardCharsets.UTF_8);
    }

    //ignore first four bytes which denote length of uncompressed data. use rest of the array for decompression
    static byte[] removeBytes(byte[] input) {
        return Arrays.copyOfRange(input, 4, input.length);
    }
}
