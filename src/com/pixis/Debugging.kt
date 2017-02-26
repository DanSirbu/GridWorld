package com.pixis

import processing.core.PApplet

object Debugging {

    @Suppress("unused")
    fun printEdges(edgeList: List<Edge>) {
        for ((source, target, weight) in edgeList) {
            PApplet.println("[$source, $target] = $weight")
        }
    }

    fun printMatrix(matrix: Array<IntArray>, matrixSize: Int) {
        printlines(3)

        val spacing = "%5s"

        val a: Char = 65.toChar()
        //Print header
        System.out.printf(spacing, ' ')
        for (y in 0..matrixSize - 1) {
            System.out.printf(spacing, (a.toInt() + y).toChar())
        }
        System.out.println()
        //Print
        for (x in 0..matrixSize - 1) {
            System.out.printf(spacing, (a.toInt() + x).toChar()) //Row header
            for (y in 0..matrixSize - 1) {
                System.out.printf(spacing, matrix[x][y].toString())
            }
            PApplet.println()
        }
    }

    fun printlines(numLines: Int) {
        for (x in 0..numLines - 1) {
            PApplet.println()
        }
    }

}
