package fadeevm.data.jpa.domain;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity(name = "archives")
public class Archive implements Serializable {

  private static final long serialVersionUID = -6946892654227377583L;
  @Id
  @GeneratedValue
  Long id;
  @Column
  String name;

}
