package mutable_ref

import AbstractGraphics
import Benchmarkable
import OffsetInSeconds
import asAbstract
import screenHeight
import screenWidth
import worldMap
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.event.*
import java.text.DecimalFormat
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.Timer
import kotlin.math.cos
import kotlin.math.sin

inline operator fun Array<IntArray>.get(location: LocationF2): Int = this[location.x][location.y]

inline fun canMove(point: Point2f2): Boolean = worldMap[point.x.toInt()][point.y.toInt()] == 0

@JvmInline
value class Point2f2(val x: Float, val y: Float) {
    fun plus(vector: Vector2f2, wrapper: MutableMfvcWrapper) {
        val result = Point2f2(x + vector.x, y + vector.y)
        wrapper.encodeLong0(result.x, result.y)
    }

    fun minus(vector: Vector2f2, wrapper: MutableMfvcWrapper) {
        val result = Point2f2(x - vector.x, y - vector.y)
        wrapper.encodeLong0(result.x, result.y)
    }

    fun toLocation(wrapper: MutableMfvcWrapper) {
        val result = LocationF2(x.toInt(), y.toInt())
        wrapper.encodeLong0(result.x, result.y)
    }

    fun toVector(wrapper: MutableMfvcWrapper) {
        val result = Vector2f2(x, y)
        wrapper.encodeLong0(result.x, result.y)
    }
}

inline fun Unit.decodePoint2f2(wrapper: MutableMfvcWrapper) = Point2f2(wrapper.decodeFloat00(), wrapper.decodeFloat01())
inline fun Unit.decodeVector2f2(wrapper: MutableMfvcWrapper) = Vector2f2(wrapper.decodeFloat00(), wrapper.decodeFloat01())
inline fun Unit.decodeLocationF2(wrapper: MutableMfvcWrapper) = LocationF2(wrapper.decodeInt00(), wrapper.decodeInt01())

@JvmInline
value class Vector2f2(val x: Float, val y: Float) {
    fun rotate(angle: Float, wrapper: MutableMfvcWrapper) {
        val res = Vector2f2(x * cos(angle) - y * sin(angle), x * sin(angle) + y * cos(angle))
        wrapper.encodeLong0(res.x, res.y)
    }

    fun times(factor: Float, wrapper: MutableMfvcWrapper) {
        val res = Vector2f2(x * factor, y * factor)
        wrapper.encodeLong0(res.x, res.y)
    }

    fun plus(vector: Vector2f2, wrapper: MutableMfvcWrapper) {
        val res = Vector2f2(x + vector.x, y + vector.y)
        wrapper.encodeLong0(res.x, res.y)
    }

    fun minus(vector: Vector2f2, wrapper: MutableMfvcWrapper) {
        val res = Vector2f2(x - vector.x, y - vector.y)
        wrapper.encodeLong0(res.x, res.y)
    }

    fun abs(wrapper: MutableMfvcWrapper) {
        val res = Vector2f2(Math.abs(x), Math.abs(y))
        wrapper.encodeLong0(res.x, res.y)
    }

    fun xProjection(wrapper: MutableMfvcWrapper) {
        val res = Vector2f2(x, 0.0f)
        wrapper.encodeLong0(res.x, res.y)
    }

    fun yProjection(wrapper: MutableMfvcWrapper) {
        val res = Vector2f2(0.0f, y)
        wrapper.encodeLong0(res.x, res.y)
    }
}

@JvmInline
value class LocationF2(val x: Int, val y: Int) {
    fun toVector(wrapper: MutableMfvcWrapper) {
        val res = Vector2f2(x.toFloat(), y.toFloat())
        wrapper.encodeLong0(res.x, res.y)
    }

    fun step(vector: Vector2f2, wrapper: MutableMfvcWrapper) {
        val res = LocationF2(
            (toVector(wrapper).decodeVector2f2(wrapper).plus(vector, wrapper).decodeVector2f2(wrapper)).x.toInt(),
            (toVector(wrapper).decodeVector2f2(wrapper).plus(vector, wrapper).decodeVector2f2(wrapper)).y.toInt()
        )
        wrapper.encodeLong0(res.x, res.y)
    }
}

fun Float.div(vector: Vector2f2, wrapper: MutableMfvcWrapper) {
    val res = Vector2f2(this / vector.x, this / vector.y)
    wrapper.encodeLong0(res.x, res.y)
}

class MyPanelF2 : JPanel(), KeyListener, MouseListener, Benchmarkable {
    private var startTime: Long = System.nanoTime()
    private var frameCount = 0
    private var fps = 100.0
    private var minFps = 100.0
    private val veryStartTime = System.nanoTime()

    var pos = Point2f2(22.0f, 12.0f) // start position

    var dir = Vector2f2(-1.0f, 0.0f) // direction vector

    var plane = Vector2f2(0.0f, 0.66f) //the 2d raycaster version of camera plane
    init {
        addKeyListener(this)
        addMouseListener(this)
        isFocusable = true

        // Set up the timer to update the FPS every second
        Timer(1000) {
            // Calculate the FPS and update the label text
            val currentTime = System.nanoTime()
            val elapsedTime = (currentTime - startTime) / 1e9
            fps = frameCount / elapsedTime
            if ((currentTime - veryStartTime) / 1e9 > OffsetInSeconds) {
                minFps = minFps.coerceAtMost(fps)
            }
            startTime = currentTime
            frameCount = 0
        }.start()
        startTime = System.nanoTime()
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        val g2d = g as Graphics2D

        mainLoop(g.asAbstract())

        g.color = Color.BLACK
        g.drawString("Current FPS: ${DecimalFormat("#.#").format(fps)}, Min FPS: ${DecimalFormat("#.#").format(minFps)}", 10, 20)

        frameCount++
    }

    private fun mainLoop(g: AbstractGraphics) {
        repeat(100) {
            drawScene(g)
        }
    }

    override fun heavyAction(graphics: AbstractGraphics) = drawScene(graphics)

    private fun drawScene(g: AbstractGraphics) {
        val wrapper = MutableMfvcWrapper()
        for (x in 0 until screenWidth) {
            //calculate ray position and direction
            val cameraX = 2 * x / screenHeight.toFloat() - 1 //x-coordinate in camera space
            val rayDir =
                dir.plus(plane.times(cameraX, wrapper).decodeVector2f2(wrapper), wrapper).decodeVector2f2(wrapper)
            //which box of the map we're in
            var mapLocation = pos.toLocation(wrapper).decodeLocationF2(wrapper)

            //length of ray from current position to next x or y-side
            var sideDist: Vector2f2

            //length of ray from one x or y-side to next x or y-side
            //these are derived as:
            //deltaDistX = sqrt(1 + (rayDirY * rayDirY) / (rayDirX * rayDirX))
            //deltaDistY = sqrt(1 + (rayDirX * rayDirX) / (rayDirY * rayDirY))
            //which can be simplified to abs(|rayDir| / rayDirX) and abs(|rayDir| / rayDirY)
            //where |rayDir| is the length of the vector (rayDirX, rayDirY). Its length,
            //unlike (dirX, dirY) is not 1, however this does not matter, only the
            //ratio between deltaDistX and deltaDistY matters, due to the way the DDA
            //stepping further below works. So the values can be computed as below.
            // Division through zero is prevented, even though technically that's not
            // needed in C++ with IEEE 754 floating point values.
            val deltaDist = 1.0f.div(rayDir.abs(wrapper).decodeVector2f2(wrapper), wrapper).decodeVector2f2(wrapper)

            var perpWallDist: Float

            //what direction to step in x or y-direction (either +1 or -1)
            var step: Vector2f2

            var hit = 0 //was there a wall hit?
            var side = 0 //was a NS or a EW wall hit?
            //calculate step and initial sideDist
            if (rayDir.x < 0) {
                step = Vector2f2((-1).toFloat(), 0.0f)
                sideDist = (pos.toVector(wrapper).decodeVector2f2(wrapper)
                    .minus(mapLocation.toVector(wrapper).decodeVector2f2(wrapper), wrapper)
                    .decodeVector2f2(wrapper)).xProjection(wrapper).decodeVector2f2(wrapper)
                    .times(deltaDist.x, wrapper).decodeVector2f2(wrapper)
            } else {
                step = Vector2f2(1.toFloat(), 0.0f)
                sideDist = (mapLocation.toVector(wrapper).decodeVector2f2(wrapper).plus(Vector2f2(1.0f, 0.0f), wrapper)
                    .decodeVector2f2(wrapper)
                    .minus(pos.toVector(wrapper).decodeVector2f2(wrapper), wrapper).decodeVector2f2(wrapper)).xProjection(
                        wrapper
                    ).decodeVector2f2(wrapper)
                    .times(deltaDist.x, wrapper).decodeVector2f2(wrapper)
            }
            if (rayDir.y < 0) {
                step = step.plus(Vector2f2(0.0f, (-1).toFloat()), wrapper).decodeVector2f2(wrapper)
                sideDist = sideDist.plus(
                    (pos.toVector(wrapper).decodeVector2f2(wrapper)
                        .minus(mapLocation.toVector(wrapper).decodeVector2f2(wrapper), wrapper)
                        .decodeVector2f2(wrapper)).yProjection(wrapper).decodeVector2f2(wrapper)
                        .times(deltaDist.y, wrapper).decodeVector2f2(wrapper), wrapper
                ).decodeVector2f2(wrapper)
            } else {
                step = step.plus(Vector2f2(0.0f, 1.toFloat()), wrapper).decodeVector2f2(wrapper)
                sideDist = sideDist.plus(
                    (mapLocation.toVector(wrapper).decodeVector2f2(wrapper).plus(Vector2f2(0.0f, 1.0f), wrapper)
                        .decodeVector2f2(wrapper)
                        .minus(pos.toVector(wrapper).decodeVector2f2(wrapper), wrapper)
                        .decodeVector2f2(wrapper)).yProjection(wrapper).decodeVector2f2(wrapper)
                        .times(deltaDist.y, wrapper).decodeVector2f2(wrapper), wrapper
                ).decodeVector2f2(wrapper)
            }
            //perform DDA
            while (hit == 0) {
                //jump to next map square, either in x-direction, or in y-direction
                if (sideDist.x < sideDist.y) {
                    sideDist = sideDist.plus(deltaDist.xProjection(wrapper).decodeVector2f2(wrapper), wrapper)
                        .decodeVector2f2(wrapper)
                    mapLocation = mapLocation.step(step.xProjection(wrapper).decodeVector2f2(wrapper), wrapper)
                        .decodeLocationF2(wrapper)
                    side = 0
                } else {
                    sideDist = sideDist.plus(deltaDist.yProjection(wrapper).decodeVector2f2(wrapper), wrapper)
                        .decodeVector2f2(wrapper)
                    mapLocation = mapLocation.step(step.yProjection(wrapper).decodeVector2f2(wrapper), wrapper)
                        .decodeLocationF2(wrapper)
                    side = 1
                }
                //Check if ray has hit a wall
                if (worldMap[mapLocation] > 0) hit = 1
            }
            //Calculate distance projected on camera direction. This is the shortest distance from the point where the wall is
            //hit to the camera plane. Euclidean to center camera point would give fisheye effect!
            //This can be computed as (mapX - posX + (1 - stepX) / 2) / rayDirX for side == 0, or same formula with Y
            //for size == 1, but can be simplified to the code below thanks to how sideDist and deltaDist are computed:
            //because they were left scaled to |rayDir|. sideDist is the entire length of the ray above after the multiple
            //steps, but we subtract deltaDist once because one step more into the wall was taken above.
            perpWallDist =
                if (side == 0) (sideDist.minus(deltaDist, wrapper).decodeVector2f2(wrapper)).x
                else (sideDist.minus(deltaDist, wrapper).decodeVector2f2(wrapper)).y

            //Calculate height of line to draw on screen
            val lineHeight = (screenHeight / perpWallDist).toInt()

            //calculate lowest and highest pixel to fill in current stripe
            val drawStart = (-lineHeight / 2 + screenHeight / 2).coerceAtLeast(0)
            val drawEnd = (lineHeight / 2 + screenHeight / 2).coerceAtMost(screenHeight - 1)

            //choose wall color
            var color =
                when (worldMap[mapLocation]) {
                    1 -> 0xFF0000 //red
                    2 -> 0x00FF00 //green
                    3 -> 0x00FF00 //blue
                    4 -> 0x0000FF //white
                    else -> 0xFFFF00 //yellow
                }

            //give x and y sides different brightness
            if (side == 1) {
                color /= 2
            }

            //draw the pixels of the stripe as a vertical line
            g.color = Color(color)
            g.drawLine(x, drawStart, x, drawEnd)
        }
    }

    override fun keyPressed(e: KeyEvent) {
        val frameTime = 1 / fps.toFloat()
        //speed modifiers

        //speed modifiers
        val moveSpeed = frameTime * .5f //the constant value is in squares/second
        val rotSpeed = frameTime * .3f //the constant value is in radians/second
        val wrapper = MutableMfvcWrapper()
        when (e.keyCode) {
            KeyEvent.VK_UP -> {
                if(canMove((pos.plus((dir.times(moveSpeed, wrapper).decodeVector2f2(wrapper)), wrapper)).decodePoint2f2(wrapper))) {
                    pos = (pos.plus((dir.times(moveSpeed, wrapper).decodeVector2f2(wrapper)).xProjection(wrapper).decodeVector2f2(wrapper), wrapper)).decodePoint2f2(wrapper)
                }
                if(canMove((pos.plus((dir.times(moveSpeed, wrapper).decodeVector2f2(wrapper)).yProjection(wrapper).decodeVector2f2(wrapper), wrapper)).decodePoint2f2(wrapper))) {
                    pos = pos.plus((dir.times(moveSpeed, wrapper).decodeVector2f2(wrapper)).yProjection(wrapper).decodeVector2f2(wrapper), wrapper).decodePoint2f2(wrapper)
                }
            }
            KeyEvent.VK_DOWN -> {
                if(canMove(pos.minus((dir.times(moveSpeed, wrapper).decodeVector2f2(wrapper)).xProjection(wrapper).decodeVector2f2(wrapper), wrapper).decodePoint2f2(wrapper))) {
                    pos = pos.minus((dir.times(moveSpeed, wrapper).decodeVector2f2(wrapper)).xProjection(wrapper).decodeVector2f2(wrapper), wrapper).decodePoint2f2(wrapper)
                }
                if(canMove(pos.minus((dir.times(moveSpeed, wrapper).decodeVector2f2(wrapper)).yProjection(wrapper).decodeVector2f2(wrapper), wrapper).decodePoint2f2(wrapper))) {
                    pos = pos.minus((dir.times(moveSpeed, wrapper).decodeVector2f2(wrapper)).yProjection(wrapper).decodeVector2f2(wrapper), wrapper).decodePoint2f2(wrapper)
                }
            }
            KeyEvent.VK_LEFT -> {
                //both camera direction and camera plane must be rotated
                dir = dir.rotate(rotSpeed, wrapper).decodeVector2f2(wrapper)
                plane = plane.rotate(rotSpeed, wrapper).decodeVector2f2(wrapper)
            }
            KeyEvent.VK_RIGHT -> {
                //both camera direction and camera plane must be rotated
                dir = dir.rotate(-rotSpeed, wrapper).decodeVector2f2(wrapper)
                plane = plane.rotate(-rotSpeed, wrapper).decodeVector2f2(wrapper)
            }
        }
        repaint()
    }

    override fun mouseClicked(e: MouseEvent) {}
    override fun mouseEntered(e: MouseEvent) {}
    override fun mouseExited(e: MouseEvent) {}
    override fun mousePressed(e: MouseEvent) {}
    override fun mouseReleased(e: MouseEvent) {}
    override fun keyTyped(e: KeyEvent) {}
    override fun keyReleased(e: KeyEvent) {}
}

class MyFrameF2 : JFrame() {
    init {
        add(MyPanelF2())
        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(screenWidth, screenHeight)
        isVisible = true

        // Set up the game loop
        Thread {
            while (true) {
                // Repaint the panel and sleep for a short time
                SwingUtilities.invokeLater {
                    contentPane.repaint()
                }
                Thread.sleep(10)
            }
        }.start()
    }
}

fun main() {
    MyFrameF2()
}
