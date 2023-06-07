package valhalla.rendering.float_;

value record Location(int x, int y) {

    Vector2 toVector() {
        return new Vector2(x, y);
    }

    Location step(Vector2 vector) {
        return new Location((int) toVector().plus(vector).x(), (int)toVector().plus(vector).y());
    }
}
