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
        byte[] buffer = new byte[1024];

        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)){

            for (File file: Objects.requireNonNull(sourceRootPath.toFile().listFiles())) {
                log.info("File Added : {}", file.getName());
                ZipEntry ze = new ZipEntry(file.getName());
                zos.putNextEntry(ze);
                try (FileInputStream in = new FileInputStream(file)) {
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
