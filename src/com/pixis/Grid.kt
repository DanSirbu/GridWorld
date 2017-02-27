package com.pixis

import com.pixis.model.Edge
import jdk.nashorn.internal.runtime.Debug
import processing.core.PApplet

import java.awt.*
import java.util.*

const val NO_EDGE = -1
const val alpha = 0.1
const val gamma = 0.8
class Grid(val numCells: Int, val rewardGridLocation: Point) {

    var cells = Array(numCells) { IntArray(numCells) }

    init {
        //Initialize cells
        //x = row
        // y = column
        cells.forEach { it.fill(0) }
        //Reward
        cells[rewardGridLocation.x][rewardGridLocation.y] = 100
    }
}
