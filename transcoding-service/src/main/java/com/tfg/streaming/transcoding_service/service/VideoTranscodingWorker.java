package com.tfg.streaming.transcoding_service.service;

import com.tfg.streaming.transcoding_service.dto.VideoEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;

@Service
@Slf4j
public class VideoTranscodingWorker {

    @RabbitListener(queues = "video_transcoding_queue")
    public void processVideo(VideoEvent event) {
        log.info("[TranscodingWorker] Iniciando procesamiento ABS/HLS para el archivo: {}", event.getFilename());

        try {
            File inputFile = new File(event.getAbsolutePath());
            if (!inputFile.exists()) {
                log.error("[TranscodingWorker] Archivo de origen no encontrado en la ruta: {}",
                        event.getAbsolutePath());
                return;
            }

            // Extraer el nombre sin la extension de forma segura
            String filename = event.getFilename();
            int dotIndex = filename.lastIndexOf(".");
            String folderName = dotIndex > 0 ? filename.substring(0, dotIndex) : filename;

            // Crear el directorio de salida
            String outputDirPath = inputFile.getParent() + File.separator + folderName;
            File outputDir = new File(outputDirPath);
            if (!outputDir.exists())
                outputDir.mkdirs();

            String segmentPattern = "%v_seg%03d.ts";
            String playlistPattern = "%v.m3u8";

            List<String> command = buildAbsCommand(
                    event.getAbsolutePath(), segmentPattern, playlistPattern);

            log.info("[TranscodingWorker] Ejecutando subproceso FFmpeg (Pipeline ABS) para: {}", folderName);

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            pb.directory(outputDir);

            Process process = pb.start();

            // Consumir el output de FFmpeg en el hilo actual para no bloquear el buffer del
            // SO
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                reader.lines().forEach(line -> log.info("[FFmpeg] {}", line)); // info para verlo en consola
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                log.info("[TranscodingWorker] Transcodificacion ABS completada con exito. Salida en: {}",
                        outputDirPath);

                // Borrar el archivo original
                if (inputFile.delete()) {
                    log.info("[TranscodingWorker] Archivo de origen crudo eliminado correctamente: {}", filename);
                }
            } else {
                log.error("[TranscodingWorker] El proceso FFmpeg finalizo con codigo de error: {}", exitCode);
            }

        } catch (Exception e) {
            log.error("[TranscodingWorker] Excepcion critica durante el procesamiento del video: ", e);
        }
    }

    /**
     * Construir el comando FFmpeg en una unica ejecucion para generar
     * tres variantes de video en diferentes resoluciones:
     * 1080p, 720p y 480p
     */
    private List<String> buildAbsCommand(String input, String segmentPattern, String playlistPattern) {
        return List.of(
                "ffmpeg", "-i", input,

                // UN decode -> TRES pipelines de escalado
                "-filter_complex",
                "[0:v]split=3[v1][v2][v3];" +
                        "[v1]scale=1920:1080[v1out];" +
                        "[v2]scale=1280:720[v2out];" +
                        "[v3]scale=854:480[v3out]",

                // Stream 0: 1080p
                "-map", "[v1out]", "-map", "0:a",
                "-c:v:0", "libx264", "-preset", "fast", "-profile:v:0", "main",
                "-b:v:0", "5000k", "-maxrate:v:0", "5350k", "-bufsize:v:0", "7500k",

                // Stream 1: 720p
                "-map", "[v2out]", "-map", "0:a",
                "-c:v:1", "libx264", "-preset", "fast", "-profile:v:1", "main",
                "-b:v:1", "2800k", "-maxrate:v:1", "2996k", "-bufsize:v:1", "4200k",

                // Stream 2: 480p
                "-map", "[v3out]", "-map", "0:a",
                "-c:v:2", "libx264", "-preset", "fast", "-profile:v:2", "main",
                "-b:v:2", "1400k", "-maxrate:v:2", "1498k", "-bufsize:v:2", "2100k",

                // Audio: AAC estereo compartido
                "-c:a", "aac", "-b:a", "128k", "-ac", "2",

                // HLS output
                "-var_stream_map", "v:0,a:0 v:1,a:1 v:2,a:2",
                "-master_pl_name", "master.m3u8",
                "-hls_time", "10",
                "-hls_playlist_type", "vod",
                "-hls_segment_filename", segmentPattern,
                playlistPattern);
    }
}