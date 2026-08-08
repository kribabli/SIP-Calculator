package com.sipcaluculator.Model

import android.util.Log

class SipCalculatorModel(
    monthlyInvestmentAmount: String,
    expectedReturnRate: String,
    investmentTimePeriod: String
) : SipCalculatorModelInterface {

    val TAG = SipCalculatorModel::class.java.simpleName

    private var monthlyInvestmentAmountInt: Int = monthlyInvestmentAmount.toInt()
    private var expectedReturnRateInt: Int = expectedReturnRate.toInt()
    private var investmentTimePeriodInt: Int = investmentTimePeriod.toInt() * 12

    override fun getTotalInvestedAmount(): Long {
        return (monthlyInvestmentAmountInt * investmentTimePeriodInt).toLong()
        Log.d(
            "Babli",
            "getTotalInvestedAmount: $monthlyInvestmentAmountInt  $investmentTimePeriodInt"
        )
    }

    override fun getEstimatedReturns(): Long {
        return getTotalValue() - getTotalInvestedAmount()
        Log.d("Babli", "getEstimatedReturns: $monthlyInvestmentAmountInt  $investmentTimePeriodInt")
    }

    override fun getTotalValue(): Long {
        val periodicInterest: Float = ((expectedReturnRateInt.toFloat() / 12) / 100)

        return (monthlyInvestmentAmountInt * (((Math.pow(
            (1 + periodicInterest).toDouble(),
            investmentTimePeriodInt.toDouble()
        )
                - 1) / periodicInterest) * (1 + periodicInterest)))
            .toLong()

        Log.d(
            "Babli",
            "getTotalValue: $monthlyInvestmentAmountInt  $investmentTimePeriodInt $periodicInterest"
        )
    }

    override fun getYearlyBreakdown(): List<YearlyBreakdown> {
        val periodicInterest: Double = ((expectedReturnRateInt.toDouble() / 12) / 100)
        val totalYears = investmentTimePeriodInt / 12
        val breakdown = mutableListOf<YearlyBreakdown>()

        for (year in 1..totalYears) {
            val months = year * 12
            val invested = monthlyInvestmentAmountInt.toDouble() * months
            val totalValue = if (periodicInterest == 0.0) {
                invested
            } else {
                monthlyInvestmentAmountInt * (((Math.pow(1 + periodicInterest, months.toDouble()) - 1) / periodicInterest) * (1 + periodicInterest))
            }
            breakdown.add(YearlyBreakdown(year, invested, totalValue))
        }

        return breakdown
    }
}
