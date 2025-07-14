package com.codeit.otboo.domain.location.vo;

public class GridCoordinate {
    private final int x;
    private final int y;

    public GridCoordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}