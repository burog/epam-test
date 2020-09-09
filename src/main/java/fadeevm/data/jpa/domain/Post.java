package fadeevm.data.jpa.domain;

import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity(name = "posts")
public class Post implements Serializable {

  private static final long serialVersionUID = -1457860238120775323L;

  @Id
  @GeneratedValue
  private Long id;

  @Column
  private Long number;
  @Column
  private String date;
  @Column(nullable = false, length = 2000)
  private String content;
  @ManyToOne(optional = false)
  private TweetsFile tweetsFile;


}
