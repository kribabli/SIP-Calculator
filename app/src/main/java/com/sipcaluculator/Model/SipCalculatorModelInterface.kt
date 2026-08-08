package com.sipcaluculator.Model

interface SipCalculatorModelInterface {
    fun getTotalInvestedAmount(): Long
    fun getEstimatedReturns(): Long
    fun getTotalValue(): Long
    fun getYearlyBreakdown(): List<YearlyBreakdown>
}
