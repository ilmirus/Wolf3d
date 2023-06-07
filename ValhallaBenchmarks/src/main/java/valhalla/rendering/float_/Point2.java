package valhalla.rendering.float_;

value record Point2(float x, float y) {

    Point2 plus(Vector2 vector) {
        return new Point2(x + vector.x(), y + vector.y());
    }

    Point2 minus(Vector2 vector) {
        return new Point2(x - vector.x(), y - vector.y());
    }

    Location toLocation() {
        return new Location((int)x, (int)y);
    }

    Vector2 toVector() {
        return new Vector2(x, y);
    }
}
