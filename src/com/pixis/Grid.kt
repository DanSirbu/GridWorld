package com.pixis

import com.pixis.model.Coordinate
import com.pixis.model.Edge
import jdk.nashorn.internal.runtime.Debug
import processing.core.PApplet

import java.awt.*
import java.util.*

class Grid(val numCells: Int, val rewardGridLocation: Coordinate) {

    var cells = Array(numCells) { IntArray(numCells) }

    init {
        //Initialize cells
        //x = row
        // y = column
        cells.forEach { it.fill(0) }
        //Reward
        cells[rewardGridLocation.row][rewardGridLocation.column] = 100
    }
}
