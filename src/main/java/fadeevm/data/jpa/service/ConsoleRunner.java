package fadeevm.data.jpa.service;

import fadeevm.data.jpa.repository.ArchiveRepository;
import fadeevm.data.jpa.repository.FileRepository;
import fadeevm.data.jpa.repository.PostRepository;
import java.util.Arrays;
import javax.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ConsoleRunner implements CommandLineRunner {

  @Qualifier("cvsArchiveFileProcessor")
//  @Qualifier("csvArchiveReaderProcessor")
  @Autowired
  ArchiveProcessor cvsArchiveHandler;

  @Autowired
  PostRepository postRepository;

  @Autowired
  FileRepository fileRepository;

  @Autowired
  ArchiveRepository archiveRepository;

  @Autowired
  ConfigurableApplicationContext context;

  @Autowired
  EntityManager entityManager;

  @Override
  public void run(String... args) {
    log.error("start app");
    log.debug("input: {}", Arrays.toString(args));
    if (args == null || args.length == 0) {
      log.error("nothing to do, please specify as first argument full path to zip archive");
      return;
    }

    if (args.length > 2 && "additional".equalsIgnoreCase(args[1])) {
      performAdditionalZip(args[2]);
    }

    String path = args[0];
    performLoading(path);
    performSelects(path);

    context.close();
  }

  public void performLoading(String archivePath) {
    long start = System.currentTimeMillis();

    Long postsCount = cvsArchiveHandler.processArchive(archivePath);

    long totalTime = System.currentTimeMillis() - start;
    log.error("total parsing time {} ms", totalTime);
    log.error("total countOfRecorderPosts {} ", postsCount);
  }

  /**
   * there is a possibility to tune the query
   */
  private void performSelects(String fullpath) {
    long start = System.currentTimeMillis();

    long totalPostsCount = postRepository.count();
    log.error("total totalPostsCount for whole table = {} ", totalPostsCount);

    Long postCountByArchive = postRepository.countPostsByArchivePath(fullpath);
    log.error("posts count per archive({}) = {} ", fullpath, postCountByArchive);

    Long postCountByArchiveAndHospital = postRepository
        .countPostsByArchivePathAndWord(fullpath, "hospital");
    log.error("posts count per Archive with word 'Hospital' = {} ", postCountByArchiveAndHospital);

    Long postCountByArchiveAndHealth = postRepository
        .countPostsByArchivePathAndWord(fullpath, "health");
    log.error("posts count per Archive with word 'Health' = {} ", postCountByArchiveAndHealth);
    long totalTime = System.currentTimeMillis() - start;
    log.error("total selecting time {} ms", totalTime);
  }

  public String performAdditionalZip(String path) {
    Long postsCount = cvsArchiveHandler.processArchive(path);

    log.error("additional countOfRecorderPosts {} ", postsCount);

    Long countPostsByWord = postRepository.countPostsByWord("health", "nytimeshealth.txt");
    log.error("countPostsByWord init{} ", countPostsByWord);

    Long postCountByArchiveAdditional = postRepository.countPostsByArchivePath(path);
    log.error("postCountByArchive additional {} ", postCountByArchiveAdditional);
    return path;
  }
}
