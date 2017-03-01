package com.pixis.model

import com.pixis.Settings

data class Coordinate(val row: Int, val column: Int) {

    fun isValid(): Boolean {
        return row in 0..(Settings.NUM_CELLS - 1) && column in 0..(Settings.NUM_CELLS - 1)
    }

    companion object {
        fun toMatrixIndex(coordinate: Coordinate): Int {
            return coordinate.row + Settings.NUM_CELLS * coordinate.column
        }

        fun fromMatrixIndex(matrixIndex: Int): Coordinate {
            return Coordinate(row =  matrixIndex % Settings.NUM_CELLS,
                    column = matrixIndex / Settings.NUM_CELLS)
        }
    }
}

fun Coordinate.toMatrixIndex(): Int {
    return Coordinate.toMatrixIndex(this)
}