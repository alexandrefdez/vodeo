package com.tfg.streaming.transcoding_service.service;

import com.tfg.streaming.transcoding_service.dto.VideoEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@Slf4j
public class VideoTranscodingWorker {

    @RabbitListener(queues = "video_transcoding_queue")
    public void processVideo(VideoEvent event) {
        log.info("[TranscodingWorker] Iniciando procesamiento HLS para el archivo: {}", event.getFilename());

        try {
            File inputFile = new File(event.getAbsolutePath());
            if (!inputFile.exists()) {
                log.error("[TranscodingWorker] Archivo de origen no encontrado en la ruta: {}",
                        event.getAbsolutePath());
                return;
            }

            // Extraer el nombre sin la extension de forma segura
            String fileNameWithoutExtension = "";
            int extensionIndex = event.getFilename().lastIndexOf(".");
            if (extensionIndex > 0) {
                fileNameWithoutExtension = event.getFilename().substring(0, extensionIndex);
            } else {
                fileNameWithoutExtension = event.getFilename();
            }

            // Crear el directorio de salida
            String outputDirPath = inputFile.getParent() + File.separator + fileNameWithoutExtension;
            File dir = new File(outputDirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String outputPlaylist = outputDirPath + File.separator + "master.m3u8";
            String outputSegments = outputDirPath + File.separator + "segment_%03d.ts";

            // Configuracion del subproceso FFmpeg
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "ffmpeg",
                    "-i", event.getAbsolutePath(),
                    "-threads", "2",
                    "-profile:v", "main",
                    "-vf", "scale=-2:720",
                    "-c:v", "libx264",
                    "-c:a", "aac",
                    "-hls_time", "10",
                    "-hls_playlist_type", "vod",
                    "-hls_segment_filename", outputSegments,
                    outputPlaylist);

            processBuilder.inheritIO();

            log.info("[TranscodingWorker] Ejecutando subproceso FFmpeg para: {}", fileNameWithoutExtension);
            Process process = processBuilder.start();

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                log.info("[TranscodingWorker] Transcodificacion completada con exito. Salida en: {}", outputDirPath);

                // Borrar el archivo original
                if (inputFile.delete()) {
                    log.info("[TranscodingWorker] Archivo de origen crudo eliminado correctamente.");
                }
            } else {
                log.error("[TranscodingWorker] El proceso FFmpeg finalizo con codigo de error: {}", exitCode);
            }

        } catch (Exception e) {
            log.error("[TranscodingWorker] Excepcion critica durante el procesamiento del video: ", e);
        }
    }
}