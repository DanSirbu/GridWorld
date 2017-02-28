package com.pixis

import com.pixis.model.Coordinate
import com.pixis.model.Edge
import com.pixis.model.toMatrixIndex
import java.awt.Point
import java.util.*

class QLearning(val grid: Grid, val trainingDelay: Int = 100, val trainedDelay: Int = 500) {
    private var iteration = 0

    val rewardRow = grid.rewardGridLocation.toMatrixIndex()

    val matrixSize = grid.numCells * grid.numCells

    var s_t: Int = 1 //s_t = currentState
    var Paused = false
    var isLearning: Boolean = true

    var RMatrix = Array(matrixSize) { IntArray(matrixSize) }
    var QMatrix = Array(matrixSize) { IntArray(matrixSize) }

    //For Q Update
    /*val random = Random(10) //Use seed to be able to compare results
    var lastUpdateTime = System.currentTimeMillis()*/

    init {
        //Initialize QMatrix
        QMatrix.forEach { it.fill(0) }

        //Initialize Reward matrix to -1 (no edge)
        RMatrix.forEach { it.fill(NO_EDGE) }
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

    /*fun QIteration() {
        if(Paused || (System.currentTimeMillis() - lastUpdateTime < trainedDelay))
            return

        lastUpdateTime = System.currentTimeMillis()

        //Restart q search if at the end
        if(s_t == rewardRow)
            restart()

        val nextTargets = getNextStatesIndexes(s_t)
        val highestRewardIndex: Int = nextTargets.map { Pair(it, RMatrix[s_t][it]) /* [rewardIndex, rewardValue] */ }.maxBy { it.second }!!.first

        s_t = highestRewardIndex
    }*/


    /*private fun restart() {
        s_t = random.nextInt(matrixSize) //note nextInt is exclusive
        println("Restarting")
    }*/

    //Q(st,at)←Q(st,at)+α[rt+γmaxaQ(st+1,a)−Q(st,at)]
    fun QLearningIteration(a_t: Int): Int {
        /*if(Paused || (System.currentTimeMillis() - lastUpdateTime < trainingDelay))
            return*/

        //Restart q search
        //if(s_t == rewardRow) restart()

        //lastUpdateTime = System.currentTimeMillis()

        //val nextStatesIndexes = getNextStatesIndexes(s_t)
        //val a_t = nextStatesIndexes[random.nextInt(nextStatesIndexes.size)]


        val nextTargets = getNextStatesIndexes(a_t)
        val maxNextReward: Int = nextTargets.map { RMatrix[a_t][it] }.max() ?: 0

        QMatrix[s_t][a_t] = (QMatrix[s_t][a_t] + alpha * (RMatrix[s_t][a_t] + gamma * maxNextReward - QMatrix[s_t][a_t])).toInt()
        //println((++iteration).toString() + ": " + (65 + s_t).toChar() + "-" + (65 + a_t).toChar() + " = " + QMatrix[s_t][a_t])

        val updatedQValue = QMatrix[s_t][a_t]
        s_t = a_t
        return updatedQValue
    }

    fun getNextStatesIndexes(rowIndex: Int): IntArray {
        val validIndexes = ArrayList<Int>()
        val row: IntArray = RMatrix[rowIndex]

        row.forEachIndexed { i, item ->  if(item != -1) validIndexes.add(i) }

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
