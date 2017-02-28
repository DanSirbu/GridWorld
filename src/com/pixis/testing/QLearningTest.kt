package com.pixis.testing

import com.pixis.Grid
import com.pixis.QLearning
import com.pixis.model.Coordinate
import com.pixis.model.toMatrixIndex
import org.junit.Test
import org.junit.runners.JUnit4

import java.awt.*

class QLearningTest {
    @Test
    fun TestGridToMatrix() {
        val gridSize = 3

        val rewardPoint = Coordinate(row = 1, column = 2) //H
        val expectedMatrixIndex = 7

        // A D G
        // B E H
        // C F I
        val rowPoint = rewardPoint.toMatrixIndex()

        assert(rowPoint == expectedMatrixIndex, { "Grid to matrix not working" })
    }
}
