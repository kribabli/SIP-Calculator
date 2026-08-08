package com.sipcaluculator.Model

/**
 * One row of a year-by-year investment growth breakdown.
 *
 * @param year year number, 1-indexed (Year 1, Year 2, ...)
 * @param investedAmount cumulative amount invested by the end of this year
 * @param totalValue cumulative corpus value by the end of this year
 */
data class YearlyBreakdown(
    val year: Int,
    val investedAmount: Double,
    val totalValue: Double
) {
    val returns: Double
        get() = totalValue - investedAmount
}
