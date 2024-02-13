package br.com.torquato.rinha;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class ZipUtil {
    //Decompress using ZLib
    public static byte[] decompressZLib(byte[] data) {
        Inflater inflater = new Inflater();
        inflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        try {
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            outputStream.close();
        } catch (IOException | DataFormatException ioe) {
            throw new RuntimeException(ioe);
        }
        return outputStream.toByteArray();
    }

    //MYSQL DECOMPRESS
    public static String decompressSQLCompression(byte[] input) {
        //ignore first four bytes which denote length of uncompressed data. use rest of the array for decompression
        byte[] data = Arrays.copyOfRange(input, 4, input.length);
        return new String(decompressZLib(data), StandardCharsets.UTF_8);
    }
}
