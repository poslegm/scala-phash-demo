package com.github.poslegm.scalaphashdemo

import java.awt.image.BufferedImage

import cats.Parallel
import cats.effect.Sync
import cats.syntax.either._
import cats.syntax.monadError._
import cats.syntax.parallel._
import com.github.poslegm.scalaphash.PHash
import com.github.poslegm.scalaphash.PHash.{DCTHash, MarrHash, RadialHash}

object ImagesComparison {
  case class ImagesComparisonResult(dctDistance: Long, marrDistance: Double, radialDistance: Double)

  def computeHashes[F[_], G[_]](
    image1: BufferedImage,
    image2: BufferedImage
  )(implicit S: Sync[F], P: Parallel[F, G]): F[ImagesComparisonResult] =
    (
      S.delay { println("d1"); PHash.dctHash(image1) }.rethrow,
      S.delay { println("m1"); PHash.marrHash(image1) }.rethrow,
      S.delay { println("r1"); PHash.radialHash(image1) }.rethrow,
      S.delay { println("d2"); PHash.dctHash(image2) }.rethrow,
      S.delay { println("m2"); PHash.marrHash(image2) }.rethrow,
      S.delay { println("r2"); PHash.radialHash(image2) }.rethrow
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
      println(image1Dct, image2Dct)
      ImagesComparisonResult(dctCompare, marrCompare, radialCompare)
    }
}
