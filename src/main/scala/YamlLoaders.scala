import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import scala.beans.BeanProperty

object YamlLoaderAsJavaMap extends App {

  override def main(args: Array[String]) = {

    val input = getClass().getResourceAsStream("experiments.yml")

    val yaml = new Yaml()

    val experimentsData: java.util.Map[String, Any] = yaml.load(input)

    println(s"Loaded: ${experimentsData}")
  }
}

object YamlLoaderAsBeans extends App {

  override def main(args: Array[String]) = {

    val input = getClass().getResourceAsStream("experiments.yml")

    val yaml = new Yaml(new Constructor(classOf[Experiments]))

    val experiments: Experiments = yaml.load(input)

    println(s"Loaded: ${experiments}")
  }
}

class Offer {
  @BeanProperty var minScore: Double = 0.0
  @BeanProperty var amount: Int = 0
  @BeanProperty var fee: Int = 0
  @BeanProperty var term: Int = 0
}

class Experiment {
  @BeanProperty var name = ""
  @BeanProperty var startDate = new java.util.Date(0)
  @BeanProperty var endDate = new java.util.Date(0)
  @BeanProperty var offers = new java.util.ArrayList[Offer]
}

class Experiments {
  @BeanProperty var experiments = new java.util.ArrayList[Experiment]
}
