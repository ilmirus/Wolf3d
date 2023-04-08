package long_pack

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

operator fun Array<IntArray>.get(location: LocationF): Int = this[location.x][location.y]

fun canMove(point: Point2f): Boolean = worldMap[point.x.toInt()][point.y.toInt()] == 0

@JvmInline
value class Point2f private constructor(private val data: Long) {
    val x get() = Float.fromBits(data.shr(32).toInt())
    val y get() = Float.fromBits(data.toInt())
    constructor(x: Float, y: Float) : this(x.toRawBits().toLong().shl(32) or y.toRawBits().toLong())
    operator fun plus(vector: Vector2f): Point2f =
        Point2f(x + vector.x, y + vector.y)

    operator fun minus(vector: Vector2f): Point2f =
        Point2f(x - vector.x, y - vector.y)

    fun toLocation(): LocationF =
        LocationF(x.toInt(), y.toInt())

    fun toVector(): Vector2f =
        Vector2f(x, y)
}

@JvmInline
value class Vector2f private constructor(private val data: Long) {
    val x get() = Float.fromBits(data.shr(32).toInt())
    val y get() = Float.fromBits(data.toInt())
    constructor(x: Float, y: Float) : this(x.toRawBits().toLong().shl(32) or y.toRawBits().toLong().and(0xFFFFFFFF))
    
    fun rotate(angle: Float): Vector2f =
        Vector2f(x * cos(angle) - y * sin(angle), x * sin(angle) + y * cos(angle))

    operator fun times(factor: Float): Vector2f =
        Vector2f(x * factor, y * factor)

    operator fun plus(vector: Vector2f): Vector2f =
        Vector2f(x + vector.x, y + vector.y)

    operator fun minus(vector: Vector2f): Vector2f =
        Vector2f(x - vector.x, y - vector.y)

    fun abs(): Vector2f =
        Vector2f(Math.abs(x), Math.abs(y))

    fun xProjection(): Vector2f =
        Vector2f(x, 0.0f)

    fun yProjection(): Vector2f =
        Vector2f(0.0f, y)
}

@JvmInline
value class LocationF private constructor(private val data: Long) {
    val x get() = data.shr(32).toInt()
    val y get() = data.toInt()
    constructor(x: Int, y: Int) : this(x.toLong().shl(32) or y.toLong().and(0xFFFFFFFF))
    fun toVector(): Vector2f =
        Vector2f(x.toFloat(), y.toFloat())

    fun step(vector: Vector2f): LocationF =
        LocationF((toVector() + vector).x.toInt(), (toVector() + vector).y.toInt())
}

operator fun Float.div(vector: Vector2f): Vector2f =
    Vector2f(this / vector.x, this / vector.y)

class MyPanelF : JPanel(), KeyListener, MouseListener, Benchmarkable {
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
        for (x in 0 until screenWidth) {
            //calculate ray position and direction
            val cameraX = 2 * x / screenHeight.toFloat() - 1 //x-coordinate in camera space
            val rayDir = dir + plane * cameraX
            //which box of the map we're in
            var mapLocation = pos.toLocation()

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
            val deltaDist = 1.0f / rayDir.abs()

            var perpWallDist: Float

            //what direction to step in x or y-direction (either +1 or -1)
            var step: Vector2f

            var hit = 0 //was there a wall hit?
            var side = 0 //was a NS or a EW wall hit?
            //calculate step and initial sideDist
            if (rayDir.x < 0) {
                step = Vector2f((-1).toFloat(), 0.0f)
                sideDist = (pos.toVector() - mapLocation.toVector()).xProjection() * deltaDist.x
            } else {
                step = Vector2f(1.toFloat(), 0.0f)
                sideDist = (mapLocation.toVector() + Vector2f(1.0f, 0.0f) - pos.toVector()).xProjection() * deltaDist.x
            }
            if (rayDir.y < 0) {
                step += Vector2f(0.0f, (-1).toFloat())
                sideDist += (pos.toVector() - mapLocation.toVector()).yProjection() * deltaDist.y
            } else {
                step += Vector2f(0.0f, 1.toFloat())
                sideDist += (mapLocation.toVector() + Vector2f(0.0f, 1.0f) - pos.toVector()).yProjection() * deltaDist.y
            }
            //perform DDA
            while (hit == 0) {
                //jump to next map square, either in x-direction, or in y-direction
                if (sideDist.x < sideDist.y) {
                    sideDist += deltaDist.xProjection()
                    mapLocation = mapLocation.step(step.xProjection())
                    side = 0
                } else {
                    sideDist += deltaDist.yProjection()
                    mapLocation = mapLocation.step(step.yProjection())
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
            perpWallDist = if (side == 0) (sideDist - deltaDist).x else (sideDist - deltaDist).y

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
        when (e.keyCode) {
            KeyEvent.VK_UP -> {
                if(canMove(pos + (dir * moveSpeed).xProjection())) pos += (dir * moveSpeed).xProjection()
                if(canMove(pos + (dir * moveSpeed).yProjection())) pos += (dir * moveSpeed).yProjection()
            }
            KeyEvent.VK_DOWN -> {
                if(canMove(pos - (dir * moveSpeed).xProjection())) pos -= (dir * moveSpeed).xProjection()
                if(canMove(pos - (dir * moveSpeed).yProjection())) pos -= (dir * moveSpeed).yProjection()
            }
            KeyEvent.VK_LEFT -> {
                //both camera direction and camera plane must be rotated
                dir = dir.rotate(rotSpeed)
                plane = plane.rotate(rotSpeed)
            }
            KeyEvent.VK_RIGHT -> {
                //both camera direction and camera plane must be rotated
                dir = dir.rotate(-rotSpeed)
                plane = plane.rotate(-rotSpeed)
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
