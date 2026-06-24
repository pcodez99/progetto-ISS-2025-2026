package io.github.iss_2025_2026.service.tts;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WavAudioAmplifierTest {
    private static final int PCM_DATA_OFFSET = 44;

    @Test
    public void amplifiesQuietPcm16SamplesByConfiguredGain() {
        byte[] source = pcmWave((short) 1000, (short) -2000, (short) 3000);

        byte[] amplified = WavAudioAmplifier.amplify(source, 3f);

        assertNotSame(source, amplified);
        assertTrue(sampleAt(amplified, 0) > 2900);
        assertTrue(sampleAt(amplified, 1) < -5700);
        assertTrue(sampleAt(amplified, 2) > 8400);
        assertEquals(1000, sampleAt(source, 0));
    }

    @Test
    public void softlyCompressesLoudSamplesWithoutClipping() {
        byte[] source = pcmWave((short) 20000, (short) -20000);

        byte[] amplified = WavAudioAmplifier.amplify(source, 8f);

        assertTrue(sampleAt(amplified, 0) > 20000);
        assertTrue(sampleAt(amplified, 0) <= Short.MAX_VALUE);
        assertTrue(Math.abs(sampleAt(amplified, 0) + sampleAt(amplified, 1)) <= 1);
    }

    @Test
    public void leavesUnsupportedAudioUntouched() {
        byte[] source = new byte[] {1, 2, 3, 4};

        byte[] result = WavAudioAmplifier.amplify(source, 3f);

        assertSame(source, result);
        assertArrayEquals(new byte[] {1, 2, 3, 4}, result);
    }

    private byte[] pcmWave(short... samples) {
        int dataLength = samples.length * 2;
        ByteBuffer buffer = ByteBuffer.allocate(PCM_DATA_OFFSET + dataLength).order(ByteOrder.LITTLE_ENDIAN);
        putAscii(buffer, "RIFF");
        buffer.putInt(buffer.capacity() - 8);
        putAscii(buffer, "WAVE");
        putAscii(buffer, "fmt ");
        buffer.putInt(16);
        buffer.putShort((short) 1);
        buffer.putShort((short) 1);
        buffer.putInt(16000);
        buffer.putInt(32000);
        buffer.putShort((short) 2);
        buffer.putShort((short) 16);
        putAscii(buffer, "data");
        buffer.putInt(dataLength);
        for (short sample : samples) {
            buffer.putShort(sample);
        }
        return buffer.array();
    }

    private int sampleAt(byte[] wave, int index) {
        return ByteBuffer.wrap(wave).order(ByteOrder.LITTLE_ENDIAN)
                .getShort(PCM_DATA_OFFSET + index * 2);
    }

    private void putAscii(ByteBuffer buffer, String value) {
        for (int i = 0; i < value.length(); i++) {
            buffer.put((byte) value.charAt(i));
        }
    }
}
