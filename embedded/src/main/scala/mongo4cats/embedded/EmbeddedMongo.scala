package mongo4cats.embedded

import cats.effect.{Async, Resource}
import cats.syntax.apply._
import cats.syntax.applicativeError._
import de.flapdoodle.embed.mongo.config.{MongodConfig, Net}
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.{MongodProcess, MongodStarter}
import de.flapdoodle.embed.process.runtime.Network

import scala.concurrent.duration._

object EmbeddedMongo {
  private val starter = MongodStarter.getDefaultInstance

  def start[F[_]](
      config: MongodConfig,
      maxAttempts: Int = 10,
      attempt: Int = 0,
      lastError: Option[Throwable] = None
  )(implicit F: Async[F]): Resource[F, MongodProcess] =
    if (attempt >= maxAttempts) {
      val error = lastError.getOrElse(new RuntimeException("Failed to start embedded mongo too many times"))
      Resource.eval(error.raiseError[F, MongodProcess])
    } else
      Resource
        .make(F.delay(starter.prepare(config)))(ex => F.delay(ex.stop()))
        .flatMap(ex => Resource.make(F.delay(ex.start()))(p => F.delay(p.stop())))
        .handleErrorWith[MongodProcess, Throwable] { e =>
          Resource.eval(F.sleep(attempt.seconds)) *> start[F](config, maxAttempts, attempt + 1, Some(e))
        }
}

trait EmbeddedMongo {
  protected val mongoHost = "localhost"
  protected val mongoPort = 27017

  def withRunningEmbeddedMongo[F[_]: Async, A](test: => F[A]): F[A] =
    runMongo(mongoHost, mongoPort)(test)

  def withRunningEmbeddedMongo[F[_]: Async, A](host: String, port: Int)(test: => F[A]): F[A] =
    runMongo(host, port)(test)

  private def runMongo[F[_]: Async, A](host: String, port: Int)(test: => F[A]): F[A] =
    EmbeddedMongo
      .start[F] {
        MongodConfig
          .builder()
          .version(Version.Main.PRODUCTION)
          .net(new Net(host, port, Network.localhostIsIPv6))
          .build
      }
      .use(_ => test)
}