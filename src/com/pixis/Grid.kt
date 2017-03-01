package com.pixis

import com.pixis.model.Coordinate
import com.pixis.model.Edge
import jdk.nashorn.internal.runtime.Debug
import processing.core.PApplet

import java.awt.*
import java.util.*

data class CoordinateReward(val coord: Coordinate, val reward: Int)

class Grid(val numCells: Int, val rewards: List<CoordinateReward>) {

    var cells = Array(numCells) { IntArray(numCells) }
    val rewardsCoord = rewards.map { it.coord }
    var maxValueIndexAndValue = CoordinateReward(Coordinate(0, 0), Int.MIN_VALUE)

    init {
        //Initialize cells
        //x = row
        // y = column
        cells.forEach { it.fill(0) }


        rewards.forEach {
            if(it.reward > maxValueIndexAndValue.reward) {
                maxValueIndexAndValue = it
            }

            cells[it.coord.row][it.coord.column] = it.reward
        }
    }
}
