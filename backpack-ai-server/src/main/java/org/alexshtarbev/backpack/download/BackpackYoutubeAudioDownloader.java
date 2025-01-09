package org.alexshtarbev.backpack.download;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.alexshtarbev.backpack.conifg.BackpackApplicationConfigDownloadsRecord;
import org.alexshtarbev.backpack.conifg.BackpackApplicationConfigRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class BackpackYoutubeAudioDownloader {
    private static final Logger LOGGER = LoggerFactory.getLogger(BackpackYoutubeAudioDownloader.class);

    private final BackpackApplicationConfigDownloadsRecord downloadsConfig;

    public BackpackYoutubeAudioDownloader(BackpackApplicationConfigRecord configRecord) {
        this.downloadsConfig = configRecord.downloads();
    }

    public boolean downloadYoutubeVideoAsAudioMp3(String youtubeVideoPath, String fileName) {
        var builder = getYoutubeDowbloaderCommandProcessBuilder(youtubeVideoPath, fileName);
        try {
            var process = builder.start();
            process.waitFor(30, TimeUnit.MINUTES);
            String result = new String(process.getInputStream().readAllBytes());
            LOGGER.info(result);

            return getIsDownloadSuccessful(result);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private ProcessBuilder getYoutubeDowbloaderCommandProcessBuilder(String youtubeVideoPath, String fileName) {
        return new ProcessBuilder("yt-dlp",
                                  youtubeVideoPath,
                                  "--extract-audio",
                                  "--audio-format", "mp3",
                                  "--no-keep-video",
                                  "--write-info-json",
                                  "-o", Paths.get(downloadsConfig.downloadsDirectory(), fileName).toString(),
                                  "--cookies", downloadsConfig.cookiesFilePath())
                .redirectErrorStream(true);
    }

    private boolean getIsDownloadSuccessful(String result) {
        return result.contains("[download] 100% of ");
    }
}
