package io.github.iss_2025_2026.service.tts;

import java.util.Arrays;

/**
 * Amplifica WAV PCM 16-bit applicando un limitatore morbido anti-clipping.
 */
final class WavAudioAmplifier {
    private static final int RIFF_HEADER_SIZE = 12;
    private static final int CHUNK_HEADER_SIZE = 8;
    private static final int PCM_FORMAT = 1;
    private static final int BITS_PER_SAMPLE_16 = 16;

    private WavAudioAmplifier() {
    }

    static byte[] amplify(byte[] audio, float requestedGain) {
        if (!isWave(audio) || requestedGain <= 1f) {
            return audio;
        }

        WaveData wave = findWaveData(audio);
        if (wave == null || wave.audioFormat != PCM_FORMAT || wave.bitsPerSample != BITS_PER_SAMPLE_16) {
            return audio;
        }

        if (findPeak(audio, wave.dataOffset, wave.dataLength) == 0) {
            return audio;
        }

        byte[] amplified = Arrays.copyOf(audio, audio.length);
        double limiterScale = Math.tanh(requestedGain);
        int dataEnd = wave.dataOffset + wave.dataLength;
        for (int offset = wave.dataOffset; offset + 1 < dataEnd; offset += 2) {
            int sample = readSigned16LittleEndian(audio, offset);
            double normalized = sample / 32768.0;
            double limited = Math.tanh(normalized * requestedGain) / limiterScale;
            int boosted = (int) Math.round(limited * Short.MAX_VALUE);
            writeSigned16LittleEndian(amplified, offset, clampToSigned16(boosted));
        }
        return amplified;
    }

    private static WaveData findWaveData(byte[] audio) {
        int audioFormat = -1;
        int bitsPerSample = -1;
        int dataOffset = -1;
        int dataLength = -1;
        int offset = RIFF_HEADER_SIZE;

        while (offset + CHUNK_HEADER_SIZE <= audio.length) {
            long chunkLength = readUnsigned32LittleEndian(audio, offset + 4);
            long chunkDataOffset = (long) offset + CHUNK_HEADER_SIZE;
            long chunkEnd = chunkDataOffset + chunkLength;
            if (chunkEnd > audio.length || chunkEnd < chunkDataOffset) {
                return null;
            }

            if (matches(audio, offset, "fmt ") && chunkLength >= 16) {
                audioFormat = readUnsigned16LittleEndian(audio, (int) chunkDataOffset);
                bitsPerSample = readUnsigned16LittleEndian(audio, (int) chunkDataOffset + 14);
            } else if (matches(audio, offset, "data")) {
                dataOffset = (int) chunkDataOffset;
                dataLength = (int) chunkLength;
            }

            long nextOffset = chunkEnd + (chunkLength & 1L);
            if (nextOffset > Integer.MAX_VALUE) {
                return null;
            }
            offset = (int) nextOffset;
        }

        if (audioFormat < 0 || bitsPerSample < 0 || dataOffset < 0 || dataLength < 2) {
            return null;
        }
        return new WaveData(audioFormat, bitsPerSample, dataOffset, dataLength);
    }

    private static int findPeak(byte[] audio, int offset, int length) {
        int peak = 0;
        int end = offset + length;
        for (int sampleOffset = offset; sampleOffset + 1 < end; sampleOffset += 2) {
            int sample = readSigned16LittleEndian(audio, sampleOffset);
            peak = Math.max(peak, Math.abs(sample));
        }
        return peak;
    }

    private static boolean isWave(byte[] audio) {
        return audio != null && audio.length >= RIFF_HEADER_SIZE
                && matches(audio, 0, "RIFF") && matches(audio, 8, "WAVE");
    }

    private static boolean matches(byte[] bytes, int offset, String value) {
        if (offset < 0 || offset + value.length() > bytes.length) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if ((byte) value.charAt(i) != bytes[offset + i]) {
                return false;
            }
        }
        return true;
    }

    private static int readUnsigned16LittleEndian(byte[] bytes, int offset) {
        return (bytes[offset] & 0xff) | ((bytes[offset + 1] & 0xff) << 8);
    }

    private static int readSigned16LittleEndian(byte[] bytes, int offset) {
        return (short) readUnsigned16LittleEndian(bytes, offset);
    }

    private static long readUnsigned32LittleEndian(byte[] bytes, int offset) {
        return (bytes[offset] & 0xffL)
                | ((bytes[offset + 1] & 0xffL) << 8)
                | ((bytes[offset + 2] & 0xffL) << 16)
                | ((bytes[offset + 3] & 0xffL) << 24);
    }

    private static void writeSigned16LittleEndian(byte[] bytes, int offset, int sample) {
        bytes[offset] = (byte) (sample & 0xff);
        bytes[offset + 1] = (byte) ((sample >>> 8) & 0xff);
    }

    private static int clampToSigned16(int sample) {
        return Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, sample));
    }

    private static final class WaveData {
        private final int audioFormat;
        private final int bitsPerSample;
        private final int dataOffset;
        private final int dataLength;

        private WaveData(int audioFormat, int bitsPerSample, int dataOffset, int dataLength) {
            this.audioFormat = audioFormat;
            this.bitsPerSample = bitsPerSample;
            this.dataOffset = dataOffset;
            this.dataLength = dataLength;
        }
    }
}
