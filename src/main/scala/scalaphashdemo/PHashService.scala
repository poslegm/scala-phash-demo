package scalaphashdemo

import java.awt.image.BufferedImage
import java.io.File

import cats.Parallel
import cats.effect.{ConcurrentEffect, ContextShift}
import cats.instances.list._
import cats.syntax.all._
import fs2.io.toInputStream
import javax.imageio.ImageIO
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`
import org.http4s.multipart.{Multipart, Part}
import org.http4s.twirl._
import org.http4s.{HttpRoutes, MediaType, StaticFile}
import org.log4s.{getLogger, Logger}
import scalaphashdemo.ImagesComparison.computeHashes
import scalaphashdemo.ImagesResize.reduceImage
import scalaphashdemo.PHashService.InvalidImagesException

import scala.concurrent.ExecutionContext

object PHashService {
  case object InvalidImagesException extends Throwable
}

// TODO ссылка на либу
// TODO ссылка на исходники демо
// TODO пометка в сообщении об ошибке, что это может быть баг либы
class PHashService[F[_], G[_]](ioEC: ExecutionContext, computationEC: ExecutionContext)(
  implicit E: ConcurrentEffect[F],
  cs: ContextShift[F],
  P: Parallel[F, G]
) extends Http4sDsl[F] {
  private val logger: Logger = getLogger

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root =>
      Ok(html.index(html.inputform()))

    case request @ GET -> Root / "style.css" =>
      StaticFile.fromFile(new File("src/main/resources/static/style.css"), ioEC, Some(request)).getOrElseF(NotFound())

    case request @ POST -> Root / "submit-images" =>
      request.decode[Multipart[F]] { multipart =>
        val eff = for {
          (originImage1, originImage2) <- deserializeImages(multipart)
          s1 <- E.delay(System.currentTimeMillis())
          (image1, image2) <- reduceImages(originImage1, originImage2)
          _ <- E.delay(logger.debug(s"reduced in ${System.currentTimeMillis() - s1}"))
          _ <- E.delay {
            ImageIO.write(image1, "jpg", new File("example1.jpg"))
            ImageIO.write(image2, "jpg", new File("example2.jpg"))
          }
          s <- E.delay(System.currentTimeMillis())
          _ <- E.delay(logger.debug(s"hashes started..."))
          result <- cs.evalOn(computationEC)(computeHashes(image1, image2))
          _ <- E.delay(logger.debug(s"result: $result, ${System.currentTimeMillis() - s}"))
          html <- Ok(html.index(html.resultblock(result)))
        } yield html

        eff.handleErrorWith {
          case InvalidImagesException =>
            logger.error("invalid images")
            Ok(html.index(html.errorblock("invalid images (supported JPEG only)")))
          case error =>
            logger.error(error)("failed to compute hashes")
            val frontendMsg = "failed to compute hashes (may be bug in scala-phash or scala-phash demo)"
            Ok(html.index(html.errorblock(frontendMsg)))
        }
      }
  }

  private def deserializeImages(multipart: Multipart[F]): F[(BufferedImage, BufferedImage)] =
    multipart.parts.toList
      .filter(_.headers.exists(_ == `Content-Type`(MediaType.image.jpeg)))
      .traverse(parseRequestPart)
      .flatMap {
        case image1 :: image2 :: Nil => E.pure(image1 -> image2)
        case _ => E.raiseError[(BufferedImage, BufferedImage)](InvalidImagesException)
      }

  private def parseRequestPart(part: Part[F]): F[BufferedImage] =
    part.body
      .through(toInputStream)
      .evalMap(imageBytesStream => cs.evalOn(ioEC)(E.delay(ImageIO.read(imageBytesStream))))
      .compile
      .toList
      .map(_.head)

  private def reduceImages(img1: BufferedImage, img2: BufferedImage): F[(BufferedImage, BufferedImage)] =
    (E.delay(reduceImage(img1)), E.delay(reduceImage(img2))).parMapN((a, b) => a -> b)
}
