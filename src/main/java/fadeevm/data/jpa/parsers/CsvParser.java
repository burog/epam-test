package fadeevm.data.jpa.parsers;

import fadeevm.data.jpa.domain.Post;
import fadeevm.data.jpa.domain.TweetsFile;
import java.io.Reader;
import java.util.List;

public interface CsvParser {

  List<Post> readCsv(Reader csvInput, TweetsFile tweetsFile);
}
