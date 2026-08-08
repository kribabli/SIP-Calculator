package com.sipcaluculator.View.Fragments

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import com.sipcaluculator.Data.ScenarioFormatter
import com.sipcaluculator.Data.ScenarioRepository
import com.sipcaluculator.Model.CalculatorType
import com.sipcaluculator.Model.SavedScenario
import com.sipcaluculator.Model.YearlyBreakdown
import com.sipcaluculator.Presenter.LanguageChange
import com.sipcaluculator.Presenter.LocaleHelper
import com.sipcaluculator.Presenter.SipCalculatorPresenter
import com.sipcaluculator.R
import com.sipcaluculator.Presenter.SipCalculatorViewInterface
import com.sipcaluculator.View.Adapters.YearlyBreakdownAdapter
import com.sipcaluculator.View.Widgets.GrowthChartView
import org.eazegraph.lib.models.PieModel
import java.text.NumberFormat
import java.util.Locale

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private lateinit var totalInvestedAmountMaterialHeading: MaterialTextView
private lateinit var totalInvestedAmountMaterialTextView: MaterialTextView
private lateinit var estimatedReturnsMaterialHeading: MaterialTextView
private lateinit var estimatedReturnsMaterialTextView: MaterialTextView
private lateinit var totalAmountMaterialHeading: MaterialTextView
private lateinit var totalAmountMaterialTextView: MaterialTextView

private lateinit var monthlyInvestmentAmountTextInputEditText: TextInputEditText
private lateinit var expectedReturnRateTextInputEditText: TextInputEditText
private lateinit var investmentTimePeriodTextInputEditText: TextInputEditText
private lateinit var monthlyInvestmentAmountTextInputLayout: TextInputLayout
private lateinit var expectedReturnRateTextInputLayout: TextInputLayout
private lateinit var investmentTimePeriodTextInputLayout: TextInputLayout
private lateinit var sipCalculatorTitle: TextView

private lateinit var sipCalculateResultButton: Button
private lateinit var monthlyInvestmentAmount: String
private lateinit var expectedReturnRate: String
private lateinit var investmentTimePeriod: String

private lateinit var monthlyInvestmentAmountSlider: Slider
private lateinit var expectedReturnRateSlider: Slider
private lateinit var investmentTimePeriodSlider: Slider
private lateinit var sipResultPieChart: org.eazegraph.lib.charts.PieChart

private lateinit var sipBreakdownHeading: MaterialTextView
private lateinit var sipGrowthChartView: GrowthChartView
private lateinit var sipBreakdownRecyclerView: RecyclerView
private lateinit var sipBreakdownDetailTextView: MaterialTextView
private lateinit var sipSaveButton: Button
private lateinit var sipShareButton: Button
private val sipBreakdownAdapter = YearlyBreakdownAdapter()
private var sipLastSelectedBreakdown: YearlyBreakdown? = null
private var sipLastInvested: Double = 0.0
private var sipLastReturns: Double = 0.0
private var sipLastTotal: Double = 0.0
private var sipLastBreakdownList: List<YearlyBreakdown> = emptyList()

class SIPFragment : Fragment(), SipCalculatorViewInterface, LanguageChange {
    private var param1: String? = null
    private var param2: String? = null
    private var resources: Resources? = null
    private var context: Context? = null

    override fun onLanguageUpdate(languageCode: String) {
        context = LocaleHelper.setLocale(requireContext(), languageCode)
        resources = context?.resources

        onLanguageChange(languageCode)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_s_i_p, container, false)

        totalInvestedAmountMaterialHeading =
            view.findViewById(R.id.totalInvestedAmountMaterialHeading)
        totalInvestedAmountMaterialTextView =
            view.findViewById(R.id.totalInvestedAmountMaterialTextView)
        estimatedReturnsMaterialHeading = view.findViewById(R.id.estimatedReturnsMaterialHeading)
        estimatedReturnsMaterialTextView = view.findViewById(R.id.estimatedReturnsMaterialTextView)
        totalAmountMaterialHeading = view.findViewById(R.id.totalAmountMaterialHeading)
        totalAmountMaterialTextView = view.findViewById(R.id.totalAmountMaterialTextView)
        monthlyInvestmentAmountTextInputEditText =
            view.findViewById(R.id.monthlyInvestmentAmountTextInputEditText)
        expectedReturnRateTextInputEditText =
            view.findViewById(R.id.expectedReturnRateTextInputEditText)
        investmentTimePeriodTextInputEditText =
            view.findViewById(R.id.investmentTimePeriodTextInputEditText)
        monthlyInvestmentAmountTextInputLayout =
            view.findViewById(R.id.monthlyInvestmentAmountTextInputLayout)
        expectedReturnRateTextInputLayout =
            view.findViewById(R.id.expectedReturnRateTextInputLayout)
        investmentTimePeriodTextInputLayout =
            view.findViewById(R.id.investmentTimePeriodTextInputLayout)
        sipCalculateResultButton = view.findViewById(R.id.sipCalculateResultButton)
        sipResultPieChart = view.findViewById(R.id.sipResultPieChart)
        monthlyInvestmentAmountSlider = view.findViewById(R.id.monthlyInvestmentAmountSlider)
        expectedReturnRateSlider = view.findViewById(R.id.expectedReturnRateSlider)
        investmentTimePeriodSlider = view.findViewById(R.id.investmentTimePeriodSlider)
        sipCalculatorTitle = view.findViewById(R.id.sipCalculatorTitle)

        sipBreakdownHeading = view.findViewById(R.id.sipBreakdownHeading)
        sipGrowthChartView = view.findViewById(R.id.sipGrowthChartView)
        sipBreakdownRecyclerView = view.findViewById(R.id.sipBreakdownRecyclerView)
        sipBreakdownDetailTextView = view.findViewById(R.id.sipBreakdownDetailTextView)
        sipSaveButton = view.findViewById(R.id.sipSaveButton)
        sipShareButton = view.findViewById(R.id.sipShareButton)
        sipBreakdownRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        sipBreakdownRecyclerView.adapter = sipBreakdownAdapter
        sipGrowthChartView.onPointSelected = { entry ->
            sipLastSelectedBreakdown = entry
            showSipBreakdownDetail(entry)
        }

        sipSaveButton.setOnClickListener {
            ScenarioRepository.save(requireContext(), buildCurrentSipScenario())
            Toast.makeText(
                requireContext(),
                getString(R.string.scenario_saved),
                Toast.LENGTH_SHORT
            ).show()
        }

        sipShareButton.setOnClickListener {
            val shareText = ScenarioFormatter.buildShareText(buildCurrentSipScenario())
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            startActivity(Intent.createChooser(intent, getString(R.string.share)))
        }

        context = LocaleHelper.setLocale(requireContext(), "en")

        monthlyInvestmentAmountTextInputEditText.setText("5000")
        expectedReturnRateTextInputEditText.setText("14")
        investmentTimePeriodTextInputEditText.setText("10")

        monthlyInvestmentAmountSlider.value = 5000f
        expectedReturnRateSlider.value = 14f
        investmentTimePeriodSlider.value = 10f

        val sipPresenter = SipCalculatorPresenter(this)
        sipPresenter.forCalculation(
            "5000",
            "14",
            "10"
        )

        monthlyInvestmentAmountSlider.addOnChangeListener { _, value, _ ->
            monthlyInvestmentAmountTextInputEditText.setText(value.toInt().toString())
        }
        expectedReturnRateSlider.addOnChangeListener { _, value, _ ->
            expectedReturnRateTextInputEditText.setText(value.toInt().toString())
        }
        investmentTimePeriodSlider.addOnChangeListener { _, value, _ ->
            investmentTimePeriodTextInputEditText.setText(value.toInt().toString())
        }

        sipCalculateResultButton.setOnClickListener {
            monthlyInvestmentAmount = monthlyInvestmentAmountTextInputEditText.text.toString()
            expectedReturnRate = expectedReturnRateTextInputEditText.text.toString()
            investmentTimePeriod = investmentTimePeriodTextInputEditText.text.toString()

            if (checkAllFields()) {
                sipPresenter.forCalculation(
                    monthlyInvestmentAmount,
                    expectedReturnRate,
                    investmentTimePeriod
                )
            }
        }
        return view
    }

    override fun onCalculationResult(
        totalInvestedAmount: String,
        estimatedReturns: String,
        totalValue: String
    ) {
        try {
            val currencyFormatter: NumberFormat =
                NumberFormat.getCurrencyInstance(Locale("en", "IN"))

            val totalInvestedAmountFormatted: String =
                currencyFormatter.format(totalInvestedAmount.toLongOrNull() ?: 0)
            val estimatedReturnsFormatted: String =
                currencyFormatter.format(estimatedReturns.toLongOrNull() ?: 0)
            val totalValueFormatted: String =
                currencyFormatter.format(totalValue.toLongOrNull() ?: 0)

            sipLastInvested = totalInvestedAmount.toDoubleOrNull() ?: 0.0
            sipLastReturns = estimatedReturns.toDoubleOrNull() ?: 0.0
            sipLastTotal = totalValue.toDoubleOrNull() ?: 0.0

            totalInvestedAmountMaterialTextView.text = totalInvestedAmountFormatted
            estimatedReturnsMaterialTextView.text = estimatedReturnsFormatted
            totalAmountMaterialTextView.text = totalValueFormatted

            sipResultPieChart.clearAnimation()
            sipResultPieChart.clearChart()

            val investedLabel = resources?.getString(R.string.invested_amount) ?: "Invested"
            val returnsLabel = resources?.getString(R.string.estimated_returns) ?: "Returns"

            val investedAmount = totalInvestedAmount.toFloatOrNull() ?: 0f
            val returnsAmount = totalValue.toFloatOrNull()?.minus(investedAmount)

            sipResultPieChart.clearChart()
            sipResultPieChart.addPieSlice(
                PieModel(
                    investedLabel,
                    investedAmount,
                    ContextCompat.getColor(requireContext(), R.color.investedAmt)
                )
            )

            if (returnsAmount != null) {
                sipResultPieChart.addPieSlice(
                    PieModel(
                        returnsLabel,
                        returnsAmount.coerceAtLeast(0f),
                        ContextCompat.getColor(requireContext(), R.color.estimatedReturns1)
                    )
                )
            }

            sipResultPieChart.startAnimation()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error updating chart", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onYearlyBreakdownResult(breakdown: List<YearlyBreakdown>) {
        sipLastBreakdownList = breakdown
        sipGrowthChartView.setData(breakdown)
        sipBreakdownAdapter.submitList(breakdown)
        if (breakdown.isEmpty()) {
            sipLastSelectedBreakdown = null
            sipBreakdownDetailTextView.text = ""
        }
    }

    private fun showSipBreakdownDetail(entry: YearlyBreakdown) {
        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        // Uses the fragment's own getString (always available once attached) instead of the
        // nullable `resources` field, which stays null until a language button is tapped —
        // that was making this text invisible on first app open.
        sipBreakdownDetailTextView.text = getString(
            R.string.breakdown_detail_format,
            entry.year,
            currencyFormatter.format(entry.investedAmount),
            currencyFormatter.format(entry.returns),
            currencyFormatter.format(entry.totalValue)
        )
    }

    private fun buildCurrentSipScenario(): SavedScenario {
        val amount = monthlyInvestmentAmountTextInputEditText.text.toString()
        val rate = expectedReturnRateTextInputEditText.text.toString()
        val years = investmentTimePeriodTextInputEditText.text.toString()

        val inputs = linkedMapOf(
            "Monthly Investment" to "₹$amount",
            "Expected Return Rate" to "$rate%",
            "Investment Period" to "$years years"
        )

        return SavedScenario(
            id = System.nanoTime(),
            calculatorType = CalculatorType.SIP,
            label = ScenarioFormatter.defaultLabel(CalculatorType.SIP.displayName),
            createdAtMillis = System.currentTimeMillis(),
            inputs = inputs,
            investedAmount = sipLastInvested,
            returns = sipLastReturns,
            totalValue = sipLastTotal,
            yearlyBreakdown = sipLastBreakdownList
        )
    }

    private fun checkAllFields(): Boolean {
        if (monthlyInvestmentAmount.isEmpty()) {
            monthlyInvestmentAmountTextInputEditText.error = "Can't be empty"
            return false
        }
        if (expectedReturnRate.isEmpty()) {
            expectedReturnRateTextInputEditText.error = "Can't be empty"
            return false
        }
        if (investmentTimePeriod.isEmpty()) {
            investmentTimePeriodTextInputEditText.error = "Can't be empty"
            return false
        }
        return true
    }

    private fun onLanguageChange(s: String) {
        monthlyInvestmentAmountTextInputLayout.hint =
            resources?.getString(R.string.monthly_investment_amount_in_rs)
        expectedReturnRateTextInputLayout.hint =
            resources?.getString(R.string.expected_rate_of_return_in)
        investmentTimePeriodTextInputLayout.hint =
            resources?.getString(R.string.investment_time_period_in_years)
        sipCalculateResultButton.text = resources?.getString(R.string.calculate)
        totalInvestedAmountMaterialHeading.text = resources?.getString(R.string.invested_amount)
        estimatedReturnsMaterialHeading.text = resources?.getString(R.string.estimated_returns)
        totalAmountMaterialHeading.text = resources?.getString(R.string.total_amount)
        sipCalculatorTitle.text = resources?.getString(R.string.app_name)
        sipBreakdownHeading.text = resources?.getString(R.string.yearly_breakdown)
        sipLastSelectedBreakdown?.let { showSipBreakdownDetail(it) }
    }
}