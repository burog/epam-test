package fadeevm.data.jpa.service;

import fadeevm.data.jpa.domain.Archive;
import fadeevm.data.jpa.repository.ArchiveRepository;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.extern.slf4j.Slf4j;
import org.mozilla.universalchardet.UniversalDetector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CvsArchiveFileProcessor implements ArchiveProcessor {

  public static final int buffer_size = 1024 * 10;
//  private final UniversalDetector detector = new UniversalDetector();

  @Autowired
  private ArchiveRepository archiveRepository;

  @Autowired
  private CvsHandlerService cvsHandlerService;

  public Long processArchive(String archivePath) {
    log.debug("start parsing file {}", archivePath);

    Path unzipp = null;
    try {
      unzipp = unzip(archivePath);
    } catch (IOException e) {
      log.error("some error occurred during unzip {}", e, archivePath);
      throw new RuntimeException(e);
    }

    Archive archive = new Archive();
    archive.setName(archivePath);
    archiveRepository.save(archive);

    List<CompletableFuture<Long>> files = new LinkedList<>();

    try {
      Path path = Files.walkFileTree(unzipp, new SimpleFileVisitor<Path>() {

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          CompletableFuture<Long> future = cvsHandlerService.handle(file, archive);
          files.add(future);
          log.debug("file {} visited", file.getFileName());

          return super.visitFile(file, attrs);
        }
      });
    } catch (IOException e) {
      log.error("some error occurred during walking files {}", e, unzipp);
      throw new RuntimeException(e);
    }

    CompletableFuture.allOf(files.toArray(new CompletableFuture[0])).join();
    return files.stream().map(longCompletableFuture -> {
      try {
        return longCompletableFuture.get();
      } catch (Exception e) {
        log.error("some error occurred during getting count of posts", e);
        throw new RuntimeException(e);
      }
    }).reduce((aLong, aLong2) -> aLong + aLong2).orElse(0L);
  }

  private Path unzip(String zipPath) throws IOException {
    long start = System.currentTimeMillis();

    Path target = Files.createTempDirectory("");

    byte[] buffer = new byte[buffer_size];
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

//        String encoding = detectEncoding(detector, zis);
//        if (isUtf8(encoding)) {
        copyToFile(target, buffer, zis, fileName);
//        } else {
//          transform(target, buffer, zis, fileName);
//        }

      }
      zis.closeEntry();
    }

    log.info("total unzip time {} ms", System.currentTimeMillis() - start);
    return target;
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
