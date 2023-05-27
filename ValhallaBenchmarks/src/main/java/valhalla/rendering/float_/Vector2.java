package valhalla.rendering.float_;

value record Vector2(float x, float y) {

    Vector2 rotate(float angle) {
        return new Vector2(x * (float)Math.cos(angle) - y * (float)Math.sin(angle), x * (float)Math.sin(angle) + y * (float)Math.cos(angle));
    }

    Vector2 times(float factor) {
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
        return new Vector2(x, 0.0f);
    }

    Vector2 yProjection() {
        return new Vector2(0.0f, y);
    }
}
