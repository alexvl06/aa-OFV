package co.com.alianza.persistence.util

import com.typesafe.config.ConfigFactory
import slick.util.AsyncExecutor

import scala.concurrent.ExecutionContext

object DataBaseExecutionContext {

  private val numThreads = 9
  private val queueSize = 1000
  implicit val executionContext: ExecutionContext = AsyncExecutor("DataAccessAdapter", numThreads, queueSize).executionContext

}