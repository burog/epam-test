package fadeevm.data.jpa.repository;

import fadeevm.data.jpa.domain.Post;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Integer> {

  @Query("SELECT COUNT(p) " +
      "FROM posts p " +
      "JOIN p.tweetsFile t " +
      "JOIN t.archive a " +
      "WHERE a.name = :archivePath")
  Long countPostsByArchivePath(@Param("archivePath") String archivePath);

  @Query("SELECT COUNT(p) " +
      "FROM posts p " +
      "JOIN p.tweetsFile t " +
      "JOIN t.archive a " +
      "WHERE a.name = :archivePath " +
      "AND lower(p.content) LIKE lower(concat('%', :word,'%')) "
  )
  Long countPostsByArchivePathAndWord(@Param("archivePath") String archivePath,
      @Param("word") String word);

  @Query("SELECT COUNT(p) FROM posts p WHERE lower(p.content) LIKE lower(concat('%', :word,'%'))")
  Long countPostsByWord(@Param("word") String word);

  /**
   * select count of twits, not count of word in twits (if one tweet have 3 repetition of word -
   * result will be 1 not 3)
   */
  @Query("SELECT COUNT(p) "
      + "FROM posts p "
      + "JOIN p.tweetsFile t "
      + "WHERE lower(p.content) LIKE lower(concat('%', :word,'%')) "
      + "and t.name = :file")
  Long countPostsByWord(@Param("word") String word, @Param("file") String file);

  @Query("SELECT p.content "
      + "FROM posts p "
      + "JOIN p.tweetsFile t "
      + "WHERE lower(p.content) LIKE lower(concat('%', :word,'%')) "
      + "and t.name = :file")
  List<String> findPostsByWord(@Param("word") String word, @Param("file") String file);

}
