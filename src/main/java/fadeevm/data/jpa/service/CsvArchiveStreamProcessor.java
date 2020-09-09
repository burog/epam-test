package fadeevm.data.jpa.service;

import fadeevm.data.jpa.domain.Archive;
import fadeevm.data.jpa.repository.ArchiveRepository;
import fadeevm.data.jpa.service.ArchiveProcessor;
import fadeevm.data.jpa.service.CvsHandlerService;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.extern.slf4j.Slf4j;
import org.mozilla.universalchardet.UniversalDetector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CsvArchiveStreamProcessor implements ArchiveProcessor {

  public static final int buffer_size = 1024 * 10;
  public static final Random RANDOM = new Random();
  private final UniversalDetector detector = new UniversalDetector();


  @Autowired
  private ArchiveRepository archiveRepository;

  @Autowired
  private CvsHandlerService cvsHandlerService;

  public Long processArchive(String archivePath) {
    log.debug("start parsing file {}", archivePath);

    List<CompletableFuture<Long>> files = unzip(archivePath);

    CompletableFuture.allOf(files.toArray(new CompletableFuture[0])).join();
    return files.stream().map(longCompletableFuture -> {
      try {
        return longCompletableFuture.get();
      } catch (InterruptedException | ExecutionException e) {
        log.error("some error occurred during getting count of posts", e);
        throw new RuntimeException(e);
      }
    }).reduce((aLong, aLong2) -> aLong + aLong2).orElse(0L);
  }

  private List<CompletableFuture<Long>> unzip(String zipPath) {
    long start = System.currentTimeMillis();
    List<CompletableFuture<Long>> futures = new ArrayList<>();

    Archive archive = new Archive();
    archive.setName(zipPath);
    archiveRepository.save(archive);

    try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath))) {
      ZipEntry zipEntry;

      while ((zipEntry = zis.getNextEntry()) != null) {
        if (zipEntry.isDirectory()) {
          continue;
        }

        String fileName = zipEntry.getName().substring(zipEntry.getName().lastIndexOf("/") + 1);

        if (fileName.startsWith("._")) {
          continue;
        }

        InputStreamReader inputStreamReader = new InputStreamReader(zis);
        futures.add(cvsHandlerService.handle(fileName, inputStreamReader, archive));

      }
      zis.closeEntry();
    } catch (IOException e) {
      log.error("error during unzip", e);
    }

    log.info("total unzip time {} ms", System.currentTimeMillis() - start);
    return futures;
  }

  private boolean isUtf8(String encoding) throws IOException {

    return "UTF-8".equals(encoding);
  }

  private void copyToFile(Path target, byte[] buffer, ZipInputStream zis, String fileName)
      throws IOException {
    File newFile = new File(target.toFile(), fileName);
    try (FileOutputStream fos = new FileOutputStream(newFile)) {
      int len;
      while ((len = zis.read(buffer)) > 0) {
        fos.write(buffer, 0, len);
      }
    }
    log.trace("successfully copied file {}", fileName);
  }

  String detectEncoding(UniversalDetector detector, ZipInputStream zis) throws IOException {
    byte[] buffer = new byte[buffer_size];
    int len;
    while ((len = zis.read(buffer)) > 0) {
      detector.handleData(buffer, 0, len);
    }

    detector.dataEnd();
    String detectedCharset = detector.getDetectedCharset();
    if (detectedCharset == null) {

      log.error("No encoding detected");
      throw new RuntimeException("No encoding detected");
    }
    log.trace("Detected encoding = {} ", detectedCharset);
    detector.reset();
    return detectedCharset;
  }
}
