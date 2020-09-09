package fadeevm.data.jpa.domain;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity(name = "files")
public class TweetsFile implements Serializable {

  private static final long serialVersionUID = 8832911775929388512L;
  @Id
  @GeneratedValue
  Long id;
  @Column
  String name;

  @ManyToOne(optional = false)
  Archive archive;
}
