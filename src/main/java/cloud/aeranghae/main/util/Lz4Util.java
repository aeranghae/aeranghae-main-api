package cloud.aeranghae.main.util;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4SafeDecompressor;
import java.nio.charset.StandardCharsets;

public class Lz4Util {
    private static final LZ4Factory factory = LZ4Factory.fastestInstance();

    /**
     * 🤐 텍스트를 LZ4로 압축 (바이트 배열 반환)
     */
    public static byte[] compress(String data) {
        if (data == null) return null;

        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        final int decompressedLength = dataBytes.length;

        LZ4Compressor compressor = factory.fastCompressor();
        int maxCompressedLength = compressor.maxCompressedLength(decompressedLength);
        byte[] compressed = new byte[maxCompressedLength];

        int compressedLength = compressor.compress(dataBytes, 0, decompressedLength, compressed, 0, maxCompressedLength);

        // 복원을 위해 맨 앞 4바이트에 원본 길이를 저장합니다.
        byte[] result = new byte[compressedLength + 4];
        result[0] = (byte) (decompressedLength >>> 24);
        result[1] = (byte) (decompressedLength >>> 16);
        result[2] = (byte) (decompressedLength >>> 8);
        result[3] = (byte) decompressedLength;

        System.arraycopy(compressed, 0, result, 4, compressedLength);
        return result;
    }

    /**
     * 🔓 압축된 바이트 배열을 다시 텍스트로 복원
     */
    public static String decompress(byte[] compressedWithLength) {
        if (compressedWithLength == null) return null;

        // 맨 앞 4바이트에서 원본 길이를 다시 읽어옵니다.
        int decompressedLength = (compressedWithLength[0] & 0xFF) << 24 |
                (compressedWithLength[1] & 0xFF) << 16 |
                (compressedWithLength[2] & 0xFF) << 8 |
                (compressedWithLength[3] & 0xFF);

        LZ4SafeDecompressor decompressor = factory.safeDecompressor();
        byte[] restored = new byte[decompressedLength];
        decompressor.decompress(compressedWithLength, 4, compressedWithLength.length - 4, restored, 0);

        return new String(restored, StandardCharsets.UTF_8);
    }
}