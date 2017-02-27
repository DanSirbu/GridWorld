package com.pixis.testing

import com.pixis.Grid
import com.pixis.QLearning
import org.junit.Test
import org.junit.runners.JUnit4

import java.awt.*

class QLearningTest {
    @Test
    fun TestGridToMatrix() {
        val rewardPoint = Point(1, 2) //H
        val expectedRowPoint = 7

        // A D G
        // B E H
        // C F I
        val grid = Grid(3, rewardPoint)
        val qLearning = QLearning(grid)

        val rowPoint = qLearning.gridPointToMatrix(rewardPoint)

        assert(rowPoint == expectedRowPoint, { "Grid to matrix not working" })
    }
}
