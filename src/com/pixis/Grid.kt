package com.pixis

import jdk.nashorn.internal.runtime.Debug
import processing.core.PApplet

import java.awt.*
import java.util.*

/**
 * Created by Dan on 2/23/2017.
 */
class Grid : PApplet() {
    internal var cellSize = 3
    internal var matrixSize = cellSize * cellSize
    internal var triangleWidth: Int = 0
    internal var triangleHeight: Int = 0

    internal var cells = Array(cellSize) { IntArray(cellSize) }
    internal var RMatrix = Array(matrixSize) { IntArray(matrixSize) }
    internal var QMatrix = Array(matrixSize) { IntArray(matrixSize) }

    internal var cellPixelSize: Int = 0
    internal var cellHalfPixelSize: Int = 0

    internal val alpha = 0.1
    internal val gamma = 0.9
    private val debug = false
    var Paused = false

    override fun settings() {
        val displaySize = 500
        size(displaySize, displaySize)
        noSmooth()
    }

    override fun keyPressed() {
        when (key) {
            ' ' -> Paused = !Paused
            'r' -> Debugging.printMatrix(RMatrix, matrixSize)
            'q' -> Debugging.printMatrix(QMatrix, matrixSize)
        }
    }

    var rewardX = 1
    var rewardY = 0

    override fun setup() {
        cellPixelSize = width / cellSize
        cellHalfPixelSize = cellPixelSize / 2
        triangleWidth = cellPixelSize / 6
        triangleHeight = cellPixelSize / 8
        stroke(48) //Color of lines

        //Initialize cells
        //x = row
        // y = column
        cells.forEach { it.fill(0) }
        //Initialize QMatrix
        QMatrix.forEach { it.fill(0) }
        //Initialize Reward matrix to -1 (no edge)
        RMatrix.forEach { it.fill(-1) }


        cells[rewardX][rewardY] = 100

        //
        //    -- y ->
        //  -
        //  x
        //  -

        var edgeList = ArrayList<Edge>()
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
        createEdge(rewardX, rewardY, rewardX, rewardY)?.let { edgeList.add(it) }

        if(debug) {
            Debugging.printEdges(edgeList)
        }

        //Add edges
        for ((source, target, weight) in edgeList) {

            //For a 3x2 grid (r x c)
            //A | D
            //B | E
            //C | F
            //This way, it means
            //   A  B  C  D  E  F
            //A 00 01 02 10 11 12 //and so on
            //B

            RMatrix[gridPointToMatrix(source)][gridPointToMatrix(target)] = weight
        }

        if (debug) {
            Debugging.printMatrix(RMatrix, matrixSize)
            Debugging.printMatrix(QMatrix, matrixSize);
        }

        runQ()
    }

    fun createEdge(sourceX: Int, sourceY: Int, x: Int, y: Int): Edge? {
        val weight = getValue(x, y)
        val edge = Edge(Point(sourceX, sourceY), Point(x, y), weight)

        if (weight == -1)
            return null
        return edge
    }

    fun getValue(x: Int, y: Int): Int {
        if (x < 0 || y < 0 || x == cellSize || y == cellSize)
            return -1

        return cells[x][y]
    }

    override fun draw() {
        background(255)

        //Draw states
        for (x in 0..cellSize - 1) {
            for (y in 0..cellSize - 1) {
                val cellReward = cells[x][y]
                var cellColor = Color.white
                if (cellReward > 0) {
                    cellColor = Color.green
                } else if (cellReward < 0) {
                    cellColor = Color.red
                }
                fill(cellColor.rgb)
                rect((x * cellPixelSize).toFloat(), (y * cellPixelSize).toFloat(), cellPixelSize.toFloat(), cellPixelSize.toFloat()) //rect(xleftcorner, ytopcorner, width, height);

                drawTriangles(x, y)
            }
        }

    }

    private fun drawTriangles(x: Int, y: Int) {
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
    }

    fun runQ() {
        val endRow = 1
        val random = Random(10)

        var currentRow = 3
        var lastUpdateTime = System.currentTimeMillis()

        Thread(Runnable {
            while(true) {
                if(Paused || System.currentTimeMillis() - lastUpdateTime < 500)
                    continue;

                if(currentRow == endRow) {
                    currentRow = random.nextInt(matrixSize - 1)
                }

                lastUpdateTime = System.currentTimeMillis()

                val targets = getValidTargets(currentRow)
                val nextRow = targets[random((targets.size - 1).toFloat()).toInt()]
                //nextMove is the column

                val nextTargets = getValidTargets(nextRow)
                val maxNextReward: Int = nextTargets.map { RMatrix[nextRow][it] }.max() ?: 0

                QMatrix[currentRow][nextRow] = (RMatrix[currentRow][nextRow] + alpha * maxNextReward).toInt()
                println(currentRow.toString() + " - Max: " + QMatrix[currentRow][nextRow])

                currentRow = nextRow
            }
        }).start()
    }

    fun getValidTargets(rowIndex: Int): IntArray {
        val validIndexes = ArrayList<Int>()
        val row = RMatrix[rowIndex]

        row.forEachIndexed { i, item ->  if(item != -1) validIndexes.add(i) }

        return validIndexes.toIntArray()
    }

    //We are relloling our grid
    fun matrixToGridPoint(sourceOrTarget: Int): Point {
        val y = sourceOrTarget % cellSize
        val x = sourceOrTarget / cellSize

        return Point(x, y)
    }

    //We are basically unrolling our grid
    fun gridPointToMatrix(p: Point): Int {
        return gridPointToMatrix(p.x, p.y)
    }

    fun gridPointToMatrix(x: Int, y: Int): Int {
        return cellSize * x + y
    }
}
