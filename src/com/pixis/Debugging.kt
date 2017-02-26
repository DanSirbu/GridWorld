package com.pixis

import processing.core.PApplet

/**
 * Created by Dan on 2/26/2017.
 */
object Debugging {

    fun printEdges(edgeList: List<Edge>) {
        for ((source, target, weight) in edgeList) {
            PApplet.println("[$source, $target] = $weight")
        }
    }

    fun printMatrix(matrix: Array<IntArray>, matrixSize: Int) {
        printlines(3)

        val a: Char = 65.toChar()
        //Print header
        PApplet.print("  ")
        for (y in 0..matrixSize - 1) {
            PApplet.print((a.toInt() + y).toChar() + " ")
        }
        PApplet.println()
        //Print
        for (x in 0..matrixSize - 1) {
            PApplet.print((a.toInt() + x).toChar() + " ") //Row header
            for (y in 0..matrixSize - 1) {
                PApplet.print(matrix[x][y].toString() + " ")
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
