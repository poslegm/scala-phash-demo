package scalaphashdemo
import java.awt.image.BufferedImage

object ImagesResize {
  private val maxSize = Config.config.maxImageSize
  // unsafe, can flush origin image!
  def reduceImage(img: BufferedImage): BufferedImage =
    if (img.getWidth >= img.getHeight && img.getWidth > maxSize) {
      resize(img, maxSize, img.getHeight * maxSize / img.getWidth)
    } else if (img.getHeight >= img.getWidth && img.getHeight > maxSize) {
      resize(img, img.getWidth * maxSize / img.getHeight, maxSize)
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
