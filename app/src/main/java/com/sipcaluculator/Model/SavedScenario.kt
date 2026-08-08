package com.sipcaluculator.Model

/**
 * A user-saved snapshot of one calculation: its inputs, headline results, and
 * full yearly breakdown, so it can be listed in History and compared later
 * without re-running the calculator.
 */
data class SavedScenario(
    val id: Long,
    val calculatorType: CalculatorType,
    val label: String,
    val createdAtMillis: Long,
    val inputs: Map<String, String>,
    val investedAmount: Double,
    val returns: Double,
    val totalValue: Double,
    val yearlyBreakdown: List<YearlyBreakdown>
)
