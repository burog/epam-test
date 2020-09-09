package fadeevm.data.jpa.parsers;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser.Feature;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.ColumnType;
import fadeevm.data.jpa.domain.Post;
import fadeevm.data.jpa.domain.TweetsFile;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class JacksonCsvParser implements CsvParser {

  private final ObjectReader csvReader;

  public JacksonCsvParser() {
    CsvMapper mapper = new CsvMapper();

    mapper.disable(Feature.IGNORE_TRAILING_UNMAPPABLE);
    mapper.enable(Feature.ALLOW_TRAILING_COMMA);
//    mapper.enable(Feature.SKIP_EMPTY_LINES);

    CsvSchema schema = CsvSchema.builder()
        .addColumn("number", ColumnType.NUMBER)
        .addColumn("date")
        .addColumn("content")
        .setColumnSeparator('|')
        .setLineSeparator('\n')
        .disableQuoteChar()
        .build();

    csvReader = mapper.readerFor(Post.class).with(schema);
  }


  public List<Post> readCsv(Reader csvInput, TweetsFile tweetsFile) {
    List<Post> entries = new LinkedList<>();
    Iterator<Post> it;
    try {
      it = csvReader.readValues(csvInput);
    } catch (IOException e) {
      throw new RuntimeException("some error during reading csv file", e);
    }
    while (it.hasNext()) {
      Post post = it.next();
      log.trace("read post {}", post);
      if (post.getContent() == null) {
        continue;
      }
      entries.add(post);
      post.setTweetsFile(tweetsFile);
    }
    log.trace("read is finished {}", entries);

    return entries;
  }

}
