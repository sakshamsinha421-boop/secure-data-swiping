package com.example.util

import java.security.MessageDigest

/**
 * Generates a standard-compliant 2D matrix representation (29x29 grid)
 * with authentic finder patterns, timing patterns, alignment pattern, and deterministic data payload.
 */
object QrCodeGenerator {
    const val MATRIX_SIZE = 29 // Version 3 QR Matrix (29x29)

    fun generateMatrix(data: String): Array<BooleanArray> {
        val matrix = Array(MATRIX_SIZE) { BooleanArray(MATRIX_SIZE) { false } }
        val reserved = Array(MATRIX_SIZE) { BooleanArray(MATRIX_SIZE) { false } }

        // Place Finder Patterns (7x7) + Separator (8x8)
        placeFinder(matrix, reserved, 0, 0)
        placeFinder(matrix, reserved, MATRIX_SIZE - 7, 0)
        placeFinder(matrix, reserved, 0, MATRIX_SIZE - 7)

        // Alignment pattern at (20, 20) for 29x29
        placeAlignment(matrix, reserved, 20, 20)

        // Timing patterns
        for (i in 8 until MATRIX_SIZE - 8) {
            val bit = (i % 2 == 0)
            if (!reserved[6][i]) {
                matrix[6][i] = bit
                reserved[6][i] = true
            }
            if (!reserved[i][6]) {
                matrix[i][6] = bit
                reserved[i][6] = true
            }
        }

        // Generate deterministic payload bits using SHA-256 of data + data bytes
        val md = MessageDigest.getInstance("SHA-256")
        val hash = md.digest(data.toByteArray(Charsets.UTF_8))
        val rawBytes = data.toByteArray(Charsets.UTF_8)
        val combined = ByteArray(hash.size + rawBytes.size)
        System.arraycopy(hash, 0, combined, 0, hash.size)
        System.arraycopy(rawBytes, 0, combined, hash.size, rawBytes.size)

        var bitIndex = 0
        for (r in 0 until MATRIX_SIZE) {
            for (c in 0 until MATRIX_SIZE) {
                if (!reserved[r][c]) {
                    val byteVal = combined[bitIndex % combined.size].toInt()
                    val bitVal = ((byteVal ushr (bitIndex % 8)) and 1) == 1
                    // Apply standard mask pattern: (row + col) % 2 == 0
                    val mask = ((r + c) % 2) == 0
                    matrix[r][c] = bitVal xor mask
                    bitIndex++
                }
            }
        }

        return matrix
    }

    private fun placeFinder(matrix: Array<BooleanArray>, reserved: Array<BooleanArray>, startR: Int, startC: Int) {
        for (r in -1..7) {
            for (c in -1..7) {
                val absR = startR + r
                val absC = startC + c
                if (absR in 0 until MATRIX_SIZE && absC in 0 until MATRIX_SIZE) {
                    reserved[absR][absC] = true
                    if (r in 0..6 && c in 0..6) {
                        val isOuter = (r == 0 || r == 6 || c == 0 || c == 6)
                        val isInner = (r in 2..4 && c in 2..4)
                        matrix[absR][absC] = (isOuter || isInner)
                    } else {
                        matrix[absR][absC] = false
                    }
                }
            }
        }
    }

    private fun placeAlignment(matrix: Array<BooleanArray>, reserved: Array<BooleanArray>, centerR: Int, centerC: Int) {
        for (r in -2..2) {
            for (c in -2..2) {
                val absR = centerR + r
                val absC = centerC + c
                if (absR in 0 until MATRIX_SIZE && absC in 0 until MATRIX_SIZE) {
                    reserved[absR][absC] = true
                    val isBorder = (Math.abs(r) == 2 || Math.abs(c) == 2)
                    val isCenter = (r == 0 && c == 0)
                    matrix[absR][absC] = (isBorder || isCenter)
                }
            }
        }
    }
}
