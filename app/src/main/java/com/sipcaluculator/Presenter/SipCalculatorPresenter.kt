package com.sipcaluculator.Presenter

import com.sipcaluculator.Model.SipCalculatorModel

class SipCalculatorPresenter(
    private val sipCalculatorViewInterface: SipCalculatorViewInterface
) : SipCalculatorPresenterInterface {

    override fun forCalculation(
        monthlyInvestmentAmount: String,
        expectedReturnRate: String,
        investmentTimePeriod: String
    ) {
        val sipModel = SipCalculatorModel(
            monthlyInvestmentAmount,
            expectedReturnRate,
            investmentTimePeriod
        )

        sipCalculatorViewInterface.onCalculationResult(
            sipModel.getTotalInvestedAmount().toString(),
            sipModel.getEstimatedReturns().toString(),
            sipModel.getTotalValue().toString()
        )

        sipCalculatorViewInterface.onYearlyBreakdownResult(sipModel.getYearlyBreakdown())
    }
}
