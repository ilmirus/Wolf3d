@file:Suppress("NOTHING_TO_INLINE")

package mutable_ref_specific.rendering

import shared.AbstractGraphics
import shared.OffsetInSeconds
import shared.MicrobenchmarkRotations
import shared.asAbstract
import shared.screenHeight
import shared.screenWidth
import shared.worldMap
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.event.*
import java.text.DecimalFormat
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.Timer
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

inline operator fun Array<IntArray>.get(location: LocationD): Int = this[location.x][location.y]

inline fun canMove(point: Point2d): Boolean = worldMap[point.x.toInt()][point.y.toInt()] == 0

@JvmInline
value class Point2d(val x: Double, val y: Double) {
    fun plus(vector: Vector2d, wrapper: Wrapper) = wrapper.encode(x + vector.x, y + vector.y)

    fun minus(vector: Vector2d, wrapper: Wrapper) = wrapper.encode(x - vector.x, y - vector.y)

    fun toLocation(wrapper: LocationD.Wrapper) = wrapper.encode(x.toInt(), y.toInt())

    fun toVector(wrapper: Vector2d.Wrapper) = wrapper.encode(x, y)

    class Wrapper {
        @JvmField
        var x: Double = 0.0
        @JvmField
        var y: Double = 0.0

        inline fun encode(x: Double, y: Double) {
            this.x = x
            this.y = y
        }

        inline fun decode() = Point2d(x, y)
    }
}

@JvmInline
value class Vector2d(val x: Double, val y: Double) {
    fun rotate(angle: Double, wrapper: Wrapper) =
        wrapper.encode(x * cos(angle) - y * sin(angle), x * sin(angle) + y * cos(angle))

    fun times(factor: Double, wrapper: Wrapper) = wrapper.encode(x * factor, y * factor)

    fun plus(vector: Vector2d, wrapper: Wrapper) = wrapper.encode(x + vector.x, y + vector.y)

    fun minus(vector: Vector2d, wrapper: Wrapper) = wrapper.encode(x - vector.x, y - vector.y)

    fun abs(wrapper: Wrapper) = wrapper.encode(abs(x), abs(y))

    fun xProjection(wrapper: Wrapper) = wrapper.encode(x, 0.0)

    fun yProjection(wrapper: Wrapper) = wrapper.encode(0.0, y)

    class Wrapper {
        @JvmField
        var x: Double = 0.0
        @JvmField
        var y: Double = 0.0

        inline fun encode(x: Double, y: Double) {
            this.x = x
            this.y = y
        }

        inline fun decode() = Vector2d(x, y)
    }
}

@Suppress("UnusedReceiverParameter")
inline fun Unit.decodePoint2d(wrapper: Point2d.Wrapper) = wrapper.decode()

@Suppress("UnusedReceiverParameter")
inline fun Unit.decodeVector2d(wrapper: Vector2d.Wrapper) = wrapper.decode()

@Suppress("UnusedReceiverParameter")
inline fun Unit.decodeLocationD(wrapper: LocationD.Wrapper) = wrapper.decode()

@JvmInline
value class LocationD(val x: Int, val y: Int) {
    fun toVector(wrapper: Vector2d.Wrapper) = wrapper.encode(x.toDouble(), y.toDouble())

    fun step(vector: Vector2d, wrapper: Wrapper) {
        val vectorWrapper = Vector2d.Wrapper()
        wrapper.encode(
            (toVector(vectorWrapper).decodeVector2d(vectorWrapper).plus(vector, vectorWrapper)
                .decodeVector2d(vectorWrapper)).x.toInt(),
            (toVector(vectorWrapper).decodeVector2d(vectorWrapper).plus(vector, vectorWrapper)
                .decodeVector2d(vectorWrapper)).y.toInt()
        )
    }

    class Wrapper {
        @JvmField
        var x: Int = 0
        @JvmField
        var y: Int = 0

        inline fun encode(x: Int, y: Int) {
            this.x = x
            this.y = y
        }

        inline fun decode() = LocationD(x, y)
    }
}

fun Double.div(vector: Vector2d, wrapper: Vector2d.Wrapper) {
    wrapper.encode(this / vector.x, this / vector.y)
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
        val wrapper = Vector2d.Wrapper()
        val pointWrapper = Point2d.Wrapper()
        when (e.keyCode) {
            KeyEvent.VK_UP -> {
                if (canMove(
                        (pos.plus(
                            (dir.times(moveSpeed, wrapper).decodeVector2d(wrapper)),
                            pointWrapper
                        )).decodePoint2d(pointWrapper)
                    )
                ) {
                    pos = (pos.plus(
                        (dir.times(moveSpeed, wrapper).decodeVector2d(wrapper)).xProjection(wrapper)
                            .decodeVector2d(wrapper), pointWrapper
                    )).decodePoint2d(pointWrapper)
                }
                if (canMove(
                        (pos.plus(
                            (dir.times(moveSpeed, wrapper).decodeVector2d(wrapper)).yProjection(wrapper)
                                .decodeVector2d(wrapper), pointWrapper
                        )).decodePoint2d(pointWrapper)
                    )
                ) {
                    pos = pos.plus(
                        (dir.times(moveSpeed, wrapper).decodeVector2d(wrapper)).yProjection(wrapper)
                            .decodeVector2d(wrapper), pointWrapper
                    ).decodePoint2d(pointWrapper)
                }
            }

            KeyEvent.VK_DOWN -> {
                if (canMove(
                        pos.minus(
                            (dir.times(moveSpeed, wrapper).decodeVector2d(wrapper)).xProjection(wrapper)
                                .decodeVector2d(wrapper), pointWrapper
                        ).decodePoint2d(pointWrapper)
                    )
                ) {
                    pos = pos.minus(
                        (dir.times(moveSpeed, wrapper).decodeVector2d(wrapper)).xProjection(wrapper)
                            .decodeVector2d(wrapper), pointWrapper
                    ).decodePoint2d(pointWrapper)
                }
                if (canMove(
                        pos.minus(
                            (dir.times(moveSpeed, wrapper).decodeVector2d(wrapper)).yProjection(wrapper)
                                .decodeVector2d(wrapper), pointWrapper
                        ).decodePoint2d(pointWrapper)
                    )
                ) {
                    pos = pos.minus(
                        (dir.times(moveSpeed, wrapper).decodeVector2d(wrapper)).yProjection(wrapper)
                            .decodeVector2d(wrapper), pointWrapper
                    ).decodePoint2d(pointWrapper)
                }
            }

            KeyEvent.VK_LEFT -> {
                //both camera direction and camera plane must be rotated
                dir = dir.rotate(rotSpeed, wrapper).decodeVector2d(wrapper)
                plane = plane.rotate(rotSpeed, wrapper).decodeVector2d(wrapper)
            }

            KeyEvent.VK_RIGHT -> {
                //both camera direction and camera plane must be rotated
                dir = dir.rotate(-rotSpeed, wrapper).decodeVector2d(wrapper)
                plane = plane.rotate(-rotSpeed, wrapper).decodeVector2d(wrapper)
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

fun heavyActionDouble(graphics: AbstractGraphics) {
    var pos = Point2d(22.0, 12.0)
    var dir = Vector2d(-1.0, 0.0)
    var plane = Vector2d(0.0, 0.66)
    val fps = 10.0
    val frameTime = 1 / fps
    //speed modifiers

    val wrapper = Vector2d.Wrapper()
    val pointWrapper = Point2d.Wrapper()

    //speed modifiers
    val moveSpeed = frameTime * .5 //the constant value is in squares/second
    val rotSpeed = frameTime * .3 //the constant value is in radians/second
    repeat(MicrobenchmarkRotations) {
        drawScene(pos, dir, plane, graphics)
        if (canMove(
                (pos.plus((dir.times(moveSpeed, wrapper).decodeVector2d(wrapper)), pointWrapper)).decodePoint2d(
                    pointWrapper
                )
            )
        ) {
            pos = (pos.plus(
                (dir.times(moveSpeed, wrapper).decodeVector2d(wrapper)).xProjection(wrapper).decodeVector2d(wrapper),
                pointWrapper
            )).decodePoint2d(pointWrapper)
        }
        if (canMove(
                (pos.plus(
                    (dir.times(moveSpeed, wrapper).decodeVector2d(wrapper)).yProjection(wrapper)
                        .decodeVector2d(wrapper), pointWrapper
                )).decodePoint2d(pointWrapper)
            )
        ) {
            pos = pos.plus(
                (dir.times(moveSpeed, wrapper).decodeVector2d(wrapper)).yProjection(wrapper).decodeVector2d(wrapper),
                pointWrapper
            ).decodePoint2d(pointWrapper)
        }
        drawScene(pos, dir, plane, graphics)
        if (canMove(
                pos.minus(
                    (dir.times(moveSpeed, wrapper).decodeVector2d(wrapper)).xProjection(wrapper)
                        .decodeVector2d(wrapper), pointWrapper
                ).decodePoint2d(pointWrapper)
            )
        ) {
            pos = pos.minus(
                (dir.times(moveSpeed, wrapper).decodeVector2d(wrapper)).xProjection(wrapper).decodeVector2d(wrapper),
                pointWrapper
            ).decodePoint2d(pointWrapper)
        }
        if (canMove(
                pos.minus(
                    (dir.times(moveSpeed, wrapper).decodeVector2d(wrapper)).yProjection(wrapper)
                        .decodeVector2d(wrapper), pointWrapper
                ).decodePoint2d(pointWrapper)
            )
        ) {
            pos = pos.minus(
                (dir.times(moveSpeed, wrapper).decodeVector2d(wrapper)).yProjection(wrapper).decodeVector2d(wrapper),
                pointWrapper
            ).decodePoint2d(pointWrapper)
        }
        drawScene(pos, dir, plane, graphics)
        dir = dir.rotate(rotSpeed, wrapper).decodeVector2d(wrapper)
        plane = plane.rotate(rotSpeed, wrapper).decodeVector2d(wrapper)
        drawScene(pos, dir, plane, graphics)
    }
}

private fun drawScene(pos: Point2d, dir: Vector2d, plane: Vector2d, g: AbstractGraphics) {
    val vectorWrapper = Vector2d.Wrapper()
    val locationWrapper = LocationD.Wrapper()
    for (x in 0 until screenWidth) {
        //calculate ray position and direction
        val cameraX = 2 * x / screenHeight.toDouble() - 1 //x-coordinate in camera space
        val rayDir =
            dir.plus(plane.times(cameraX, vectorWrapper).decodeVector2d(vectorWrapper), vectorWrapper)
                .decodeVector2d(vectorWrapper)
        //which box of the map we're in
        var mapLocation = pos.toLocation(locationWrapper).decodeLocationD(locationWrapper)

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
        val deltaDist = 1.0.div(rayDir.abs(vectorWrapper).decodeVector2d(vectorWrapper), vectorWrapper)
            .decodeVector2d(vectorWrapper)

        var perpWallDist: Double

        //what direction to step in x or y-direction (either +1 or -1)
        var step: Vector2d

        var hit = 0 //was there a wall hit?
        var side = 0 //was a NS or a EW wall hit?
        //calculate step and initial sideDist
        if (rayDir.x < 0) {
            step = Vector2d((-1).toDouble(), 0.0)
            sideDist = (pos.toVector(vectorWrapper).decodeVector2d(vectorWrapper)
                .minus(mapLocation.toVector(vectorWrapper).decodeVector2d(vectorWrapper), vectorWrapper)
                .decodeVector2d(vectorWrapper)).xProjection(vectorWrapper).decodeVector2d(vectorWrapper)
                .times(deltaDist.x, vectorWrapper).decodeVector2d(vectorWrapper)
        } else {
            step = Vector2d(1.toDouble(), 0.0)
            sideDist = (mapLocation.toVector(vectorWrapper).decodeVector2d(vectorWrapper)
                .plus(Vector2d(1.0, 0.0), vectorWrapper)
                .decodeVector2d(vectorWrapper)
                .minus(pos.toVector(vectorWrapper).decodeVector2d(vectorWrapper), vectorWrapper)
                .decodeVector2d(vectorWrapper)).xProjection(
                    vectorWrapper
                ).decodeVector2d(vectorWrapper)
                .times(deltaDist.x, vectorWrapper).decodeVector2d(vectorWrapper)
        }
        if (rayDir.y < 0) {
            step = step.plus(Vector2d(0.0, (-1).toDouble()), vectorWrapper).decodeVector2d(vectorWrapper)
            sideDist = sideDist.plus(
                (pos.toVector(vectorWrapper).decodeVector2d(vectorWrapper)
                    .minus(mapLocation.toVector(vectorWrapper).decodeVector2d(vectorWrapper), vectorWrapper)
                    .decodeVector2d(vectorWrapper)).yProjection(vectorWrapper).decodeVector2d(vectorWrapper)
                    .times(deltaDist.y, vectorWrapper).decodeVector2d(vectorWrapper), vectorWrapper
            ).decodeVector2d(vectorWrapper)
        } else {
            step = step.plus(Vector2d(0.0, 1.toDouble()), vectorWrapper).decodeVector2d(vectorWrapper)
            sideDist = sideDist.plus(
                (mapLocation.toVector(vectorWrapper).decodeVector2d(vectorWrapper)
                    .plus(Vector2d(0.0, 1.0), vectorWrapper)
                    .decodeVector2d(vectorWrapper)
                    .minus(pos.toVector(vectorWrapper).decodeVector2d(vectorWrapper), vectorWrapper)
                    .decodeVector2d(vectorWrapper)).yProjection(vectorWrapper).decodeVector2d(vectorWrapper)
                    .times(deltaDist.y, vectorWrapper).decodeVector2d(vectorWrapper), vectorWrapper
            ).decodeVector2d(vectorWrapper)
        }
        //perform DDA
        while (hit == 0) {
            //jump to next map square, either in x-direction, or in y-direction
            if (sideDist.x < sideDist.y) {
                sideDist =
                    sideDist.plus(deltaDist.xProjection(vectorWrapper).decodeVector2d(vectorWrapper), vectorWrapper)
                        .decodeVector2d(vectorWrapper)
                mapLocation =
                    mapLocation.step(step.xProjection(vectorWrapper).decodeVector2d(vectorWrapper), locationWrapper)
                        .decodeLocationD(locationWrapper)
                side = 0
            } else {
                sideDist =
                    sideDist.plus(deltaDist.yProjection(vectorWrapper).decodeVector2d(vectorWrapper), vectorWrapper)
                        .decodeVector2d(vectorWrapper)
                mapLocation =
                    mapLocation.step(step.yProjection(vectorWrapper).decodeVector2d(vectorWrapper), locationWrapper)
                        .decodeLocationD(locationWrapper)
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
            if (side == 0) (sideDist.minus(deltaDist, vectorWrapper).decodeVector2d(vectorWrapper)).x
            else (sideDist.minus(deltaDist, vectorWrapper).decodeVector2d(vectorWrapper)).y

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
