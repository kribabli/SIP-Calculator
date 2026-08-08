package com.sipcaluculator.Presenter

interface SipCalculatorPresenterInterface {
    fun forCalculation(
        monthlyInvestmentAmount: String,
        expectedReturnRate: String,
        investmentTimePeriod: String
    )
}