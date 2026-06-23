package io.github.iss_2025_2026.service.tts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Coordina sintesi REST e riproduzione libGDX senza bloccare il render thread.
 */
public class NpcSpeechService implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(NpcSpeechService.class.getName());

    private final TtsConfig config;
    private final TtsClient client;
    private final ExecutorService executor;
    private final AtomicLong generation = new AtomicLong();

    private volatile boolean closed;
    private Music currentMusic;
    private File currentAudioFile;

    public NpcSpeechService() {
        this(TtsConfig.fromGameProperties());
    }

    public NpcSpeechService(TtsConfig config) {
        this(config, TtsClientCreatorResolver.resolve(config.getProvider()).createClient(config));
    }

    public NpcSpeechService(TtsConfig config, TtsClient client) {
        this.config = config != null ? config : TtsConfig.fromGameProperties();
        this.client = client != null
                ? client
                : TtsClientCreatorResolver.resolve(this.config.getProvider()).createClient(this.config);
        this.executor = Executors.newSingleThreadExecutor(new DaemonThreadFactory());
    }

    public void speak(String text) {
        if (closed || !config.isEnabled() || text == null || text.trim().isEmpty()) {
            return;
        }

        final long requestGeneration = generation.incrementAndGet();
        executor.submit(() -> synthesizeAndSchedulePlayback(requestGeneration, text));
    }

    public boolean isAvailable() {
        return config.isEnabled() && client.isAvailable();
    }

    public void stop() {
        generation.incrementAndGet();
        if (Gdx.app != null) {
            Gdx.app.postRunnable(this::disposeCurrentPlayback);
        } else {
            disposeCurrentPlayback();
        }
    }

    @Override
    public void close() {
        closed = true;
        generation.incrementAndGet();
        executor.shutdownNow();
        disposeCurrentPlayback();
    }

    private void synthesizeAndSchedulePlayback(long requestGeneration, String text) {
        File audioFile = null;
        try {
            TtsResult result = client.synthesize(TtsRequest.of(text, config));
            if (closed || requestGeneration != generation.get()) {
                return;
            }

            audioFile = File.createTempFile("viddani-voxtral-", "." + config.getResponseFormat());
            Files.write(audioFile.toPath(), result.getAudioData());
            final File readyFile = audioFile;
            audioFile = null;

            if (Gdx.app == null) {
                deleteQuietly(readyFile);
                return;
            }
            Gdx.app.postRunnable(() -> playIfCurrent(requestGeneration, readyFile));
        } catch (TtsException exception) {
            LOGGER.log(Level.WARNING, "[NPC TTS] Sintesi non disponibile; mantengo il dialogo testuale.", exception);
        } catch (Exception exception) {
            LOGGER.log(Level.WARNING, "[NPC TTS] Errore durante la preparazione dell'audio.", exception);
        } finally {
            deleteQuietly(audioFile);
        }
    }

    private void playIfCurrent(long requestGeneration, File audioFile) {
        if (closed || requestGeneration != generation.get()) {
            deleteQuietly(audioFile);
            return;
        }

        disposeCurrentPlayback();
        try {
            Music music = Gdx.audio.newMusic(Gdx.files.absolute(audioFile.getAbsolutePath()));
            currentMusic = music;
            currentAudioFile = audioFile;
            music.setVolume(config.getVolume());
            music.setOnCompletionListener(completedMusic -> {
                if (currentMusic == completedMusic) {
                    currentMusic = null;
                    currentAudioFile = null;
                }
                completedMusic.dispose();
                deleteQuietly(audioFile);
            });
            music.play();
        } catch (RuntimeException exception) {
            LOGGER.log(Level.WARNING, "[NPC TTS] Riproduzione audio fallita.", exception);
            disposeCurrentPlayback();
            deleteQuietly(audioFile);
        }
    }

    private void disposeCurrentPlayback() {
        Music music = currentMusic;
        File audioFile = currentAudioFile;
        currentMusic = null;
        currentAudioFile = null;
        if (music != null) {
            try {
                music.stop();
                music.dispose();
            } catch (RuntimeException exception) {
                LOGGER.log(Level.FINE, "[NPC TTS] Errore ignorato durante dispose audio.", exception);
            }
        }
        deleteQuietly(audioFile);
    }

    private static void deleteQuietly(File file) {
        if (file != null && file.exists() && !file.delete()) {
            file.deleteOnExit();
        }
    }

    private static final class DaemonThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "npc-tts");
            thread.setDaemon(true);
            return thread;
        }
    }
}
