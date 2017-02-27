package com.pixis

import processing.core.PApplet
import java.awt.Color
import java.awt.Point

fun main(args: Array<String>) {
    PApplet.main("com.pixis.ExampleApplet")
}
class ExampleApplet : PApplet() {
    val numCells = 3
    val grid = Grid(numCells = numCells, rewardGridLocation = Point(0, 0))
    val QLearner = QLearning(grid = grid, trainingDelay = 100, trainedDelay = 500)

    val screenSize = 500
    val cellPixelSize = screenSize / numCells
    /*
    val cellHalfPixelSize: Int = cellPixelSize / 2
    val triangleWidth: Int = cellPixelSize / 6
    val triangleHeight: Int = cellPixelSize / 8
*/

    override fun settings() {
        size(screenSize, screenSize)
        noSmooth()
    }

    override fun mouseClicked() {
        //Note, since we use row = x, y and x are reversed
        if(!QLearner.Paused)
            return

        val gridY = mouseX / cellPixelSize
        val gridX = mouseY / cellPixelSize

        QLearner.s_t = QLearner.gridPointToMatrix(Point(gridX, gridY))
    }
    override fun keyPressed() {
        when (key) {
            ' ' -> QLearner.Paused = !QLearner.Paused
            10.toChar() -> { // 10 = Enter
                QLearner.isLearning = !QLearner.isLearning
                System.out.println("Learning: " + QLearner.isLearning)
            }
            'r' -> Debugging.printMatrix(QLearner.RMatrix, QLearner.matrixSize)
            'q' -> Debugging.printMatrix(QLearner.QMatrix, QLearner.matrixSize)
        }
    }

    override fun setup() {
        stroke(48) //Color of lines
        textSize(25f)
    }

    override fun draw() {
        background(255)

        if (QLearner.isLearning) QLearner.QLearningIteration() else QLearner.QIteration()

        val currentStatePoint = QLearner.matrixToGridPoint(QLearner.s_t)

        val a = 'A'
        //Draw states
        for (x in 0..numCells - 1) {
            for (y in 0..numCells - 1) {
                fill(getStateColor(grid.cells[x][y]))
                if(currentStatePoint.x == x && currentStatePoint.y == y) {
                    fill(Color.BLUE.rgb)
                }
                //Note, since we use row = x, y and x are reversed
                rect((y * cellPixelSize).toFloat(), (x * cellPixelSize).toFloat(), cellPixelSize.toFloat(), cellPixelSize.toFloat()) //rect(xleftcorner, ytopcorner, width, height);

                fill(0)
                text((a + QLearner.gridPointToMatrix(Point(x, y))), ((y + 0.5) * cellPixelSize).toFloat(), ((x + 0.5) * cellPixelSize).toFloat())
            }
        }
    }

    fun getStateColor(reward: Int): Int {
        var cellColor = Color.white
        if (reward > 0) {
            cellColor = Color.green
        } else if (reward < 0) {
            cellColor = Color.red
        }

        return cellColor.rgb
    }

    /*private fun drawTriangles(x: Int, y: Int) {
fill(Color.gray.rgb)

val borderBuffer = 0

val yHorizontalCenter = y * cellPixelSize + cellHalfPixelSize
val xHorizontalCenter = x * cellPixelSize + cellHalfPixelSize

val leftBorderX = x * cellPixelSize + borderBuffer
val rightBorderX = (x + 1) * cellPixelSize - borderBuffer

val topBorderY = y * cellPixelSize + borderBuffer
val bottomBorderY = (y + 1) * cellPixelSize - borderBuffer


val leftTriangleX = leftBorderX + triangleWidth
val y2 = yHorizontalCenter - triangleHeight
val y3 = yHorizontalCenter + triangleHeight
//Left triangle
triangle(leftBorderX.toFloat(), yHorizontalCenter.toFloat(), leftTriangleX.toFloat(), y2.toFloat(), leftTriangleX.toFloat(), y3.toFloat())
//Right triangle
val rightTriangleX = rightBorderX - triangleWidth
triangle(rightBorderX.toFloat(), yHorizontalCenter.toFloat(), rightTriangleX.toFloat(), y2.toFloat(), rightTriangleX.toFloat(), y3.toFloat())

val x2 = xHorizontalCenter - triangleWidth
val x3 = xHorizontalCenter + triangleWidth
//Top triangle
val topTriangleY = topBorderY + triangleHeight
triangle(xHorizontalCenter.toFloat(), topBorderY.toFloat(), x2.toFloat(), topTriangleY.toFloat(), x3.toFloat(), topTriangleY.toFloat())
//Bottom triangle
val bottomTriangleY = bottomBorderY - triangleHeight
triangle(xHorizontalCenter.toFloat(), bottomBorderY.toFloat(), x2.toFloat(), bottomTriangleY.toFloat(), x3.toFloat(), bottomTriangleY.toFloat())
}*/
}
