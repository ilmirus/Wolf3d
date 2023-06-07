package valhalla.rendering.double_;

value record Vector2(double x, double y) {

    Vector2 rotate(double angle) {
        return new Vector2(x * Math.cos(angle) - y * Math.sin(angle), x * Math.sin(angle) + y * Math.cos(angle));
    }

    Vector2 times(double factor) {
        return new Vector2(x * factor, y * factor);
    }

    Vector2 plus(Vector2 vector) {
        return new Vector2(x + vector.x, y + vector.y);
    }

    Vector2 minus(Vector2 vector) {
        return new Vector2(x - vector.x, y - vector.y);
    }

    Vector2 abs() {
        return new Vector2(Math.abs(x), Math.abs(y));
    }

    Vector2 xProjection() {
        return new Vector2(x, 0.0);
    }

    Vector2 yProjection() {
        return new Vector2(0.0, y);
    }
}
