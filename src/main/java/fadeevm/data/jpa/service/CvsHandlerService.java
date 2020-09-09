package fadeevm.data.jpa.service;

import fadeevm.data.jpa.parsers.CsvParser;
import fadeevm.data.jpa.domain.Archive;
import fadeevm.data.jpa.domain.Post;
import fadeevm.data.jpa.domain.TweetsFile;
import fadeevm.data.jpa.repository.ArchiveRepository;
import fadeevm.data.jpa.repository.FileRepository;
import fadeevm.data.jpa.repository.PostRepository;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@Slf4j
public class CvsHandlerService {


  @Value("${flushEach}")
  private int flushEach;
  @Autowired
  ArchiveRepository archiveRepository;
  @Autowired
  FileRepository fileRepository;
  @Autowired
  PostRepository postRepository;

  @Qualifier("csvApacheParser")
  @Autowired
  CsvParser jacksonCsvParser;

  @Autowired
  TransactionTemplate transactionTemplate;


  @Async("threadPoolCsvTaskExecutor")
  public CompletableFuture<Long> handle(Path cvsPath, Archive archive) {
    log.debug("handle start path = {}", cvsPath);

    TweetsFile tweetsFile = saveTweetsFile(cvsPath.getFileName().toString(), archive);

    List<Post> posts = parsePosts(cvsPath, tweetsFile);

    savingPosts(posts);
    log.trace("handle for file {} ends {}", cvsPath.getFileName(), posts);
    return CompletableFuture.completedFuture((long) posts.size());
  }

  @Async("threadPoolCsvTaskExecutor")
  public CompletableFuture<Long> handle(String cvsPath, Reader reader, Archive archive) {
    log.debug("handle start path = {}", cvsPath);

    TweetsFile tweetsFile = saveTweetsFile(cvsPath, archive);

    List<Post> posts = parsePosts(reader, tweetsFile);

    savingPosts(posts);
    log.trace("handle for file {} ends {}", cvsPath, posts);
    return CompletableFuture.completedFuture((long) posts.size());
  }

  private void savingPosts(List<Post> posts) {

    long start = System.currentTimeMillis();

    transactionTemplate.execute(status -> {
      for (int i = 0; i < posts.size(); i++) {
        Post entity = posts.get(i);
        postRepository.save(entity);

        if (i % flushEach == 0) {
          log.trace("flushing");

          postRepository.flush();
        }
      }

      return true;
    });

    log.info("savingPosts time {} ms", System.currentTimeMillis() - start);
  }

  private List<Post> parsePosts(Path cvsPath, TweetsFile tweetsFile) {

    long start = System.currentTimeMillis();

    InputStreamReader inputStreamReader = null;
    String charsetName = "UTF-8";
//    String charsetName = "ansi-1251";
    try {
      inputStreamReader = new InputStreamReader(
          new FileInputStream(cvsPath.toFile()), charsetName);
    } catch (UnsupportedEncodingException | FileNotFoundException e) {
      log.error("cant parse file {} with enc ", cvsPath, charsetName, e);
    }
    List<Post> posts = jacksonCsvParser.readCsv(inputStreamReader, tweetsFile);
    log.info("parsePosts time {} ms", System.currentTimeMillis() - start);
    return posts;
  }

  private List<Post> parsePosts(Reader reader, TweetsFile tweetsFile) {
    long start = System.currentTimeMillis();
    List<Post> posts = jacksonCsvParser.readCsv(reader, tweetsFile);
    log.info("parsePosts time {} ms", System.currentTimeMillis() - start);
    return posts;
  }

  private TweetsFile saveTweetsFile(String fileName, Archive archive) {
    long start = System.currentTimeMillis();

    TweetsFile tweetsFile = new TweetsFile();
    tweetsFile.setArchive(archive);
    tweetsFile.setName(fileName);
    tweetsFile = fileRepository.save(tweetsFile);
    log.info("saveTweetsFile time {} ms {}", System.currentTimeMillis() - start, tweetsFile);
    return tweetsFile;
  }

}
