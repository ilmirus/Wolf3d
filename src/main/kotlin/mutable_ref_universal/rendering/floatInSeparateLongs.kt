@file:Suppress("NOTHING_TO_INLINE")

package mutable_ref_universal.rendering

import shared.AbstractGraphics
import shared.OffsetInSeconds
import shared.MicrobenchmarkRotations
import shared.asAbstract
import mutable_ref_universal.MutableMfvcWrapper
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

inline operator fun Array<IntArray>.get(location: LocationFSeparate): Int = this[location.x][location.y]

inline fun canMove(point: Point2fSeparate): Boolean = worldMap[point.x.toInt()][point.y.toInt()] == 0

@JvmInline
value class Point2fSeparate(val x: Float, val y: Float) {
    fun plus(vector: Vector2fSeparate, wrapper: MutableMfvcWrapper) {
        val result = Point2fSeparate(x + vector.x, y + vector.y)
        wrapper.encodeLong0(result.x)
        wrapper.encodeLong1(result.y)
    }

    fun minus(vector: Vector2fSeparate, wrapper: MutableMfvcWrapper) {
        val result = Point2fSeparate(x - vector.x, y - vector.y)
        wrapper.encodeLong0(result.x)
        wrapper.encodeLong1(result.y)
    }

    fun toLocation(wrapper: MutableMfvcWrapper) {
        val result = LocationFSeparate(x.toInt(), y.toInt())
        wrapper.encodeLong0(result.x)
        wrapper.encodeLong1(result.y)
    }

    fun toVector(wrapper: MutableMfvcWrapper) {
        val result = Vector2fSeparate(x, y)
        wrapper.encodeLong0(result.x)
        wrapper.encodeLong1(result.y)
    }
}

@Suppress("UnusedReceiverParameter")
inline fun Unit.decodePoint2fSeparate(wrapper: MutableMfvcWrapper) = Point2fSeparate(wrapper.decodeFloat0(), wrapper.decodeFloat1())
@Suppress("UnusedReceiverParameter")
inline fun Unit.decodeVector2fSeparate(wrapper: MutableMfvcWrapper) = Vector2fSeparate(wrapper.decodeFloat0(), wrapper.decodeFloat1())
@Suppress("UnusedReceiverParameter")
inline fun Unit.decodeLocationFSeparate(wrapper: MutableMfvcWrapper) = LocationFSeparate(wrapper.decodeInt0(), wrapper.decodeInt1())

@JvmInline
value class Vector2fSeparate(val x: Float, val y: Float) {
    fun rotate(angle: Float, wrapper: MutableMfvcWrapper) {
        val res = Vector2fSeparate(x * cos(angle) - y * sin(angle), x * sin(angle) + y * cos(angle))
        wrapper.encodeLong0(res.x)
        wrapper.encodeLong1(res.y)
    }

    fun times(factor: Float, wrapper: MutableMfvcWrapper) {
        val res = Vector2fSeparate(x * factor, y * factor)
        wrapper.encodeLong0(res.x)
        wrapper.encodeLong1(res.y)
    }

    fun plus(vector: Vector2fSeparate, wrapper: MutableMfvcWrapper) {
        val res = Vector2fSeparate(x + vector.x, y + vector.y)
        wrapper.encodeLong0(res.x)
        wrapper.encodeLong1(res.y)
    }

    fun minus(vector: Vector2fSeparate, wrapper: MutableMfvcWrapper) {
        val res = Vector2fSeparate(x - vector.x, y - vector.y)
        wrapper.encodeLong0(res.x)
        wrapper.encodeLong1(res.y)
    }

    fun abs(wrapper: MutableMfvcWrapper) {
        val res = Vector2fSeparate(abs(x), abs(y))
        wrapper.encodeLong0(res.x)
        wrapper.encodeLong1(res.y)
    }

    fun xProjection(wrapper: MutableMfvcWrapper) {
        val res = Vector2fSeparate(x, 0.0f)
        wrapper.encodeLong0(res.x)
        wrapper.encodeLong1(res.y)
    }

    fun yProjection(wrapper: MutableMfvcWrapper) {
        val res = Vector2fSeparate(0.0f, y)
        wrapper.encodeLong0(res.x)
        wrapper.encodeLong1(res.y)
    }
}

@JvmInline
value class LocationFSeparate(val x: Int, val y: Int) {
    fun toVector(wrapper: MutableMfvcWrapper) {
        val res = Vector2fSeparate(x.toFloat(), y.toFloat())
        wrapper.encodeLong0(res.x)
        wrapper.encodeLong1(res.y)
    }

    fun step(vector: Vector2fSeparate, wrapper: MutableMfvcWrapper) {
        val res = LocationFSeparate(
            (toVector(wrapper).decodeVector2fSeparate(wrapper).plus(vector, wrapper).decodeVector2fSeparate(wrapper)).x.toInt(),
            (toVector(wrapper).decodeVector2fSeparate(wrapper).plus(vector, wrapper).decodeVector2fSeparate(wrapper)).y.toInt()
        )
        wrapper.encodeLong0(res.x)
        wrapper.encodeLong1(res.y)
    }
}

fun Float.div(vector: Vector2fSeparate, wrapper: MutableMfvcWrapper) {
    val res = Vector2fSeparate(this / vector.x, this / vector.y)
    wrapper.encodeLong0(res.x)
    wrapper.encodeLong1(res.y)
}

class MyPanelFSeparate : JPanel(), KeyListener, MouseListener {
    private var startTime: Long = System.nanoTime()
    private var frameCount = 0
    private var fps = 100.0
    private var minFps = 100.0
    private val veryStartTime = System.nanoTime()

    var pos = Point2fSeparate(22.0f, 12.0f) // start position

    var dir = Vector2fSeparate(-1.0f, 0.0f) // direction vector

    var plane = Vector2fSeparate(0.0f, 0.66f) //the 2d raycaster version of camera plane
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
        g.drawString("Current FPS: ${DecimalFormat("#.#").format(fps)}, Min FPS: ${DecimalFormat("#.#").format(minFps)}", 10, 20)

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
        val wrapper = MutableMfvcWrapper()
        when (e.keyCode) {
            KeyEvent.VK_UP -> {
                if(canMove((pos.plus((dir.times(moveSpeed, wrapper).decodeVector2fSeparate(wrapper)), wrapper)).decodePoint2fSeparate(wrapper))) {
                    pos = (pos.plus((dir.times(moveSpeed, wrapper).decodeVector2fSeparate(wrapper)).xProjection(wrapper).decodeVector2fSeparate(wrapper), wrapper)).decodePoint2fSeparate(wrapper)
                }
                if(canMove((pos.plus((dir.times(moveSpeed, wrapper).decodeVector2fSeparate(wrapper)).yProjection(wrapper).decodeVector2fSeparate(wrapper), wrapper)).decodePoint2fSeparate(wrapper))) {
                    pos = pos.plus((dir.times(moveSpeed, wrapper).decodeVector2fSeparate(wrapper)).yProjection(wrapper).decodeVector2fSeparate(wrapper), wrapper).decodePoint2fSeparate(wrapper)
                }
            }

            KeyEvent.VK_DOWN -> {
                if(canMove(pos.minus((dir.times(moveSpeed, wrapper).decodeVector2fSeparate(wrapper)).xProjection(wrapper).decodeVector2fSeparate(wrapper), wrapper).decodePoint2fSeparate(wrapper))) {
                    pos = pos.minus((dir.times(moveSpeed, wrapper).decodeVector2fSeparate(wrapper)).xProjection(wrapper).decodeVector2fSeparate(wrapper), wrapper).decodePoint2fSeparate(wrapper)
                }
                if(canMove(pos.minus((dir.times(moveSpeed, wrapper).decodeVector2fSeparate(wrapper)).yProjection(wrapper).decodeVector2fSeparate(wrapper), wrapper).decodePoint2fSeparate(wrapper))) {
                    pos = pos.minus((dir.times(moveSpeed, wrapper).decodeVector2fSeparate(wrapper)).yProjection(wrapper).decodeVector2fSeparate(wrapper), wrapper).decodePoint2fSeparate(wrapper)
                }
            }

            KeyEvent.VK_LEFT -> {
                //both camera direction and camera plane must be rotated
                dir = dir.rotate(rotSpeed, wrapper).decodeVector2fSeparate(wrapper)
                plane = plane.rotate(rotSpeed, wrapper).decodeVector2fSeparate(wrapper)
            }

            KeyEvent.VK_RIGHT -> {
                //both camera direction and camera plane must be rotated
                dir = dir.rotate(-rotSpeed, wrapper).decodeVector2fSeparate(wrapper)
                plane = plane.rotate(-rotSpeed, wrapper).decodeVector2fSeparate(wrapper)
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

fun heavyActionFloatSeparate(graphics: AbstractGraphics) {
    var pos = Point2fSeparate(22.0f, 12.0f)
    var dir = Vector2fSeparate(-1.0f, 0.0f)
    var plane = Vector2fSeparate(0.0f, 0.66f)
    val fps = 10.0f
    val frameTime = 1 / fps
    //speed modifiers
    val wrapper = MutableMfvcWrapper()
    //speed modifiers
    val moveSpeed = frameTime * .5f //the constant value is in squares/second
    val rotSpeed = frameTime * .3f //the constant value is in radians/second
    repeat(MicrobenchmarkRotations) {
        drawScene(pos, dir, plane, graphics)
        if(canMove((pos.plus((dir.times(moveSpeed, wrapper).decodeVector2fSeparate(wrapper)), wrapper)).decodePoint2fSeparate(wrapper))) {
            pos = (pos.plus((dir.times(moveSpeed, wrapper).decodeVector2fSeparate(wrapper)).xProjection(wrapper).decodeVector2fSeparate(wrapper), wrapper)).decodePoint2fSeparate(wrapper)
        }
        if(canMove((pos.plus((dir.times(moveSpeed, wrapper).decodeVector2fSeparate(wrapper)).yProjection(wrapper).decodeVector2fSeparate(wrapper), wrapper)).decodePoint2fSeparate(wrapper))) {
            pos = pos.plus((dir.times(moveSpeed, wrapper).decodeVector2fSeparate(wrapper)).yProjection(wrapper).decodeVector2fSeparate(wrapper), wrapper).decodePoint2fSeparate(wrapper)
        }
        drawScene(pos, dir, plane, graphics)
        if(canMove(pos.minus((dir.times(moveSpeed, wrapper).decodeVector2fSeparate(wrapper)).xProjection(wrapper).decodeVector2fSeparate(wrapper), wrapper).decodePoint2fSeparate(wrapper))) {
            pos = pos.minus((dir.times(moveSpeed, wrapper).decodeVector2fSeparate(wrapper)).xProjection(wrapper).decodeVector2fSeparate(wrapper), wrapper).decodePoint2fSeparate(wrapper)
        }
        if(canMove(pos.minus((dir.times(moveSpeed, wrapper).decodeVector2fSeparate(wrapper)).yProjection(wrapper).decodeVector2fSeparate(wrapper), wrapper).decodePoint2fSeparate(wrapper))) {
            pos = pos.minus((dir.times(moveSpeed, wrapper).decodeVector2fSeparate(wrapper)).yProjection(wrapper).decodeVector2fSeparate(wrapper), wrapper).decodePoint2fSeparate(wrapper)
        }
        drawScene(pos, dir, plane, graphics)
        dir = dir.rotate(rotSpeed, wrapper).decodeVector2fSeparate(wrapper)
        plane = plane.rotate(rotSpeed, wrapper).decodeVector2fSeparate(wrapper)
        drawScene(pos, dir, plane, graphics)
    }
}


private fun drawScene(pos: Point2fSeparate, dir: Vector2fSeparate, plane: Vector2fSeparate, g: AbstractGraphics) {
    val wrapper = MutableMfvcWrapper()
    for (x in 0 until screenWidth) {
        //calculate ray position and direction
        val cameraX = 2 * x / screenHeight.toFloat() - 1 //x-coordinate in camera space
        val rayDir = dir.plus(plane.times(cameraX, wrapper).decodeVector2fSeparate(wrapper), wrapper).decodeVector2fSeparate(wrapper)
        //which box of the map we're in
        var mapLocation = pos.toLocation(wrapper).decodeLocationFSeparate(wrapper)

        //length of ray from current position to next x or y-side
        var sideDist: Vector2fSeparate

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
        val deltaDist = 1.0f.div(rayDir.abs(wrapper).decodeVector2fSeparate(wrapper), wrapper).decodeVector2fSeparate(wrapper)

        var perpWallDist: Float

        //what direction to step in x or y-direction (either +1 or -1)
        var step: Vector2fSeparate

        var hit = 0 //was there a wall hit?
        var side = 0 //was a NS or a EW wall hit?
        //calculate step and initial sideDist
        if (rayDir.x < 0) {
            step = Vector2fSeparate((-1).toFloat(), 0.0f)
            sideDist = (pos.toVector(wrapper).decodeVector2fSeparate(wrapper)
                .minus(mapLocation.toVector(wrapper).decodeVector2fSeparate(wrapper), wrapper)
                .decodeVector2fSeparate(wrapper)).xProjection(wrapper).decodeVector2fSeparate(wrapper)
                .times(deltaDist.x, wrapper).decodeVector2fSeparate(wrapper)
        } else {
            step = Vector2fSeparate(1.toFloat(), 0.0f)
            sideDist = (mapLocation.toVector(wrapper).decodeVector2fSeparate(wrapper).plus(Vector2fSeparate(1.0f, 0.0f), wrapper)
                .decodeVector2fSeparate(wrapper)
                .minus(pos.toVector(wrapper).decodeVector2fSeparate(wrapper), wrapper).decodeVector2fSeparate(wrapper)).xProjection(
                    wrapper
                ).decodeVector2fSeparate(wrapper)
                .times(deltaDist.x, wrapper).decodeVector2fSeparate(wrapper)
        }
        if (rayDir.y < 0) {
            step = step.plus(Vector2fSeparate(0.0f, (-1).toFloat()), wrapper).decodeVector2fSeparate(wrapper)
            sideDist = sideDist.plus(
                (pos.toVector(wrapper).decodeVector2fSeparate(wrapper)
                    .minus(mapLocation.toVector(wrapper).decodeVector2fSeparate(wrapper), wrapper)
                    .decodeVector2fSeparate(wrapper)).yProjection(wrapper).decodeVector2fSeparate(wrapper)
                    .times(deltaDist.y, wrapper).decodeVector2fSeparate(wrapper), wrapper
            ).decodeVector2fSeparate(wrapper)
        } else {
            step = step.plus(Vector2fSeparate(0.0f, 1.toFloat()), wrapper).decodeVector2fSeparate(wrapper)
            sideDist = sideDist.plus(
                (mapLocation.toVector(wrapper).decodeVector2fSeparate(wrapper).plus(Vector2fSeparate(0.0f, 1.0f), wrapper)
                    .decodeVector2fSeparate(wrapper)
                    .minus(pos.toVector(wrapper).decodeVector2fSeparate(wrapper), wrapper)
                    .decodeVector2fSeparate(wrapper)).yProjection(wrapper).decodeVector2fSeparate(wrapper)
                    .times(deltaDist.y, wrapper).decodeVector2fSeparate(wrapper), wrapper
            ).decodeVector2fSeparate(wrapper)
        }
        //perform DDA
        while (hit == 0) {
            //jump to next map square, either in x-direction, or in y-direction
            if (sideDist.x < sideDist.y) {
                sideDist = sideDist.plus(deltaDist.xProjection(wrapper).decodeVector2fSeparate(wrapper), wrapper)
                    .decodeVector2fSeparate(wrapper)
                mapLocation = mapLocation.step(step.xProjection(wrapper).decodeVector2fSeparate(wrapper), wrapper)
                    .decodeLocationFSeparate(wrapper)
                side = 0
            } else {
                sideDist = sideDist.plus(deltaDist.yProjection(wrapper).decodeVector2fSeparate(wrapper), wrapper)
                    .decodeVector2fSeparate(wrapper)
                mapLocation = mapLocation.step(step.yProjection(wrapper).decodeVector2fSeparate(wrapper), wrapper)
                    .decodeLocationFSeparate(wrapper)
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
            if (side == 0) (sideDist.minus(deltaDist, wrapper).decodeVector2fSeparate(wrapper)).x
            else (sideDist.minus(deltaDist, wrapper).decodeVector2fSeparate(wrapper)).y

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

class MyFrameFSeparate : JFrame() {
    init {
        add(MyPanelFSeparate())
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
    MyFrameFSeparate()
}
