package com.sipcaluculator.Presenter

import com.sipcaluculator.Model.YearlyBreakdown

interface SipCalculatorViewInterface {
    fun onCalculationResult(
        totalInvestedAmount: String,
        estimatedReturns: String,
        totalValue: String
    )

    fun onYearlyBreakdownResult(breakdown: List<YearlyBreakdown>)
}