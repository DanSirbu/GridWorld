package com.pixis

import jdk.nashorn.internal.runtime.Debug
import processing.core.PApplet

import java.awt.*
import java.util.*

const val screenSize = 500
const val cellSize = 4
const val matrixSize = cellSize * cellSize
const val cellPixelSize = screenSize / cellSize
/*
const val cellHalfPixelSize: Int = cellPixelSize / 2
const val triangleWidth: Int = cellPixelSize / 6
const val triangleHeight: Int = cellPixelSize / 8
*/
const val NO_EDGE = -1
const val alpha = 0.8
class Grid : PApplet() {
    val rewardGridLocation = Point(0, 2)
    val rewardRow = gridPointToMatrix(rewardGridLocation)

    var currentState: Int = 1
    var Paused = false
    var isLearning: Boolean = true

    var cells = Array(cellSize) { IntArray(cellSize) }
    var RMatrix = Array(matrixSize) { IntArray(matrixSize) }
    var QMatrix = Array(matrixSize) { IntArray(matrixSize) }

    //For Q Update
    val random = Random(10) //Use seed to be able to compare results
    var lastUpdateTime = System.currentTimeMillis()

    override fun settings() {
        size(screenSize, screenSize)
        noSmooth()
    }

    override fun keyPressed() {
        when (key) {
            ' ' -> Paused = !Paused
            10.toChar() -> isLearning = !isLearning // 10 = Enter
            'r' -> Debugging.printMatrix(RMatrix, matrixSize)
            'q' -> Debugging.printMatrix(QMatrix, matrixSize)
        }
    }

    override fun setup() {
        stroke(48) //Color of lines

        //Initialize cells
        //x = row
        // y = column
        cells.forEach { it.fill(0) }
        //Reward
        cells[rewardGridLocation.x][rewardGridLocation.y] = 100

        //Initialize QMatrix
        QMatrix.forEach { it.fill(0) }

        //Initialize Reward matrix to -1 (no edge)
        RMatrix.forEach { it.fill(NO_EDGE) }

        createRMatrix()
    }

    private fun createRMatrix() {
        val edgeList = ArrayList<Edge>()
        for (x in 0..cellSize - 1) {
            for (y in 0..cellSize - 1) {
                //Up
                createEdge(x, y, x - 1, y)?.let { edgeList.add(it) }
                //Down
                createEdge(x, y, x + 1, y)?.let { edgeList.add(it) }
                //Left
                createEdge(x, y, x, y - 1)?.let { edgeList.add(it) }
                //Right
                createEdge(x, y, x, y + 1)?.let { edgeList.add(it) }
            }
        }
        //Add recursive edge(s)
        createEdge(rewardGridLocation.x, rewardGridLocation.y, rewardGridLocation.x, rewardGridLocation.y)?.let { edgeList.add(it) }

        //Add edges
        for ((source, target, weight) in edgeList) {
            RMatrix[gridPointToMatrix(source)][gridPointToMatrix(target)] = weight
        }
    }

    fun createEdge(sourceX: Int, sourceY: Int, x: Int, y: Int): Edge? {
        if(!(x in 0..cellSize - 1 && y in 0..cellSize - 1))
            return null

        val weight = cells[x][y]
        return Edge(Point(sourceX, sourceY), Point(x, y), weight)
    }

    fun getNextStatesIndexes(rowIndex: Int): IntArray {
        val validIndexes = ArrayList<Int>()
        val row: IntArray = RMatrix[rowIndex]

        row.forEachIndexed { i, item ->  if(item != -1) validIndexes.add(i) }

        return validIndexes.toIntArray()
    }

    //Inverse of unroll
    fun matrixToGridPoint(sourceOrTarget: Int): Point {
        val x = sourceOrTarget % cellSize
        val y = sourceOrTarget / cellSize

        return Point(x, y)
    }

    //We are basically unrolling our grid
    //Ex.
    // (x, y)
    // A(0,0) | C (1,0)
    // B(1,0) | D (1, 1)
    //
    //We want to convert the coordinate to:
    // (0 indexed)
    //
    // A (0)
    // B (1)
    // C (2)
    // D (3)
    fun gridPointToMatrix(p: Point): Int {
        return p.x + cellSize * p.y
    }

    override fun draw() {
        background(255)

        if (isLearning) QLearningIteration() else QIteration()

        val currentStatePoint = matrixToGridPoint(currentState)

        //Draw states
        for (x in 0..cellSize - 1) {
            for (y in 0..cellSize - 1) {
                fill(getStateColor(cells[x][y]))
                if(currentStatePoint.x == x && currentStatePoint.y == y) {
                    fill(Color.BLUE.rgb)
                }
                //Note, since for us row = x, we must change our coordinates
                rect((y * cellPixelSize).toFloat(), (x * cellPixelSize).toFloat(), cellPixelSize.toFloat(), cellPixelSize.toFloat()) //rect(xleftcorner, ytopcorner, width, height);
            }
        }
    }

    private fun QIteration() {
        if(Paused || (System.currentTimeMillis() - lastUpdateTime < 1000))
            return

        lastUpdateTime = System.currentTimeMillis()

        //Restart q search if at the end
        if(currentState == rewardRow)
            currentState = random.nextInt(matrixSize) //note nextInt is exclusive

        val nextTargets = getNextStatesIndexes(currentState)
        val highestRewardIndex: Int = nextTargets.map { Pair(it, RMatrix[currentState][it]) /* [rewardIndex, rewardValue] */ }.maxBy { it.second }!!.first

        currentState = highestRewardIndex
    }
    private fun QLearningIteration() {
        if(Paused || (System.currentTimeMillis() - lastUpdateTime < 50))
            return

        //Restart q search
        if(currentState == rewardRow)
            currentState = random.nextInt(matrixSize) //note nextInt is exclusive

        lastUpdateTime = System.currentTimeMillis()

        val nextStatesIndexes = getNextStatesIndexes(currentState)

        val nextRow = nextStatesIndexes[random.nextInt(nextStatesIndexes.size)]


        val nextTargets = getNextStatesIndexes(nextRow)
        val maxNextReward: Int = nextTargets.map { RMatrix[nextRow][it] }.max() ?: 0

        QMatrix[currentState][nextRow] = (RMatrix[currentState][nextRow] + alpha * maxNextReward).toInt()
        println((65 + currentState).toChar() + " - Max: " + QMatrix[currentState][nextRow])

        currentState = nextRow
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
