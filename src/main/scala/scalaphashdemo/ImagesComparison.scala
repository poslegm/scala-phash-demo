package scalaphashdemo

import java.awt.image.BufferedImage

import cats.Parallel
import cats.effect.Sync
import cats.syntax.all._
import scalaphash.PHash
import scalaphash.PHash.{DCTHash, MarrHash, RadialHash}

object ImagesComparison {
  case class ImagesComparisonResult(dctDistance: Long, marrDistance: Double, radialDistance: Double, similar: Boolean)

  def computeHashes[F[_], G[_]](
    image1: BufferedImage,
    image2: BufferedImage
  )(implicit S: Sync[F], P: Parallel[F, G]): F[ImagesComparisonResult] =
    (
      S.delay(PHash.dctHash(image1)).rethrow,
      S.delay(PHash.marrHash(image1)).rethrow,
      S.delay(PHash.radialHash(image1)).rethrow,
      S.delay(PHash.dctHash(image2)).rethrow,
      S.delay(PHash.marrHash(image2)).rethrow,
      S.delay(PHash.radialHash(image2)).rethrow
    ).parMapN(compareHashes).rethrow

  private def compareHashes(
    image1Dct: DCTHash,
    image1Marr: MarrHash,
    image1Radial: RadialHash,
    image2Dct: DCTHash,
    image2Marr: MarrHash,
    image2Radial: RadialHash
  ): Either[Throwable, ImagesComparisonResult] =
    for {
      marrCompare <- Either.fromOption(
        PHash.marrHashDistance(image1Marr, image2Marr).filterNot(d => d.isInfinity || d.isNaN),
        new IllegalStateException("Can not compute Marr distance")
      )
      radialCompareRaw = PHash.radialHashDistance(image1Radial, image2Radial)
      radialCompare <- Either.cond(
        !radialCompareRaw.isNaN && !radialCompareRaw.isInfinity,
        radialCompareRaw,
        new IllegalStateException("Can not compute radial distance")
      )
      dctCompare = PHash.dctHashDistance(image1Dct, image2Dct)
    } yield {
      ImagesComparisonResult(
        dctCompare,
        marrCompare,
        radialCompare,
        predictSimilarity(dctCompare, marrCompare, radialCompare)
      )
    }

  private def predictSimilarity(dctDistance: Long, marrDistance: Double, radialDistance: Double): Boolean =
    List(
      dctDistance < Config.config.dctThreshold,
      marrDistance < Config.config.marrThreshold,
      radialDistance > Config.config.radialThreshold
    ).count(identity) > 2
}
