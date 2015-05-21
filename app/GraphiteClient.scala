package com.olx.location.driver

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.libs.Akka
import java.net.{ DatagramPacket, DatagramSocket, InetAddress, UnknownHostException }
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import play.api.Logger

object GraphiteClient {
  
  def apply(): GraphiteClient = {
    lazy val interval = 60
    lazy val enabled = true

    lazy val client = new GraphiteClient(
      new GraphiteSender("graphite.innovations.olx.com", 2003),
      new MetricsBuffer("prd.us.east.aws.inv.application.location."),
      enabled)
    if (enabled) {
      Akka.system.scheduler.schedule(0 seconds, interval seconds, new Runnable {
        def run() {
          client.flush
        }
      })
    }

    client
  }
}

/**
 * This class stores Smaug metrics in memory every time is called.
 * If the metric already exists then adds the new value to the previous one. If not, adds a new metric.
 * When the method flush is called, all metrics are send to Graphite using the GraphiteSender.
 */
class GraphiteClient(graphiteSender: GraphiteSender, metricsBuffer: MetricsBuffer, enabled: Boolean) {

  def hasMetric(name: String) = metricsBuffer.hasMetric(name)
  def getMetric(name: String) = metricsBuffer.getMetric(name)
  def getMetricsSize = metricsBuffer.getMetricsSize

  /**
   * Add a metric to the buffer.
   * If the metric already exists, it will increase it by <value>.
   */
  def addMetric(name: String, value: Int) {
    if (enabled) metricsBuffer.addMetric(name, value)
  }

  /**
   * addMetric overload that assumes value=1.
   * This method is preferred over addMetric(name: String, value: Int).
   * It needs to exists as an overload because arguments with default values are super effective against spec2.
   */
  def addMetric(name: String) {
    addMetric(name, 1)
  }

  def addAverageMetric(name: String, value: Long) = {
    if (enabled) metricsBuffer.addAverageMetric(name, value)
  }

  def flush = {
    if (getMetricsSize != 0) {
      val ts = System.currentTimeMillis()
      metricsBuffer.getMetrics.foreach {
        kv =>
          {
            val metricAsText = metricsBuffer.getMetricAsPlainText(kv, ts, getHostName)
            graphiteSender.send(metricAsText)
          }
      }
      metricsBuffer.clearMetrics
    }
  }

  def produceGraphiteData(metricName: String, ts: Long, hostName: String) = {
    val metric = metricsBuffer.getMetric(metricName)
    metric match {
      case Some(value) => metricsBuffer.getMetricAsPlainText((metricName, value), ts, hostName)
      case None => ""
    }
  }

  def getHostName: String = {
    try {
      InetAddress.getLocalHost().getHostName()
    } catch {
      case e: UnknownHostException => "any"
    }
  }
}

/**
 * This class is used by GraphiteClient to store the metrics in memory all together.
 * It is implemented using a Map, saving the name of the metric and a pair.
 * There will be two kinds of metrics: simple metrics and average metrics. For the average metrics
 * the first component of the pair is a counter that represents how many metrics of the same type were stored.
 * The counter is used to calculate the average metric value that is saved in the second component.
 * For the simple metrics, the first component will be always 1 because the average is not needed.
 *
 */
class MetricsBuffer(prefix: String) {
  private val metrics: scala.collection.mutable.Map[String, (Int, Long)] = scala.collection.mutable.Map()
  def hasMetric(name: String) = metrics.contains(name)
  def getMetric(name: String) = metrics.get(name)

  def getMetricsSize = metrics.size

  def addMetric(name: String, value: Long) = {
    if (metrics.contains(name)) metrics.update(name, (1, metrics.get(name).get._2 + value))
    else metrics += (name -> (1, value))
  }

  def addAverageMetric(name: String, value: Long) = {
    if (metrics.contains(name)) metrics.update(name, (metrics.get(name).get._1 + 1, metrics.get(name).get._2 + value))
    else metrics += (name -> (1, value))
  }

  def clearMetrics {
    metrics.clear
  }

  def getMetrics = metrics

  def getMetricAsPlainText(metric: (String, (Int, Long)), ts: Long, hostName: String): String = {

    val prefixWithHostName = prefix + hostName + "."
    val tsString = ts.toString.substring(0, 10)
    val text = new StringBuilder
    val value: Double = metric._2._2.toDouble / metric._2._1.toDouble
    text.append(prefixWithHostName + metric._1 + ' ' + value + ' ' + tsString + "\n")

    text.toString
  }

}

class GraphiteSender(host: String, port: Int) {

  def send(text: String) = {
    Logger.info("Send graphite: " + text);
    val sock = new DatagramSocket();
    val addr = InetAddress.getByName(host);
    val packet = new DatagramPacket(text.getBytes(), text.length, addr, port);

    sock.send(packet);
    sock.close();
  }
}







