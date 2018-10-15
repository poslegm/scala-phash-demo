package scalaphashdemo

import java.util.concurrent.Executors

import cats.effect.{ExitCode, IO, IOApp}
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.dsl.io.http4sKleisliResponseSyntax

import scala.concurrent.ExecutionContext

object PHashServer extends IOApp {
  private val ioEC = ExecutionContext.fromExecutorService(Executors.newCachedThreadPool())
  private val computationEC = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(6))

  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(8080, "localhost")
      .withHttpApp(new PHashService[IO, IO.Par](ioEC, computationEC).routes.orNotFound)
      .serve
      .compile
      .drain
      .map(_ => ExitCode.Success)
}