package com.github.kennarddh.mindustry.plague.core.commons


enum class Align(val arcAlign: Int, val displayName: String) {
    Center(arc.util.Align.center, "Center"),
    Top(arc.util.Align.top, "Top"),
    Bottom(arc.util.Align.bottom, "Bottom"),
    Left(arc.util.Align.left, "Left"),
    Right(arc.util.Align.right, "Right"),
    TopLeft(arc.util.Align.topLeft, "Top Left"),
    TopRight(arc.util.Align.topRight, "Top Right"),
    BottomLeft(arc.util.Align.bottomLeft, "Bottom Left"),
    BottomRight(arc.util.Align.bottomRight, "Bottom Right"),
}