@file:Suppress("NOTHING_TO_INLINE")

package mutable_ref

import AbstractGraphics
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

inline operator fun Array<IntArray>.get(location: LocationD): Int = this[location.x][location.y]

inline fun canMove(point: Point2d): Boolean = worldMap[point.x.toInt()][point.y.toInt()] == 0

@JvmInline
value class Point2d(val x: Double, val y: Double) {
    fun plus(vector: Vector2d, wrapper: MutableMfvcWrapper) {
        val result = Point2d(x + vector.x, y + vector.y)
        wrapper.long0 = result.x.toRawBits()
        wrapper.long1 = result.y.toRawBits()
    }

    fun minus(vector: Vector2d, wrapper: MutableMfvcWrapper) {
        val result = Point2d(x - vector.x, y - vector.y)
        wrapper.long0 = result.x.toRawBits()
        wrapper.long1 = result.y.toRawBits()
    }

    fun toLocation(wrapper: MutableMfvcWrapper) {
        val result = LocationD(x.toInt(), y.toInt())
        wrapper.long0 = result.x.toLong()
        wrapper.long1 = result.y.toLong()
    }

    fun toVector(wrapper: MutableMfvcWrapper) {
        val result = Vector2d(x, y)
        wrapper.long0 = result.x.toRawBits()
        wrapper.long1 = result.y.toRawBits()
    }
}

@JvmInline
value class Vector2d(val x: Double, val y: Double) {
    fun rotate(angle: Double, wrapper: MutableMfvcWrapper) {
        val res = Vector2d(x * cos(angle) - y * sin(angle), x * sin(angle) + y * cos(angle))
        wrapper.long0 = res.x.toRawBits()
        wrapper.long1 = res.y.toRawBits()
    }

    fun times(factor: Double, wrapper: MutableMfvcWrapper) {
        val res = Vector2d(x * factor, y * factor)
        wrapper.long0 = res.x.toRawBits()
        wrapper.long1 = res.y.toRawBits()
    }

    fun plus(vector: Vector2d, wrapper: MutableMfvcWrapper) {
        val res = Vector2d(x + vector.x, y + vector.y)
        wrapper.long0 = res.x.toRawBits()
        wrapper.long1 = res.y.toRawBits()
    }

    fun minus(vector: Vector2d, wrapper: MutableMfvcWrapper) {
        val res = Vector2d(x - vector.x, y - vector.y)
        wrapper.long0 = res.x.toRawBits()
        wrapper.long1 = res.y.toRawBits()
    }

    fun abs(wrapper: MutableMfvcWrapper) {
        val res = Vector2d(Math.abs(x), Math.abs(y))
        wrapper.long0 = res.x.toRawBits()
        wrapper.long1 = res.y.toRawBits()
    }

    fun xProjection(wrapper: MutableMfvcWrapper) {
        val res = Vector2d(x, 0.0)
        wrapper.long0 = res.x.toRawBits()
        wrapper.long1 = res.y.toRawBits()
    }

    fun yProjection(wrapper: MutableMfvcWrapper) {
        val res = Vector2d(0.0, y)
        wrapper.long0 = res.x.toRawBits()
        wrapper.long1 = res.y.toRawBits()
    }
}

@JvmInline
value class LocationD(val x: Int, val y: Int) {
    fun toVector(wrapper: MutableMfvcWrapper) {
        val res = Vector2d(x.toDouble(), y.toDouble())
        wrapper.long0 = res.x.toRawBits()
        wrapper.long1 = res.y.toRawBits()
    }

    fun step(vector: Vector2d, wrapper: MutableMfvcWrapper) {
        toVector(wrapper)
        Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1)).plus(vector, wrapper)
        toVector(wrapper)
        Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1)).plus(vector, wrapper)
        val res = LocationD(
     (Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))).x.toInt(),
     (Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))).y.toInt()
        )
        wrapper.long0 = res.x.toLong()
        wrapper.long1 = res.y.toLong()
    }
}

fun Double.div(vector: Vector2d, wrapper: MutableMfvcWrapper) {
    val res = Vector2d(this / vector.x, this / vector.y)
    wrapper.long0 = res.x.toRawBits()
    wrapper.long1 = res.y.toRawBits()
}

class MyPanelD : JPanel(), KeyListener, MouseListener {
    private var startTime: Long = System.nanoTime()
    private var frameCount = 0
    private var fps = 100.0
    private var minFps = 100.0
    private val veryStartTime = System.nanoTime()

    var pos = Point2d(22.0, 12.0) // start position

    var dir = Vector2d(-1.0, 0.0) // direction vector

    var plane = Vector2d(0.0, 0.66) //the 2d raycaster version of camera plane

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

        g as Graphics2D

        mainLoop(g.asAbstract())

        g.color = Color.BLACK
        g.drawString(
            "Current FPS: ${DecimalFormat("#.#").format(fps)}, Min FPS: ${DecimalFormat("#.#").format(minFps)}",
            10,
            20
        )

        frameCount++
    }

    private fun mainLoop(g: AbstractGraphics) {
        repeat(100) {
            drawScene(pos, dir, plane, g)
        }
    }

    override fun keyPressed(e: KeyEvent) {
        val frameTime = 1 / fps
        //speed modifiers

        //speed modifiers
        val moveSpeed = frameTime * .5 //the constant value is in squares/second
        val rotSpeed = frameTime * .3 //the constant value is in radians/second
        val wrapper = MutableMfvcWrapper()
        when (e.keyCode) {
            KeyEvent.VK_UP -> {
                dir.times(moveSpeed, wrapper)
                (pos.plus((Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))), wrapper))
                if (canMove(
                        Point2d(
                            Double.fromBits(
                                wrapper
                                    .long0
                            ), Double.fromBits(wrapper.long1)
                        )
                    )
                ) {
                    pos = run {
                        dir.times(moveSpeed, wrapper)
                        (Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))).xProjection(wrapper)
                        (pos.plus(
                            Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1)), wrapper
 ))
                        Point2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))
                    }
                }
                dir.times(moveSpeed, wrapper)
                (Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))).yProjection(wrapper)
                (pos.plus(
                    Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1)), wrapper
 ))
                if (canMove(
         Point2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))
     )
                ) {
                    pos = run {
                        dir.times(moveSpeed, wrapper)
                        (Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))).yProjection(wrapper)
                        pos.plus(
                            Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1)), wrapper
 )
                        Point2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))
                    }
                }
            }

            KeyEvent.VK_DOWN -> {
                dir.times(moveSpeed, wrapper)
                (Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))).xProjection(wrapper)
                pos.minus(
                    Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1)), wrapper
 )
                if (canMove(
         Point2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))
     )
                ) {
                    pos = run {
                        dir.times(moveSpeed, wrapper)
                        (Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))).xProjection(wrapper)
                        pos.minus(
                            Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1)), wrapper
 )
                        Point2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))
                    }
                }
                dir.times(moveSpeed, wrapper)
                (Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))).yProjection(wrapper)
                pos.minus(
                    Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1)), wrapper
 )
                if (canMove(
         Point2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))
     )
                ) {
                    pos = run {
                        dir.times(moveSpeed, wrapper)
                        (Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))).yProjection(wrapper)
                        pos.minus(
                            Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1)), wrapper
 )
                        Point2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))
                    }
                }
            }

            KeyEvent.VK_LEFT -> {
                //both camera direction and camera plane must be rotated
                dir = run {
                    dir.rotate(rotSpeed, wrapper)
                    Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))
                }
                plane = run {
                    plane.rotate(rotSpeed, wrapper)
                    Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))
                }
            }

            KeyEvent.VK_RIGHT -> {
                //both camera direction and camera plane must be rotated
                dir = run {
                    dir.rotate(-rotSpeed, wrapper)
                    Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))
                }
                plane = run {
                    plane.rotate(-rotSpeed, wrapper)
                    Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))
                }
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

fun heavyActionD(graphics: AbstractGraphics) =
    drawScene(Point2d(22.0, 12.0), Vector2d(-1.0, 0.0), Vector2d(0.0, 0.66), graphics)

private fun drawScene(pos: Point2d, dir: Vector2d, plane: Vector2d, g: AbstractGraphics) {
    val wrapper = MutableMfvcWrapper()
    for (x in 0 until screenWidth) {
        //calculate ray position and direction
        val cameraX = 2 * x / screenHeight.toDouble() - 1 //x-coordinate in camera space
        plane.times(cameraX, wrapper)
        dir.plus(Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1)), wrapper)
        val rayDir =
            Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))
        //which box of the map we're in
        pos.toLocation(wrapper)
        var mapLocation = LocationD(wrapper.long0.toInt(), wrapper.long1.toInt())

        //length of ray from current position to next x or y-side
        var sideDist: Vector2d

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
        rayDir.abs(wrapper)
        1.0.div(Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1)), wrapper)
        val deltaDist = Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))

        var perpWallDist: Double

        //what direction to step in x or y-direction (either +1 or -1)
        var step: Vector2d

        var hit = 0 //was there a wall hit?
        var side = 0 //was a NS or a EW wall hit?
        //calculate step and initial sideDist
        if (rayDir.x < 0) {
            step = Vector2d((-1).toDouble(), 0.0)
            sideDist = run {
                pos.toVector(wrapper)
                mapLocation.toVector(wrapper)
                Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))
                    .minus(Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1)), wrapper)
                (Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))).xProjection(wrapper)
                Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))
                    .times(deltaDist.x, wrapper)
                Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))
            }
        } else {
            step = Vector2d(1.toDouble(), 0.0)
            sideDist = run {
                mapLocation.toVector(wrapper)
                Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1)).plus(
                    Vector2d(1.0, 0.0),
                    wrapper
                )
                pos.toVector(wrapper)
                Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))
                    .minus(Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1)), wrapper)
                (Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))).xProjection(
                    wrapper
                )
                Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))
                    .times(deltaDist.x, wrapper)
                Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))
            }
        }
        if (rayDir.y < 0) {
            step = run {
                step.plus(Vector2d(0.0, (-1).toDouble()), wrapper)
                Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))
            }
            sideDist = run {
                pos.toVector(wrapper)
                mapLocation.toVector(wrapper)
                Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))
                    .minus(Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1)), wrapper)
                (Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))).yProjection(wrapper)
                Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))
                    .times(deltaDist.y, wrapper)
                sideDist.plus(
                    Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1)), wrapper
                )
                Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))
            }
        } else {
            step = run {
                step.plus(Vector2d(0.0, 1.toDouble()), wrapper)
                Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))
            }
            sideDist = run {
                mapLocation.toVector(wrapper)
                Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1)).plus(
                    Vector2d(0.0, 1.0),
                    wrapper
                )
                pos.toVector(wrapper)
                Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))
                    .minus(Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1)), wrapper)
                (Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))).yProjection(wrapper)
                Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))
                    .times(deltaDist.y, wrapper)
                sideDist.plus(
                    Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1)), wrapper
                )
                Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))
            }
        }
        //perform DDA
        while (hit == 0) {
            //jump to next map square, either in x-direction, or in y-direction
            if (sideDist.x < sideDist.y) {
                sideDist = run {
                    deltaDist.xProjection(wrapper)
                    sideDist.plus(Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1)), wrapper)
                    Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))
                }
                mapLocation = run {
                    step.xProjection(wrapper)
                    mapLocation.step(Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1)), wrapper)
                    LocationD(wrapper.long0.toInt(), wrapper.long1.toInt())
                }
                side = 0
            } else {
                sideDist = run {
                    deltaDist.yProjection(wrapper)
                    sideDist.plus(Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1)), wrapper)
                    Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))
                }
                mapLocation = run {
                    step.yProjection(wrapper)
                    mapLocation.step(Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1)), wrapper)
                    LocationD(wrapper.long0.toInt(), wrapper.long1.toInt())
                }
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
            if (side == 0) {
                sideDist.minus(deltaDist, wrapper)
                (Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))).x
            } else {
                sideDist.minus(deltaDist, wrapper)
                (Vector2d(Double.fromBits(wrapper.long0), Double.fromBits(wrapper.long1))).y
            }

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
        g.setIntColor(color)
        g.drawLine(x, drawStart, x, drawEnd)
    }
}

class MyFrameD : JFrame() {
    init {
        add(MyPanelD())
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
    MyFrameD()
}
