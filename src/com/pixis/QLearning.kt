package com.pixis

import com.pixis.model.Coordinate
import com.pixis.model.Edge
import com.pixis.model.toMatrixIndex
import java.awt.Point
import java.util.*

class QLearning(val grid: Grid) {
    val matrixSize = grid.numCells * grid.numCells

    var state: Int = 0 //Initial state
    val finalState = grid.maxValueIndexAndValue.coord.toMatrixIndex()

    var RMatrix = Array(matrixSize) { IntArray(matrixSize) }
    var QMatrix = Array(matrixSize) { IntArray(matrixSize) }

    init {
        //Initialize QMatrix
        QMatrix.forEach { it.fill(0) }

        //Initialize Reward matrix to (no edge)
        RMatrix.forEach { it.fill(Settings.NO_EDGE) }
        createRMatrix()
    }

    //We are basically unrolling our grid
    //Ex.
    // (row, column)
    // A(0,0) | C (0,1)
    // B(1,0) | D (1, 1)
    //
    //We want to convert the coordinate to:
    // (0 indexed)
    //
    // A (0)
    // B (1)
    // C (2)
    // D (3)

    fun QIteration() {
        val maxQ = maxQ(state)
        state = maxQ.first
    }

    //Q(st,at)←Q(st,at)+α[rt+γmaxaQ(st+1,a)−Q(st,at)]
    fun QLearningIteration(action: Int): Int {
        if(state == finalState) {
            state = action
            return 0
        }

        QMatrix[state][action] = (QMatrix[state][action] + Settings.Alpha * (RMatrix[state][action] + Settings.DiscountFactor * maxQ(action).second - QMatrix[state][action])).toInt()

        val updatedQValue = QMatrix[state][action]
        state = action
        return updatedQValue
    }

    /**
     * Gets the max q for the action
     * Returns [index, value]
     */
    fun maxQ(action: Int): Pair<Int, Int> {
        val nextTargets = getNextStatesIndexes(action)
        return nextTargets.map { rewardIndex -> Pair(rewardIndex, QMatrix[action][rewardIndex]) }.maxBy { it.second }!!
    }
    fun getNextStatesIndexes(rowIndex: Int): IntArray {
        val validIndexes = ArrayList<Int>()
        val row: IntArray = RMatrix[rowIndex]

        row.forEachIndexed { i, item ->  if(item != Settings.NO_EDGE) validIndexes.add(i) }

        return validIndexes.toIntArray()
    }

    fun createRMatrix() {
        val edgeList = ArrayList<Edge>()
        for (row in 0..grid.numCells - 1) {
            for (column in 0..grid.numCells - 1) {
                //Up
                createEdge(Coordinate(row, column), Coordinate(row - 1, column))?.let { edgeList.add(it) }
                //Down
                createEdge(Coordinate(row, column), Coordinate(row + 1, column))?.let { edgeList.add(it) }
                //Left
                createEdge(Coordinate(row, column), Coordinate(row, column - 1))?.let { edgeList.add(it) }
                //Right
                createEdge(Coordinate(row, column), Coordinate(row, column + 1))?.let { edgeList.add(it) }
            }
        }
        //Add recursive edge(s) TODO
        //createEdge(grid.rewardGridLocation.x, grid.rewardGridLocation.y, grid.rewardGridLocation.x, grid.rewardGridLocation.y)?.let { edgeList.add(it) }

        //Add edges
        for ((first, second, weight) in edgeList) {
            RMatrix[first.toMatrixIndex()][second.toMatrixIndex()] = weight
        }
    }

    private fun createEdge(first: Coordinate, second: Coordinate): Edge? {
        if(second.row in 0..grid.numCells - 1 && second.column in 0..grid.numCells - 1) {
            val weight = grid.cells[second.row][second.column]
            return Edge(first, second, weight)
        }
        return null
    }


}
