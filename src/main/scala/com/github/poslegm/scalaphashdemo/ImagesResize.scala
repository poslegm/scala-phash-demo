package com.github.poslegm.scalaphashdemo

import java.awt.image.BufferedImage

object ImagesResize {
  // unsafe, can flush origin image!
  def reduceImage(img: BufferedImage): BufferedImage =
    if (img.getWidth >= img.getHeight && img.getWidth > 1024) {
      resize(img, 1024, img.getHeight * 1024 / img.getWidth)
    } else if (img.getHeight >= img.getWidth && img.getHeight > 1024) {
      resize(img, img.getWidth * 1024 / img.getHeight, 1024)
    } else {
      img
    }

  // unsafe, flushes origin image!
  private def resize(img: BufferedImage, width: Int, height: Int): BufferedImage = {
    val destImage = new BufferedImage(width, height, img.getType)
    val g = destImage.createGraphics

    g.drawImage(img, 0, 0, width, height, null)
    g.dispose()

    img.flush()

    destImage
  }
}
