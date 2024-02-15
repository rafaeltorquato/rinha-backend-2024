package br.com.torquato.rinha.application.impl;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ZlibUtil {

    //ignore first four bytes which denote length of uncompressed data. use rest of the array for decompression
    static byte[] removeBytes(byte[] input) {
        return Arrays.copyOfRange(input, 4, input.length);
    }
}
