package fadeevm.data.jpa.repository;

import fadeevm.data.jpa.domain.TweetsFile;
import org.springframework.data.jpa.repository.JpaRepository;


public interface FileRepository extends JpaRepository<TweetsFile, String> {

}
