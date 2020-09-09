package fadeevm.data.jpa.parsers;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import fadeevm.data.jpa.domain.Post;
import fadeevm.data.jpa.domain.TweetsFile;
import java.io.Reader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CsvApacheParser implements CsvParser {

  private final ObjectReader csvReader;

  public CsvApacheParser() {

    CsvSchema csvSchema = CsvSchema.emptySchema()
        .withLineSeparator("\n")
        .withColumnSeparator('|');
    csvReader = new CsvMapper().readerFor(List.class).with(csvSchema);
  }

  public List<Post> readCsv(Reader csvInput, TweetsFile tweetsFile) {
    List<Post> entries = new LinkedList<>();

    final CSVParser parser = new CSVParserBuilder()
        .withSeparator('|')
        .withIgnoreQuotations(true)
        .build();
    final CSVReader csvReader = new CSVReaderBuilder(csvInput)
        .withCSVParser(parser)
        .build();

    Iterator<String[]> it = csvReader.iterator();
    while (it.hasNext()) {
      String[] row = it.next();
      if (row == null || row.length < 3) {
        continue;
      }
      log.trace("read post {}", Arrays.toString(row));
      Post post = new Post();
      try {
        post.setNumber(Long.valueOf(row[0]));
      } catch (NumberFormatException e) {
        post.setNumber(0L);
      }
      post.setDate(row[1]);

      if (row[2] == null) {
        continue;
      }

      StringBuilder stringBuilder = new StringBuilder();
      for (int i = 2; i < row.length; i++) {
        stringBuilder.append(row[i]);
      }
      post.setContent(stringBuilder.toString());

      entries.add(post);
      post.setTweetsFile(tweetsFile);
    }
    log.trace("read is finished {}", entries);

    return entries;
  }


}
