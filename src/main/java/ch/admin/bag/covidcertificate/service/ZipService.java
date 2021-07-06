package ch.admin.bag.covidcertificate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class ZipService {

    public void zipIt(Path sourceRootPath, File zipFile) throws IOException {
        log.info("Creating Zip file {}", zipFile.getName());
        var buffer = new byte[1024];

        try (var fos = new FileOutputStream(zipFile);
             var zos = new ZipOutputStream(fos)){

            for (File file: Objects.requireNonNull(sourceRootPath.toFile().listFiles())) {
                log.debug("File Added : {}", file.getName());
                var ze = new ZipEntry(file.getName());
                zos.putNextEntry(ze);
                try (var in = new FileInputStream(file)) {
                    int len;
                    while ((len = in .read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                }
            }

            zos.closeEntry();
            log.info("Folder successfully compressed");

        } catch (IOException e) {
            log.error("Failed to compress folder {}", zipFile.getName(), e);
            throw e;
        }
    }
}
