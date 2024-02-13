package br.com.torquato.rinha.application.impl;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ZlibUtil {

    static byte[] decompressZLib(final byte[] data) {
        final Inflater inflater = new Inflater();
        inflater.setInput(data);

        try (final var out = new ByteArrayOutputStream(data.length)) {
            final byte[] buffer = new byte[1024];
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                out.write(buffer, 0, count);
            }
            return out.toByteArray();
        } catch (IOException | DataFormatException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    static String decompressMysqlCompression(final byte[] input) {
        //ignore first four bytes which denote length of uncompressed data. use rest of the array for decompression
        final byte[] data = Arrays.copyOfRange(input, 4, input.length);
        return new String(decompressZLib(data), StandardCharsets.UTF_8);
    }
}
