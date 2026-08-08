package com.sipcaluculator.Data

import com.sipcaluculator.Model.SavedScenario
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Builds the shareable text summary for a scenario, and its default label. */
object ScenarioFormatter {

    private fun currencyFormatter(): NumberFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    fun defaultLabel(calculatorDisplayName: String): String {
        val formatter = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        return "$calculatorDisplayName - ${formatter.format(Date())}"
    }

    fun buildShareText(scenario: SavedScenario): String {
        val cf = currencyFormatter()
        val sb = StringBuilder()

        sb.appendLine("${scenario.calculatorType.displayName} Calculation")
        sb.appendLine(scenario.label)
        sb.appendLine()

        scenario.inputs.forEach { (label, value) -> sb.appendLine("$label: $value") }
        sb.appendLine()

        sb.appendLine("Invested Amount: ${cf.format(scenario.investedAmount)}")
        sb.appendLine("Estimated Returns: ${cf.format(scenario.returns)}")
        sb.appendLine("Total Value: ${cf.format(scenario.totalValue)}")

        if (scenario.yearlyBreakdown.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("Year-wise breakdown:")
            scenario.yearlyBreakdown.forEach { entry ->
                sb.appendLine(
                    "Year ${entry.year}: Invested ${cf.format(entry.investedAmount)}, " +
                        "Returns ${cf.format(entry.returns)}, Total ${cf.format(entry.totalValue)}"
                )
            }
        }

        sb.appendLine()
        sb.append("Calculated using SIP Calculator app")

        return sb.toString()
    }
}
