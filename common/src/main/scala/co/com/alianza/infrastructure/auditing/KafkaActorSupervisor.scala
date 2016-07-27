package co.com.alianza.infrastructure.auditing

import java.util.Properties

import akka.actor.{ Actor, ActorLogging, Props }
import akka.routing.RoundRobinPool
import co.com.alianza.exceptions.{ AlianzaException, TechnicalLevel }
import co.com.alianza.infrastructure.auditing.AuditingMessages.AuditRequest
import co.com.alianza.util.json.JsonUtil
import com.typesafe.config.{ Config, ConfigFactory }
import kafka.producer.{ KeyedMessage, ProducerConfig, Producer }

class KafkaActorSupervisor extends Actor with ActorLogging {

  import akka.actor.OneForOneStrategy
  import akka.actor.SupervisorStrategy._

  val kafkaActor = context.actorOf(Props[KafkaActor].withRouter(RoundRobinPool(nrOfInstances = 10)), "kafkaActor")

  def receive = {
    case message: Any =>
      kafkaActor forward message
  }

  override val supervisorStrategy = OneForOneStrategy() {
    case exception: Exception =>
      exception.printStackTrace()
      log.error(exception, exception.getMessage)
      Restart
  }

}

class KafkaActor extends Actor with ActorLogging {

  import scala.concurrent.ExecutionContext

  implicit val _: ExecutionContext = context.dispatcher

  val kafkaConf: Config = ConfigFactory.load

  def receive = {

    case message: AuditRequest =>
      sendToKafka(JsonUtil.toJson(message), message.kafkaTopic)

    case any: Any =>
      val currentSender = sender()
      currentSender ! AlianzaException(new Exception(s"El mensaje recibido no es soportado actualmente: $any"), TechnicalLevel, "Error mesnaje no soportado Fiducia/KafkaActor")

  }

  /**
   * Sends a byte representation of the transaction 't' to Kafka, under the topic named after
   * the transaction's entity, so it can be collected later on in order to apply the timestamp and
   * persist it on the offline data center.
   */
  private def sendToKafka(message: String, topic: String) {

    val kafkaServers: String = kafkaConf.getString("kafka.alianza.servers")

    val props: Properties = new Properties()
    props.put("metadata.broker.list", kafkaServers)
    props.put("serializer.class", "kafka.serializer.StringEncoder")
    props.put("request.required.acks", "1")

    val config: ProducerConfig = new ProducerConfig(props)
    val producer: Producer[String, String] = new Producer[String, String](config)
    val data = new KeyedMessage[String, String](topic, "key", "partKey", message)

    try {
      //producer.send(data)
      println("Mensaje enviado exitosamente a los brokers de kafka")
    } catch {
      case e: Exception =>
        println("Error enviando el mensaje a los brokers de kafka: " + kafkaServers)
    } finally {
      producer.close
    }
  }

}