package valhalla.rendering.double_;

import kotlin.ranges.RangesKt;
import shared.AbstractGraphics;
import shared.CommonKt;

import static shared.ConfigKt.MicrobenchmarkRotations;

public final class Runner {
    static int get(int[][] $this$get, Location location) {
        return $this$get[location.x()][location.y()];
    }

    static boolean canMove(Point2 point) {
        return CommonKt.getWorldMap()[(int)point.x()][(int)point.y()] == 0;
    }

    static Vector2 div(double $this$div, Vector2 vector) {
        return new Vector2($this$div / vector.x(), $this$div / vector.y());
    }

    public static void heavyAction(AbstractGraphics graphics) {
        Point2 pos = new Point2(22.0, 12.0);
        Vector2 dir = new Vector2(-1.0, 0.0);
        Vector2 plane = new Vector2(0.0, 0.66);
        final double fps = 10.0;
        final double frameTime = (double)1 / fps;
        final double moveSpeed = frameTime * 0.5;
        final double rotSpeed = frameTime * 0.3;

        for(int i = 0; i < MicrobenchmarkRotations; ++i) {
            drawScene(pos, dir, plane, graphics);
            if (canMove(pos.plus(dir.times(moveSpeed).xProjection()))) {
                pos = pos.plus(dir.times(moveSpeed).xProjection());
            }

            if (canMove(pos.plus(dir.times(moveSpeed).yProjection()))) {
                pos = pos.plus(dir.times(moveSpeed).yProjection());
            }

            drawScene(pos, dir, plane, graphics);
            if (canMove(pos.minus(dir.times(moveSpeed).xProjection()))) {
                pos = pos.minus(dir.times(moveSpeed).xProjection());
            }

            if (canMove(pos.minus(dir.times(moveSpeed).yProjection()))) {
                pos = pos.minus(dir.times(moveSpeed).yProjection());
            }

            drawScene(pos, dir, plane, graphics);
            dir = dir.rotate(rotSpeed);
            plane = plane.rotate(rotSpeed);
            drawScene(pos, dir, plane, graphics);
        }
    }

    private static void drawScene(Point2 pos, Vector2 dir, Vector2 plane, AbstractGraphics g) {
        for (int x = 0; x < CommonKt.screenWidth; x++) {
            double cameraX = 2 * x / (double)CommonKt.screenHeight - 1; //x-coordinate in camera space
            Vector2 rayDir = dir.plus(plane.times(cameraX));
            Location mapLocation = pos.toLocation();
            Vector2 sideDist;
            Vector2 deltaDist = div(1.0, rayDir.abs());
            double perpWallDist;
            Vector2 step;
            int hit = 0;
            int side = 0;
            
            if (rayDir.x() < 0) {
                step = new Vector2((double)-1, 0.0);
                sideDist = pos.toVector().minus(mapLocation.toVector()).xProjection().times(deltaDist.x());
            } else {
                step = new Vector2((double)1, 0.0);
                sideDist = mapLocation.toVector().plus(new Vector2(1.0, 0.0)).minus(pos.toVector()).xProjection().times(deltaDist.x());
            }
            if (rayDir.y() < 0) {
                step = step.plus(new Vector2(0.0, (double) -1));
                sideDist = sideDist.plus(pos.toVector().minus(mapLocation.toVector()).yProjection().times(deltaDist.y()));
            } else {
                step = step.plus(new Vector2(0.0, (double) 1));
                sideDist = sideDist.plus(mapLocation.toVector().plus(new Vector2(0.0, 1.0)).minus(pos.toVector()).yProjection().times(deltaDist.y()));
            }
            while (hit == 0) {
                if (sideDist.x() < sideDist.y()) {
                    sideDist = sideDist.plus(deltaDist.xProjection());
                    mapLocation = mapLocation.step(step.xProjection());
                    side = 0;
                } else {
                    sideDist = sideDist.plus(deltaDist.yProjection());
                    mapLocation = mapLocation.step(step.yProjection());
                    side = 1;
                }
                if (get(CommonKt.getWorldMap(), mapLocation) > 0) hit = 1;
            }
            perpWallDist = side == 0 ? sideDist.minus(deltaDist).x() : sideDist.minus(deltaDist).y();

            int lineHeight = (int)(CommonKt.screenHeight / perpWallDist);

            //calculate lowest and highest pixel to fill in current stripe
            int drawStart = RangesKt.coerceAtLeast(-lineHeight / 2 + CommonKt.screenHeight / 2, 0);
            int drawEnd = RangesKt.coerceAtMost(lineHeight / 2 + CommonKt.screenHeight / 2, CommonKt.screenHeight - 1);

            //choose wall color
            var color = switch (get(CommonKt.getWorldMap(), mapLocation)) {
                case 1 -> 0xFF0000; //red
                case 2 -> 0x00FF00; //green
                case 3 -> 0x00FF00; //blue
                case 4 -> 0x0000FF; //white
                default -> 0xFFFF00; //yellow
            };

            //give x and y sides different brightness
            if (side == 1) {
                color /= 2;
            }

            //draw the pixels of the stripe as a vertical line
            g.setIntColor(color);
            g.drawLine(x, drawStart, x, drawEnd);
        }
    }

    public static void main() {
        new MyFrame();
    }

    // $FF: synthetic method
    public static void main(String[] var0) {
        main();
    }

    // $FF: synthetic method
    static void access$drawScene(Point2 pos, Vector2 dir, Vector2 plane, AbstractGraphics g) {
        drawScene(pos, dir, plane, g);
    }
}
