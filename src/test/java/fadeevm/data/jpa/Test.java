package fadeevm.data.jpa;

import fadeevm.data.jpa.domain.Post;
import fadeevm.data.jpa.domain.TweetsFile;
import fadeevm.data.jpa.parsers.CsvApacheParser;
import fadeevm.data.jpa.service.ConsoleRunner;
import java.io.StringReader;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = SampleDataJpaApplication.class)
@RunWith(SpringRunner.class)

@Slf4j
public class Test {

  @Autowired
  ConsoleRunner consoleRunner;

  @Before
  public void setUp() throws Exception {

    String additionalZip = consoleRunner.performAdditionalZip(
        "C:\\Users\\fadeevm\\Downloads\\Health-News-Tweets_nytimeshealth.zip");

  }

//  @Ignore
  @org.junit.Test
  public void name() {
    consoleRunner.performLoading("C:\\Users\\fadeevm\\Downloads\\Health-News-Tweets.zip");
  }


  @org.junit.Test
  public void testParser(){
    CsvApacheParser csvParser = new CsvApacheParser();
    String input =
        "586278450392133633|Thu Apr 09 21:24:09 +0000 2015|Planning to hire a personal trainer? Read these 7 tips first: http://ow.ly/LpxFq\n"
            + "586260156155043843|Thu Apr 09 20:11:28 +0000 2015|RT @AnnaMedaris: | Any dads out their who struggled w/ #depression or #anxiety after their kid was born? Let's talk! amiller[at]usnews[dot]co…\n"
            + "397344404072960001|Mon Nov 04 12:47:42 +0000 2013|RT @cmaries08: Thanks, @cnnhealth | finally a solid explanation | Good!: \"The ACA penalizes hospitals that see patients return\" http://t.co…\n"
            + "\n";

    List<Post> posts = csvParser.readCsv(new StringReader(input), new TweetsFile());
    log.error("mafa result: " + posts);
  }
}
