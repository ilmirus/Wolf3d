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

inline operator fun Array<IntArray>.get(location: LocationF): Int = this[location.x][location.y]

inline fun canMove(point: Point2f): Boolean = worldMap[point.x.toInt()][point.y.toInt()] == 0

@JvmInline
value class Point2f(val x: Float, val y: Float) {
    fun plus(vector: Vector2f, wrapper: Wrapper) = wrapper.encode(x + vector.x, y + vector.y)

    fun minus(vector: Vector2f, wrapper: Wrapper) = wrapper.encode(x - vector.x, y - vector.y)

    fun toLocation(wrapper: LocationF.Wrapper) = wrapper.encode(x.toInt(), y.toInt())

    fun toVector(wrapper: Vector2f.Wrapper) = wrapper.encode(x, y)

    class Wrapper {
        @JvmField
        var x: Float = 0.0f
        @JvmField
        var y: Float = 0.0f

        inline fun encode(x: Float, y: Float) {
            this.x = x
            this.y = y
        }

        inline fun decode() = Point2f(x, y)
    }
}

@JvmInline
value class Vector2f(val x: Float, val y: Float) {
    fun rotate(angle: Float, wrapper: Wrapper) =
        wrapper.encode(x * cos(angle) - y * sin(angle), x * sin(angle) + y * cos(angle))

    fun times(factor: Float, wrapper: Wrapper) = wrapper.encode(x * factor, y * factor)

    fun plus(vector: Vector2f, wrapper: Wrapper) = wrapper.encode(x + vector.x, y + vector.y)

    fun minus(vector: Vector2f, wrapper: Wrapper) = wrapper.encode(x - vector.x, y - vector.y)

    fun abs(wrapper: Wrapper) = wrapper.encode(abs(x), abs(y))

    fun xProjection(wrapper: Wrapper) = wrapper.encode(x, 0.0f)

    fun yProjection(wrapper: Wrapper) = wrapper.encode(0.0f, y)

    class Wrapper {
        @JvmField
        var x: Float = 0.0f
        @JvmField
        var y: Float = 0.0f

        inline fun encode(x: Float, y: Float) {
            this.x = x
            this.y = y
        }

        inline fun decode() = Vector2f(x, y)
    }
}

@Suppress("UnusedReceiverParameter")
inline fun Unit.decodePoint2f(wrapper: Point2f.Wrapper) = wrapper.decode()

@Suppress("UnusedReceiverParameter")
inline fun Unit.decodeVector2f(wrapper: Vector2f.Wrapper) = wrapper.decode()

@Suppress("UnusedReceiverParameter")
inline fun Unit.decodeLocationF(wrapper: LocationF.Wrapper) = wrapper.decode()

@JvmInline
value class LocationF(val x: Int, val y: Int) {
    fun toVector(wrapper: Vector2f.Wrapper) = wrapper.encode(x.toFloat(), y.toFloat())

    fun step(vector: Vector2f, wrapper: Wrapper) {
        val vectorWrapper = Vector2f.Wrapper()
        wrapper.encode(
            (toVector(vectorWrapper).decodeVector2f(vectorWrapper).plus(vector, vectorWrapper)
                .decodeVector2f(vectorWrapper)).x.toInt(),
            (toVector(vectorWrapper).decodeVector2f(vectorWrapper).plus(vector, vectorWrapper)
                .decodeVector2f(vectorWrapper)).y.toInt()
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

        inline fun decode() = LocationF(x, y)
    }
}

fun Float.div(vector: Vector2f, wrapper: Vector2f.Wrapper) {
    wrapper.encode(this / vector.x, this / vector.y)
}

class MyPanelF : JPanel(), KeyListener, MouseListener {
    private var startTime: Long = System.nanoTime()
    private var frameCount = 0
    private var fps = 100.0
    private var minFps = 100.0
    private val veryStartTime = System.nanoTime()

    var pos = Point2f(22.0f, 12.0f) // start position

    var dir = Vector2f(-1.0f, 0.0f) // direction vector

    var plane = Vector2f(0.0f, 0.66f) //the 2d raycaster version of camera plane

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
        val frameTime = 1 / fps.toFloat()
        //speed modifiers

        //speed modifiers
        val moveSpeed = frameTime * .5f //the constant value is in squares/second
        val rotSpeed = frameTime * .3f //the constant value is in radians/second
        val wrapper = Vector2f.Wrapper()
        val pointWrapper = Point2f.Wrapper()
        when (e.keyCode) {
            KeyEvent.VK_UP -> {
                if (canMove(
                        (pos.plus(
                            (dir.times(moveSpeed, wrapper).decodeVector2f(wrapper)),
                            pointWrapper
                        )).decodePoint2f(pointWrapper)
                    )
                ) {
                    pos = (pos.plus(
                        (dir.times(moveSpeed, wrapper).decodeVector2f(wrapper)).xProjection(wrapper)
                            .decodeVector2f(wrapper), pointWrapper
                    )).decodePoint2f(pointWrapper)
                }
                if (canMove(
                        (pos.plus(
                            (dir.times(moveSpeed, wrapper).decodeVector2f(wrapper)).yProjection(wrapper)
                                .decodeVector2f(wrapper), pointWrapper
                        )).decodePoint2f(pointWrapper)
                    )
                ) {
                    pos = pos.plus(
                        (dir.times(moveSpeed, wrapper).decodeVector2f(wrapper)).yProjection(wrapper)
                            .decodeVector2f(wrapper), pointWrapper
                    ).decodePoint2f(pointWrapper)
                }
            }

            KeyEvent.VK_DOWN -> {
                if (canMove(
                        pos.minus(
                            (dir.times(moveSpeed, wrapper).decodeVector2f(wrapper)).xProjection(wrapper)
                                .decodeVector2f(wrapper), pointWrapper
                        ).decodePoint2f(pointWrapper)
                    )
                ) {
                    pos = pos.minus(
                        (dir.times(moveSpeed, wrapper).decodeVector2f(wrapper)).xProjection(wrapper)
                            .decodeVector2f(wrapper), pointWrapper
                    ).decodePoint2f(pointWrapper)
                }
                if (canMove(
                        pos.minus(
                            (dir.times(moveSpeed, wrapper).decodeVector2f(wrapper)).yProjection(wrapper)
                                .decodeVector2f(wrapper), pointWrapper
                        ).decodePoint2f(pointWrapper)
                    )
                ) {
                    pos = pos.minus(
                        (dir.times(moveSpeed, wrapper).decodeVector2f(wrapper)).yProjection(wrapper)
                            .decodeVector2f(wrapper), pointWrapper
                    ).decodePoint2f(pointWrapper)
                }
            }

            KeyEvent.VK_LEFT -> {
                //both camera direction and camera plane must be rotated
                dir = dir.rotate(rotSpeed, wrapper).decodeVector2f(wrapper)
                plane = plane.rotate(rotSpeed, wrapper).decodeVector2f(wrapper)
            }

            KeyEvent.VK_RIGHT -> {
                //both camera direction and camera plane must be rotated
                dir = dir.rotate(-rotSpeed, wrapper).decodeVector2f(wrapper)
                plane = plane.rotate(-rotSpeed, wrapper).decodeVector2f(wrapper)
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

fun heavyActionFloat(graphics: AbstractGraphics) {
    var pos = Point2f(22.0f, 12.0f)
    var dir = Vector2f(-1.0f, 0.0f)
    var plane = Vector2f(0.0f, 0.66f)
    val fps = 10.0f
    val frameTime = 1 / fps
    //speed modifiers

    val wrapper = Vector2f.Wrapper()
    val pointWrapper = Point2f.Wrapper()

    //speed modifiers
    val moveSpeed = frameTime * .5f //the constant value is in squares/second
    val rotSpeed = frameTime * .3f //the constant value is in radians/second
    repeat(MicrobenchmarkRotations) {
        drawScene(pos, dir, plane, graphics)
        if (canMove(
                (pos.plus((dir.times(moveSpeed, wrapper).decodeVector2f(wrapper)), pointWrapper)).decodePoint2f(
                    pointWrapper
                )
            )
        ) {
            pos = (pos.plus(
                (dir.times(moveSpeed, wrapper).decodeVector2f(wrapper)).xProjection(wrapper).decodeVector2f(wrapper),
                pointWrapper
            )).decodePoint2f(pointWrapper)
        }
        if (canMove(
                (pos.plus(
                    (dir.times(moveSpeed, wrapper).decodeVector2f(wrapper)).yProjection(wrapper)
                        .decodeVector2f(wrapper), pointWrapper
                )).decodePoint2f(pointWrapper)
            )
        ) {
            pos = pos.plus(
                (dir.times(moveSpeed, wrapper).decodeVector2f(wrapper)).yProjection(wrapper).decodeVector2f(wrapper),
                pointWrapper
            ).decodePoint2f(pointWrapper)
        }
        drawScene(pos, dir, plane, graphics)
        if (canMove(
                pos.minus(
                    (dir.times(moveSpeed, wrapper).decodeVector2f(wrapper)).xProjection(wrapper)
                        .decodeVector2f(wrapper), pointWrapper
                ).decodePoint2f(pointWrapper)
            )
        ) {
            pos = pos.minus(
                (dir.times(moveSpeed, wrapper).decodeVector2f(wrapper)).xProjection(wrapper).decodeVector2f(wrapper),
                pointWrapper
            ).decodePoint2f(pointWrapper)
        }
        if (canMove(
                pos.minus(
                    (dir.times(moveSpeed, wrapper).decodeVector2f(wrapper)).yProjection(wrapper)
                        .decodeVector2f(wrapper), pointWrapper
                ).decodePoint2f(pointWrapper)
            )
        ) {
            pos = pos.minus(
                (dir.times(moveSpeed, wrapper).decodeVector2f(wrapper)).yProjection(wrapper).decodeVector2f(wrapper),
                pointWrapper
            ).decodePoint2f(pointWrapper)
        }
        drawScene(pos, dir, plane, graphics)
        dir = dir.rotate(rotSpeed, wrapper).decodeVector2f(wrapper)
        plane = plane.rotate(rotSpeed, wrapper).decodeVector2f(wrapper)
        drawScene(pos, dir, plane, graphics)
    }
}

private fun drawScene(pos: Point2f, dir: Vector2f, plane: Vector2f, g: AbstractGraphics) {
    val vectorWrapper = Vector2f.Wrapper()
    val locationWrapper = LocationF.Wrapper()
    for (x in 0 until screenWidth) {
        //calculate ray position and direction
        val cameraX = 2 * x / screenHeight.toFloat() - 1 //x-coordinate in camera space
        val rayDir =
            dir.plus(plane.times(cameraX, vectorWrapper).decodeVector2f(vectorWrapper), vectorWrapper)
                .decodeVector2f(vectorWrapper)
        //which box of the map we're in
        var mapLocation = pos.toLocation(locationWrapper).decodeLocationF(locationWrapper)

        //length of ray from current position to next x or y-side
        var sideDist: Vector2f

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
        val deltaDist = 1.0f.div(rayDir.abs(vectorWrapper).decodeVector2f(vectorWrapper), vectorWrapper)
            .decodeVector2f(vectorWrapper)

        var perpWallDist: Float

        //what direction to step in x or y-direction (either +1 or -1)
        var step: Vector2f

        var hit = 0 //was there a wall hit?
        var side = 0 //was a NS or a EW wall hit?
        //calculate step and initial sideDist
        if (rayDir.x < 0) {
            step = Vector2f((-1).toFloat(), 0.0f)
            sideDist = (pos.toVector(vectorWrapper).decodeVector2f(vectorWrapper)
                .minus(mapLocation.toVector(vectorWrapper).decodeVector2f(vectorWrapper), vectorWrapper)
                .decodeVector2f(vectorWrapper)).xProjection(vectorWrapper).decodeVector2f(vectorWrapper)
                .times(deltaDist.x, vectorWrapper).decodeVector2f(vectorWrapper)
        } else {
            step = Vector2f(1.toFloat(), 0.0f)
            sideDist = (mapLocation.toVector(vectorWrapper).decodeVector2f(vectorWrapper)
                .plus(Vector2f(1.0f, 0.0f), vectorWrapper)
                .decodeVector2f(vectorWrapper)
                .minus(pos.toVector(vectorWrapper).decodeVector2f(vectorWrapper), vectorWrapper)
                .decodeVector2f(vectorWrapper)).xProjection(
                    vectorWrapper
                ).decodeVector2f(vectorWrapper)
                .times(deltaDist.x, vectorWrapper).decodeVector2f(vectorWrapper)
        }
        if (rayDir.y < 0) {
            step = step.plus(Vector2f(0.0f, (-1).toFloat()), vectorWrapper).decodeVector2f(vectorWrapper)
            sideDist = sideDist.plus(
                (pos.toVector(vectorWrapper).decodeVector2f(vectorWrapper)
                    .minus(mapLocation.toVector(vectorWrapper).decodeVector2f(vectorWrapper), vectorWrapper)
                    .decodeVector2f(vectorWrapper)).yProjection(vectorWrapper).decodeVector2f(vectorWrapper)
                    .times(deltaDist.y, vectorWrapper).decodeVector2f(vectorWrapper), vectorWrapper
            ).decodeVector2f(vectorWrapper)
        } else {
            step = step.plus(Vector2f(0.0f, 1.toFloat()), vectorWrapper).decodeVector2f(vectorWrapper)
            sideDist = sideDist.plus(
                (mapLocation.toVector(vectorWrapper).decodeVector2f(vectorWrapper)
                    .plus(Vector2f(0.0f, 1.0f), vectorWrapper)
                    .decodeVector2f(vectorWrapper)
                    .minus(pos.toVector(vectorWrapper).decodeVector2f(vectorWrapper), vectorWrapper)
                    .decodeVector2f(vectorWrapper)).yProjection(vectorWrapper).decodeVector2f(vectorWrapper)
                    .times(deltaDist.y, vectorWrapper).decodeVector2f(vectorWrapper), vectorWrapper
            ).decodeVector2f(vectorWrapper)
        }
        //perform DDA
        while (hit == 0) {
            //jump to next map square, either in x-direction, or in y-direction
            if (sideDist.x < sideDist.y) {
                sideDist =
                    sideDist.plus(deltaDist.xProjection(vectorWrapper).decodeVector2f(vectorWrapper), vectorWrapper)
                        .decodeVector2f(vectorWrapper)
                mapLocation =
                    mapLocation.step(step.xProjection(vectorWrapper).decodeVector2f(vectorWrapper), locationWrapper)
                        .decodeLocationF(locationWrapper)
                side = 0
            } else {
                sideDist =
                    sideDist.plus(deltaDist.yProjection(vectorWrapper).decodeVector2f(vectorWrapper), vectorWrapper)
                        .decodeVector2f(vectorWrapper)
                mapLocation =
                    mapLocation.step(step.yProjection(vectorWrapper).decodeVector2f(vectorWrapper), locationWrapper)
                        .decodeLocationF(locationWrapper)
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
            if (side == 0) (sideDist.minus(deltaDist, vectorWrapper).decodeVector2f(vectorWrapper)).x
            else (sideDist.minus(deltaDist, vectorWrapper).decodeVector2f(vectorWrapper)).y

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

class MyFrameF : JFrame() {
    init {
        add(MyPanelF())
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
    MyFrameF()
}
