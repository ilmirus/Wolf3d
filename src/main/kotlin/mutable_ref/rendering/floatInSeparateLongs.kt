@file:Suppress("NOTHING_TO_INLINE")

package mutable_ref.rendering

import AbstractGraphics
import OffsetInSeconds
import asAbstract
import mutable_ref.MutableMfvcWrapper
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
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

inline operator fun Array<IntArray>.get(location: LocationFSeparate): Int = this[location.x][location.y]

inline fun canMove(point: Point2fSeparate): Boolean = worldMap[point.x.toInt()][point.y.toInt()] == 0

@JvmInline
value class Point2fSeparate(val x: Float, val y: Float) {
    fun plus(vector: Vector2fSeparate, wrapper: MutableMfvcWrapper) {
        val result = Point2fSeparate(x + vector.x, y + vector.y)
        wrapper.long0 = result.x.toRawBits().toLong()
        wrapper.long1 = result.y.toRawBits().toLong()
    }

    fun minus(vector: Vector2fSeparate, wrapper: MutableMfvcWrapper) {
        val result = Point2fSeparate(x - vector.x, y - vector.y)
        wrapper.long0 = result.x.toRawBits().toLong()
        wrapper.long1 = result.y.toRawBits().toLong()
    }

    fun toLocation(wrapper: MutableMfvcWrapper) {
        val result = LocationFSeparate(x.toInt(), y.toInt())
        wrapper.long0 = result.x.toLong()
        wrapper.long1 = result.y.toLong()
    }

    fun toVector(wrapper: MutableMfvcWrapper) {
        val result = Vector2fSeparate(x, y)
        wrapper.long0 = result.x.toRawBits().toLong()
        wrapper.long1 = result.y.toRawBits().toLong()
    }
}

@JvmInline
value class Vector2fSeparate(val x: Float, val y: Float) {
    fun rotate(angle: Float, wrapper: MutableMfvcWrapper) {
        val res = Vector2fSeparate(x * cos(angle) - y * sin(angle), x * sin(angle) + y * cos(angle))
        wrapper.long0 = res.x.toRawBits().toLong()
        wrapper.long1 = res.y.toRawBits().toLong()
    }

    fun times(factor: Float, wrapper: MutableMfvcWrapper) {
        val res = Vector2fSeparate(x * factor, y * factor)
        wrapper.long0 = res.x.toRawBits().toLong()
        wrapper.long1 = res.y.toRawBits().toLong()
    }

    fun plus(vector: Vector2fSeparate, wrapper: MutableMfvcWrapper) {
        val res = Vector2fSeparate(x + vector.x, y + vector.y)
        wrapper.long0 = res.x.toRawBits().toLong()
        wrapper.long1 = res.y.toRawBits().toLong()
    }

    fun minus(vector: Vector2fSeparate, wrapper: MutableMfvcWrapper) {
        val res = Vector2fSeparate(x - vector.x, y - vector.y)
        wrapper.long0 = res.x.toRawBits().toLong()
        wrapper.long1 = res.y.toRawBits().toLong()
    }

    fun abs(wrapper: MutableMfvcWrapper) {
        val res = Vector2fSeparate(abs(x), abs(y))
        wrapper.long0 = res.x.toRawBits().toLong()
        wrapper.long1 = res.y.toRawBits().toLong()
    }

    fun xProjection(wrapper: MutableMfvcWrapper) {
        val res = Vector2fSeparate(x, 0.0f)
        wrapper.long0 = res.x.toRawBits().toLong()
        wrapper.long1 = res.y.toRawBits().toLong()
    }

    fun yProjection(wrapper: MutableMfvcWrapper) {
        val res = Vector2fSeparate(0.0f, y)
        wrapper.long0 = res.x.toRawBits().toLong()
        wrapper.long1 = res.y.toRawBits().toLong()
    }
}

@JvmInline
value class LocationFSeparate(val x: Int, val y: Int) {
    fun toVector(wrapper: MutableMfvcWrapper) {
        val res = Vector2fSeparate(x.toFloat(), y.toFloat())
        wrapper.long0 = res.x.toRawBits().toLong()
        wrapper.long1 = res.y.toRawBits().toLong()
    }

    fun step(vector: Vector2fSeparate, wrapper: MutableMfvcWrapper) {
        toVector(wrapper)
        Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt())).plus(vector, wrapper)
        toVector(wrapper)
        Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt())).plus(vector, wrapper)
        val res = LocationFSeparate(
     (Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))).x.toInt(),
     (Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))).y.toInt()
        )
        wrapper.long0 = res.x.toLong()
        wrapper.long1 = res.y.toLong()
    }
}

fun Float.div(vector: Vector2fSeparate, wrapper: MutableMfvcWrapper) {
    val res = Vector2fSeparate(this / vector.x, this / vector.y)
    wrapper.long0 = res.x.toRawBits().toLong()
    wrapper.long1 = res.y.toRawBits().toLong()
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
        val wrapper = MutableMfvcWrapper()
        when (e.keyCode) {
            KeyEvent.VK_UP -> {
                dir.times(moveSpeed, wrapper)
                (pos.plus(
     (Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))),
     wrapper
 ))
                if (canMove(
         Point2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))
     )
                ) {
                    pos = run {
                        dir.times(moveSpeed, wrapper)
                        (Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))).xProjection(wrapper)
                        (pos.plus(
                            Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt())), wrapper
 ))
                        Point2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))
                    }
                }
                dir.times(moveSpeed, wrapper)
                (Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))).yProjection(wrapper)
                (pos.plus(
                    Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt())), wrapper
 ))
                if (canMove(
         Point2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))
     )
                ) {
                    pos = run {
                        dir.times(moveSpeed, wrapper)
                        (Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))).yProjection(wrapper)
                        pos.plus(
                            Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt())), wrapper
 )
                        Point2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))
                    }
                }
            }

            KeyEvent.VK_DOWN -> {
                dir.times(moveSpeed, wrapper)
                (Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))).xProjection(wrapper)
                pos.minus(
                    Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt())), wrapper
 )
                if (canMove(
         Point2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))
     )
                ) {
                    pos = run {
                        dir.times(moveSpeed, wrapper)
                        (Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))).xProjection(wrapper)
                        pos.minus(
                            Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt())), wrapper
 )
                        Point2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))
                    }
                }
                dir.times(moveSpeed, wrapper)
                (Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))).yProjection(wrapper)
                pos.minus(
                    Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt())), wrapper
 )
                if (canMove(
         Point2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))
     )
                ) {
                    pos = run {
                        dir.times(moveSpeed, wrapper)
                        (Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))).yProjection(wrapper)
                        pos.minus(
                            Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt())), wrapper
 )
                        Point2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))
                    }
                }
            }

            KeyEvent.VK_LEFT -> {
                //both camera direction and camera plane must be rotated
                dir = run {
                    dir.rotate(rotSpeed, wrapper)
                    Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))
                }
                plane = run {
                    plane.rotate(rotSpeed, wrapper)
                    Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))
                }
            }

            KeyEvent.VK_RIGHT -> {
                //both camera direction and camera plane must be rotated
                dir = run {
                    dir.rotate(-rotSpeed, wrapper)
                    Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))
                }
                plane = run {
                    plane.rotate(-rotSpeed, wrapper)
                    Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))
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

fun heavyActionFSeparate(graphics: AbstractGraphics) =
    drawScene(Point2fSeparate(22.0f, 12.0f), Vector2fSeparate(-1.0f, 0.0f), Vector2fSeparate(0.0f, 0.66f), graphics)


private fun drawScene(pos: Point2fSeparate, dir: Vector2fSeparate, plane: Vector2fSeparate, g: AbstractGraphics) {
    val wrapper = MutableMfvcWrapper()
    for (x in 0 until screenWidth) {
        //calculate ray position and direction
        val cameraX = 2 * x / screenHeight.toFloat() - 1 //x-coordinate in camera space
        plane.times(cameraX, wrapper)
        dir.plus(Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt())), wrapper)
        val rayDir =
            Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))
        //which box of the map we're in
        pos.toLocation(wrapper)
        var mapLocation = LocationFSeparate(wrapper.long0.toInt(), wrapper.long1.toInt())

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
        rayDir.abs(wrapper)
        1.0f.div(
            Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt())),
            wrapper
        )
        val deltaDist = Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))

        var perpWallDist: Float

        //what direction to step in x or y-direction (either +1 or -1)
        var step: Vector2fSeparate

        var hit = 0 //was there a wall hit?
        var side = 0 //was a NS or a EW wall hit?
        //calculate step and initial sideDist
        if (rayDir.x < 0) {
            step = Vector2fSeparate((-1).toFloat(), 0.0f)
            sideDist = run {
                pos.toVector(wrapper)
                mapLocation.toVector(wrapper)
                Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))
                    .minus(
                        Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt())),
                        wrapper
                    )
                (Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))).xProjection(
                    wrapper
                )
                Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))
                    .times(deltaDist.x, wrapper)
                Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))
            }
        } else {
            step = Vector2fSeparate(1.toFloat(), 0.0f)
            sideDist = run {
                mapLocation.toVector(wrapper)
                Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt())).plus(
                    Vector2fSeparate(1.0f, 0.0f),
                    wrapper
                )
                pos.toVector(wrapper)
                Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))
                    .minus(
                        Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt())),
                        wrapper
                    )
                (Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))).xProjection(
                    wrapper
                )
                Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))
                    .times(deltaDist.x, wrapper)
                Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))
            }
        }
        if (rayDir.y < 0) {
            step = run {
                step.plus(Vector2fSeparate(0.0f, (-1).toFloat()), wrapper)
                Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))
            }
            sideDist = run {
                pos.toVector(wrapper)
                mapLocation.toVector(wrapper)
                Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))
                    .minus(
                        Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt())),
                        wrapper
                    )
                (Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))).yProjection(
                    wrapper
                )
                Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))
                    .times(deltaDist.y, wrapper)
                sideDist.plus(
                    Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt())), wrapper
                )
                Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))
            }
        } else {
            step = run {
                step.plus(Vector2fSeparate(0.0f, 1.toFloat()), wrapper)
                Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))
            }
            sideDist = run {
                mapLocation.toVector(wrapper)
                Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt())).plus(
                    Vector2fSeparate(
                        0.0f,
                        1.0f
                    ), wrapper
                )
                pos.toVector(wrapper)
                Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))
                    .minus(
                        Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt())),
                        wrapper
                    )
                (Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))).yProjection(
                    wrapper
                )
                Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))
                    .times(deltaDist.y, wrapper)
                sideDist.plus(
                    Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt())), wrapper
                )
                Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))
            }
        }
        //perform DDA
        while (hit == 0) {
            //jump to next map square, either in x-direction, or in y-direction
            if (sideDist.x < sideDist.y) {
                sideDist = run {
                    deltaDist.xProjection(wrapper)
                    sideDist.plus(
                        Vector2fSeparate(
                            Float.fromBits(wrapper.long0.toInt()),
                            Float.fromBits(wrapper.long1.toInt())
                        ), wrapper
                    )
                    Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))
                }
                mapLocation = run {
                    step.xProjection(wrapper)
                    mapLocation.step(
                        Vector2fSeparate(
                            Float.fromBits(wrapper.long0.toInt()),
                            Float.fromBits(wrapper.long1.toInt())
                        ), wrapper
                    )
                    LocationFSeparate(wrapper.long0.toInt(), wrapper.long1.toInt())
                }
                side = 0
            } else {
                sideDist = run {
                    deltaDist.yProjection(wrapper)
                    sideDist.plus(
                        Vector2fSeparate(
                            Float.fromBits(wrapper.long0.toInt()),
                            Float.fromBits(wrapper.long1.toInt())
                        ), wrapper
                    )
                    Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))
                }
                mapLocation = run {
                    step.yProjection(wrapper)
                    mapLocation.step(
                        Vector2fSeparate(
                            Float.fromBits(wrapper.long0.toInt()),
                            Float.fromBits(wrapper.long1.toInt())
                        ), wrapper
                    )
                    LocationFSeparate(wrapper.long0.toInt(), wrapper.long1.toInt())
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
                (Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))).x
            } else {
                sideDist.minus(deltaDist, wrapper)
                (Vector2fSeparate(Float.fromBits(wrapper.long0.toInt()), Float.fromBits(wrapper.long1.toInt()))).y
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
