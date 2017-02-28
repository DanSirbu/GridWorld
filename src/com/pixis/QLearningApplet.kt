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
    val lastCellIndex = Settings.NUM_CELLS - 1
    val grid = Grid(numCells = Settings.NUM_CELLS, rewardGridLocation = Coordinate(row = 1, column = 2))
    val QLearner = QLearning(grid = grid, trainingDelay = 100, trainedDelay = 500)

    val screenSize = 500
    val cellPixelSize = screenSize / Settings.NUM_CELLS

    val showGrid = false

    override fun settings() {
        size(screenSize, screenSize)
        noSmooth()
    }

    override fun mouseClicked() {
        //Note, since we use row = x, y and x are reversed
        if (!QLearner.Paused)
            return

        val row = mouseY / cellPixelSize
        val column = mouseX / cellPixelSize

        QLearner.s_t = Coordinate(row, column).toMatrixIndex()
    }

    override fun keyPressed() {
        if (key == CODED.toChar()) {
            val xy = Coordinate.fromMatrixIndex(QLearner.s_t)
            when (keyCode) {
                PConstants.UP -> moveStateTo(xy.row - 1, xy.column)
                PConstants.LEFT -> moveStateTo(xy.row, xy.column - 1)
                PConstants.RIGHT -> moveStateTo(xy.row, xy.column + 1)
                PConstants.DOWN -> moveStateTo(xy.row + 1, xy.column)
            }
        } else {
            when (key) {
                ' ' -> {
                    QLearner.Paused = !QLearner.Paused
                    if(QLearner.Paused)
                        System.out.println("Paused")
                    else
                        System.out.println("UnPaused")
                }
                10.toChar() -> { // 10 = Enter
                    QLearner.isLearning = !QLearner.isLearning
                    System.out.println("Learning: " + QLearner.isLearning)
                }
                'r' -> Debugging.printMatrix(QLearner.RMatrix, QLearner.matrixSize)
                'q' -> Debugging.printMatrix(QLearner.QMatrix, QLearner.matrixSize)
            }
        }
    }

    private fun moveStateTo(row1: Int, column1: Int) {
        val row = constrain(row1, 0, lastCellIndex)
        val column = constrain(column1, 0, lastCellIndex)

        val a_t = Coordinate(row, column).toMatrixIndex()
        QLearner.QLearningIteration(a_t)
    }

    override fun setup() {
        stroke(48) //Color of lines
        textSize(cellPixelSize / 10f)
        textAlign(PConstants.CENTER, PConstants.CENTER)
    }

    override fun draw() {
        background(255)

        //if (QLearner.isLearning) QLearner.QLearningIteration() else QLearner.QIteration()

        val currentStatePoint = Coordinate.fromMatrixIndex(QLearner.s_t)

        val a = 'A'
        //Draw states
        for (row in 0..Settings.NUM_CELLS - 1) {
            for (column in 0..Settings.NUM_CELLS - 1) {
                val coordinate = Coordinate(row, column)

                if (showGrid) {
                    fill(getStateColor(grid.cells[row][column]))
                    //Note, since we use row = x, y and x are reversed
                    rect((column * cellPixelSize).toFloat(), (row * cellPixelSize).toFloat(), cellPixelSize.toFloat(), cellPixelSize.toFloat()) //rect(xleftcorner, ytopcorner, width, height);
                } else {
                    drawTriangles(coordinate)
                }

                if (currentStatePoint == coordinate) {
                    fill(Color.BLUE.rgb)
                    ellipse((coordinate.column + 0.5f) * cellPixelSize, (coordinate.row + 0.5f) * cellPixelSize, cellPixelSize / 4.0f, cellPixelSize / 4.0f)
                }

                /*fill(255)
                val stateChar:Char = (a + coordinate.toMatrixIndex())
                text(stateChar, (column + 0.5f) * cellPixelSize, (row + 0.5f) * cellPixelSize)*/
            }
        }
    }

    fun getStateColor(reward: Int): Int {
        var cellColor = Color.gray
        if (reward > 0) {
            cellColor = Color.green
        } else if (reward < 0) {
            cellColor = Color.red
        }

        return cellColor.rgb
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
