package com.pixis

import com.pixis.model.Coordinate
import com.pixis.model.toMatrixIndex
import processing.core.PApplet
import processing.core.PConstants
import java.awt.Color
import java.awt.Point

fun main(args: Array<String>) {
    PApplet.main("com.pixis.ExampleApplet")
}

class ExampleApplet : PApplet() {
    val grid = Grid(Settings.NUM_CELLS,
            listOf(
                CoordinateReward(Coordinate(row = 2, column = 2), reward = 100),
                CoordinateReward(Coordinate(row = 3, column = 1), reward = -100)
            )
    )

    val QLearner = QLearning(grid)

    val screenSize = 500
    val cellPixelSize = screenSize / Settings.NUM_CELLS

    var Paused = false
    var showGrid = false

    override fun settings() {
        size(screenSize, screenSize)
        noSmooth()
    }

    var cur_state = 0
    override fun mouseClicked() {
        val row = mouseY / cellPixelSize
        val column = mouseX / cellPixelSize

        cur_state = Coordinate(row, column).toMatrixIndex()
    }

    override fun keyPressed() {
        if (key == CODED.toChar()) {
            val xy = Coordinate.fromMatrixIndex(cur_state)
            when (keyCode) {
                PConstants.UP -> moveStateTo(xy.row - 1, xy.column)
                PConstants.LEFT -> moveStateTo(xy.row, xy.column - 1)
                PConstants.RIGHT -> moveStateTo(xy.row, xy.column + 1)
                PConstants.DOWN -> moveStateTo(xy.row + 1, xy.column)
            }
        } else {
            when (key) {
                ' ' -> {
                    Paused = !Paused
                    showGrid = Paused
                    if(Paused)
                        System.out.println("Paused")
                    else
                        System.out.println("UnPaused")
                }
                'r' -> Debugging.printMatrix(QLearner.RMatrix, QLearner.matrixSize)
                'q' -> Debugging.printMatrix(QLearner.QMatrix, QLearner.matrixSize)
            }
        }
    }

    private fun moveStateTo(row1: Int, column1: Int) {
        val coordinate = Coordinate(row1, column1)
        if(!coordinate.isValid())
            return

        val action = coordinate.toMatrixIndex()
        cur_state = QLearner.QLearningIteration(cur_state, action)
    }

    override fun setup() {
        stroke(48) //Color of lines
        textSize(cellPixelSize / 10f)
        textAlign(PConstants.CENTER, PConstants.CENTER)

        cur_state = QLearner.QLearning(30)
    }

    override fun draw() {
        background(255)

        val currentStateCoord = Coordinate.fromMatrixIndex(cur_state)

        //Draw states
        for (row in 0..Settings.NUM_CELLS - 1) {
            for (column in 0..Settings.NUM_CELLS - 1) {
                val gridCoord = Coordinate(row, column)

                if (showGrid) {
                    drawState(row, column)
                } else {
                    drawTriangles(gridCoord)
                    if(grid.rewardsCoord.contains(gridCoord)) { //Always draw reward
                        drawState(row, column)
                    }
                }

                //Draw Current State
                if (gridCoord == currentStateCoord) {
                    fill(Color.BLUE.rgb)
                    ellipse((gridCoord.column + 0.5f) * cellPixelSize, (gridCoord.row + 0.5f) * cellPixelSize, cellPixelSize / 4.0f, cellPixelSize / 4.0f)
                }

                //drawStateLabel(gridCoord)
            }
        }
    }

    private fun drawState(row: Int, column:Int) {
        fill(getStateColor(grid.cells[row][column]))
        //Note, since we use row = x, y and x are reversed
        rect((column * cellPixelSize).toFloat(), (row * cellPixelSize).toFloat(), cellPixelSize.toFloat(), cellPixelSize.toFloat()) //rect(xleftcorner, ytopcorner, width, height);
    }

    private fun drawStateLabel(coord: Coordinate) {
        fill(255)
        val stateChar:Char = ('A' + coord.toMatrixIndex())
        text(stateChar, (coord.column + 0.5f) * cellPixelSize, (coord.row + 0.5f) * cellPixelSize)
    }

    fun getStateColor(reward: Int): Int {
        var cellColor = Color.gray
        if (reward > 0) {
            cellColor = lerpColor(reward / 100f, Color.gray, Color.green)
        } else if (reward < 0) {
            cellColor = lerpColor(Math.abs(reward) / 100f, Color.gray, Color.red)
        }

        return cellColor.rgb
    }

    fun lerpColor(percent: Float, colorA: Color, colorB: Color): Color {
        val r = percent * (colorB.red - colorA.red) + colorA.red
        val g = percent * (colorB.green - colorA.green) + colorA.green
        val b = percent * (colorB.blue - colorA.blue) + colorA.blue

        return Color(r.toInt(), g.toInt(), b.toInt())
    }

    private fun drawTriangles(coordinate: Coordinate) {
        val currentState = coordinate.toMatrixIndex()

        val xHorizontalCenter = (coordinate.column + 0.5f) * cellPixelSize
        val yHorizontalCenter = (coordinate.row + 0.5f) * cellPixelSize

        val leftBorderX = (coordinate.column * cellPixelSize).toFloat()
        val rightBorderX = (coordinate.column + 1f) * cellPixelSize
        val topBorderY = (coordinate.row * cellPixelSize).toFloat()
        val bottomBorderY = (coordinate.row + 1f) * cellPixelSize

        //Left triangle
        val leftCoordinate = Coordinate(coordinate.row, coordinate.column - 1)
        if(leftCoordinate.isValid()) {
            val newState = leftCoordinate.toMatrixIndex()
            val weight = QLearner.QMatrix[currentState][newState]

            fill(getStateColor(weight))
            triangle(xHorizontalCenter, yHorizontalCenter, leftBorderX, topBorderY, leftBorderX, bottomBorderY)
            fill(255)
            text(weight, xHorizontalCenter - cellPixelSize / 3, yHorizontalCenter)
        }


        //Right triangle
        val rightCoordinate = Coordinate(coordinate.row, coordinate.column + 1)
        if(rightCoordinate.isValid()) {
            val newState = rightCoordinate.toMatrixIndex()
            val weight = QLearner.QMatrix[currentState][newState]

            fill(getStateColor(weight))
            triangle(xHorizontalCenter, yHorizontalCenter, rightBorderX, topBorderY, rightBorderX, bottomBorderY)
            fill(255)
            text(weight, xHorizontalCenter + cellPixelSize / 3, yHorizontalCenter)
        }

        //Top triangle
        val topCoordinate = Coordinate(coordinate.row - 1, coordinate.column)
        if(topCoordinate.isValid()) {
            val newState = topCoordinate.toMatrixIndex()
            val weight = QLearner.QMatrix[currentState][newState]

            fill(getStateColor(weight))
            triangle(xHorizontalCenter, yHorizontalCenter, leftBorderX, topBorderY, rightBorderX, topBorderY)
            fill(255)
            text(weight, xHorizontalCenter, yHorizontalCenter - cellPixelSize / 3)
        }

        //Bottom triangle
        val bottomCoordinate = Coordinate(coordinate.row + 1, coordinate.column)
        if(bottomCoordinate.isValid()) {
            val newState = bottomCoordinate.toMatrixIndex()
            val weight = QLearner.QMatrix[currentState][newState]

            fill(getStateColor(weight))
            triangle(xHorizontalCenter, yHorizontalCenter, leftBorderX, bottomBorderY, rightBorderX, bottomBorderY)
            fill(255)
            text(weight, xHorizontalCenter, yHorizontalCenter + cellPixelSize / 3)
        }
    }
}
