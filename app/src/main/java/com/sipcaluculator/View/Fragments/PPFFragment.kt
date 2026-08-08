package com.sipcaluculator.View.Fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
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
import com.sipcaluculator.R
import com.sipcaluculator.View.Adapters.YearlyBreakdownAdapter
import com.sipcaluculator.View.Widgets.GrowthChartView
import org.eazegraph.lib.models.PieModel
import java.text.NumberFormat
import java.util.Locale

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private lateinit var investedAmountMaterialHeading: MaterialTextView
private lateinit var investedAmountMaterialTextView: MaterialTextView
private lateinit var maturityAmountMaterialHeading: MaterialTextView
private lateinit var maturityAmountTextView: MaterialTextView
private lateinit var totalInterestMaterialHeading: MaterialTextView
private lateinit var totalInterestMaterialTextView: MaterialTextView

private lateinit var annualInvestmentTextInputEditText: TextInputEditText
private lateinit var tenureYearsTextInputEditText: TextInputEditText
private lateinit var interestRateTextInputEditText: TextInputEditText

private lateinit var annualInvestmentTextInputLayout: TextInputLayout
private lateinit var tenureYearsTextInputLayout: TextInputLayout
private lateinit var interestRateTextInputLayout: TextInputLayout

private lateinit var ppfButton: Button

private lateinit var annualInvestmentSlider: Slider
private lateinit var tenureYearsSlider: Slider
private lateinit var interestRateSlider: Slider
private lateinit var ppfResultPieChart: org.eazegraph.lib.charts.PieChart

private lateinit var ppfBreakdownHeading: MaterialTextView
private lateinit var ppfGrowthChartView: GrowthChartView
private lateinit var ppfBreakdownRecyclerView: RecyclerView
private lateinit var ppfBreakdownDetailTextView: MaterialTextView
private lateinit var ppfSaveButton: Button
private lateinit var ppfShareButton: Button
private val ppfBreakdownAdapter = YearlyBreakdownAdapter()
private var ppfLastSelectedBreakdown: YearlyBreakdown? = null
private var ppfLastInvested: Double = 0.0
private var ppfLastReturns: Double = 0.0
private var ppfLastTotal: Double = 0.0
private var ppfLastBreakdownList: List<YearlyBreakdown> = emptyList()

class PPFFragment : Fragment(), LanguageChange {
    private var param1: String? = null
    private var param2: String? = null
    private var resources: Resources? = null
    private var context: Context? = null
    private lateinit var ppfCalculatorTitle: TextView

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

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_p_p_f, container, false)

        investedAmountMaterialHeading = view.findViewById(R.id.investedAmountMaterialHeading)
        investedAmountMaterialTextView = view.findViewById(R.id.investedAmountMaterialTextView)
        maturityAmountMaterialHeading = view.findViewById(R.id.maturityAmountMaterialHeading)
        maturityAmountTextView = view.findViewById(R.id.maturityAmountTextView)
        totalInterestMaterialHeading = view.findViewById(R.id.totalInterestMaterialHeading)
        totalInterestMaterialTextView = view.findViewById(R.id.totalInterestMaterialTextView)

        annualInvestmentTextInputEditText =
            view.findViewById(R.id.annualInvestmentTextInputEditText)
        tenureYearsTextInputEditText = view.findViewById(R.id.tenureYearsTextInputEditText)
        interestRateTextInputEditText = view.findViewById(R.id.interestRateTextInputEditText)
        annualInvestmentTextInputLayout =
            view.findViewById(R.id.annualInvestmentTextInputLayout)
        tenureYearsTextInputLayout = view.findViewById(R.id.tenureYearsTextInputLayout)
        interestRateTextInputLayout = view.findViewById(R.id.interestRateTextInputLayout)
        ppfButton = view.findViewById(R.id.ppfButton)

        annualInvestmentSlider = view.findViewById(R.id.annualInvestmentSlider)
        tenureYearsSlider = view.findViewById(R.id.tenureYearsSlider)
        interestRateSlider = view.findViewById(R.id.interestRateSlider)
        ppfResultPieChart = view.findViewById(R.id.ppfResultPieChart)
        ppfCalculatorTitle = view.findViewById(R.id.ppfCalculatorTitle)

        ppfBreakdownHeading = view.findViewById(R.id.ppfBreakdownHeading)
        ppfGrowthChartView = view.findViewById(R.id.ppfGrowthChartView)
        ppfBreakdownRecyclerView = view.findViewById(R.id.ppfBreakdownRecyclerView)
        ppfBreakdownDetailTextView = view.findViewById(R.id.ppfBreakdownDetailTextView)
        ppfSaveButton = view.findViewById(R.id.ppfSaveButton)
        ppfShareButton = view.findViewById(R.id.ppfShareButton)
        ppfBreakdownRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        ppfBreakdownRecyclerView.adapter = ppfBreakdownAdapter
        ppfGrowthChartView.onPointSelected = { entry ->
            ppfLastSelectedBreakdown = entry
            showPpfBreakdownDetail(entry)
        }

        ppfSaveButton.setOnClickListener {
            ScenarioRepository.save(requireContext(), buildCurrentPpfScenario())
            Toast.makeText(
                requireContext(),
                getString(R.string.scenario_saved),
                Toast.LENGTH_SHORT
            ).show()
        }

        ppfShareButton.setOnClickListener {
            val shareText = ScenarioFormatter.buildShareText(buildCurrentPpfScenario())
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            startActivity(Intent.createChooser(intent, getString(R.string.share)))
        }

        context = LocaleHelper.setLocale(requireContext(), "en")

        annualInvestmentTextInputEditText.setText("50000")
        tenureYearsTextInputEditText.setText("18")
        interestRateTextInputEditText.setText("7.1")

        annualInvestmentSlider.value = 50000f
        tenureYearsSlider.value = 18f
        interestRateSlider.value = 7.1f

        annualInvestmentSlider.addOnChangeListener { _, value, _ ->
            annualInvestmentTextInputEditText.setText(value.toInt().toString())
        }
        tenureYearsSlider.addOnChangeListener { _, value, _ ->
            tenureYearsTextInputEditText.setText(value.toInt().toString())
        }
        interestRateSlider.addOnChangeListener { _, value, _ ->
            interestRateTextInputEditText.setText(value.toInt().toString())
        }

        calculatePPF()

        ppfButton.setOnClickListener {
            if (validateInputs()) {
                calculatePPF()
            }
        }
        return view
    }


    private fun validateInputs(): Boolean {
        val annualInvestment = annualInvestmentTextInputEditText.text.toString()
        val tenureYears = tenureYearsTextInputEditText.text.toString()
        val interestRate = interestRateTextInputEditText.text.toString()

        if (annualInvestment.isEmpty() || tenureYears.isEmpty() || interestRate.isEmpty()) {
            Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun updatePieChart(contributions: Double, maturityAmount: Double, interest: Double) {
        ppfResultPieChart.clearChart()
        ppfResultPieChart.addPieSlice(
            PieModel(
                "Invested Amount",
                contributions.toFloat(),
                Color.parseColor("#AFDC8F")
            )
        )
        ppfResultPieChart.addPieSlice(
            PieModel(
                "Maturity Amount",
                maturityAmount.toFloat(),
                Color.parseColor("#FFA726")
            )
        )
        ppfResultPieChart.addPieSlice(
            PieModel(
                "Total Interest",
                interest.toFloat(),
                Color.parseColor("#204D00")
            )
        )
        ppfResultPieChart.startAnimation()
    }

    private fun calculatePPF() {
        val annualInvestment =
            annualInvestmentTextInputEditText.text.toString().toDoubleOrNull() ?: 0.0
        val tenureYears = tenureYearsTextInputEditText.text.toString().toIntOrNull() ?: 0
        val interestRate = interestRateTextInputEditText.text.toString().toDoubleOrNull() ?: 0.0

        if (annualInvestment <= 0 || tenureYears <= 0 || interestRate <= 0) {
            Toast.makeText(context, "Please enter valid inputs", Toast.LENGTH_SHORT).show()
            return
        }

        val rate = interestRate / 100
        val maturityAmount =
            annualInvestment * ((Math.pow(1 + rate, tenureYears.toDouble()) - 1) / rate)
        val totalContributions = annualInvestment * tenureYears
        val totalInterest = maturityAmount - totalContributions

        investedAmountMaterialTextView.text = String.format("₹%.2f", totalContributions)
        totalInterestMaterialTextView.text = String.format("₹%.2f", totalInterest)
        maturityAmountTextView.text = String.format("₹%.2f", maturityAmount)

        updatePieChart(totalContributions, maturityAmount, totalInterest)

        ppfLastInvested = totalContributions
        ppfLastReturns = totalInterest
        ppfLastTotal = maturityAmount

        val breakdown = calculateYearlyBreakdown(annualInvestment, interestRate, tenureYears)
        ppfLastBreakdownList = breakdown
        ppfGrowthChartView.setData(breakdown)
        ppfBreakdownAdapter.submitList(breakdown)
        if (breakdown.isEmpty()) {
            ppfLastSelectedBreakdown = null
            ppfBreakdownDetailTextView.text = ""
        }
    }

    private fun calculateYearlyBreakdown(
        annualInvestment: Double,
        interestRate: Double,
        tenureYears: Int
    ): List<YearlyBreakdown> {
        val rate = interestRate / 100
        return (1..tenureYears).map { year ->
            val invested = annualInvestment * year
            val totalValue = annualInvestment * ((Math.pow(1 + rate, year.toDouble()) - 1) / rate)
            YearlyBreakdown(year, invested, totalValue)
        }
    }

    private fun showPpfBreakdownDetail(entry: YearlyBreakdown) {
        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        // Uses the fragment's own getString (always available once attached) instead of the
        // nullable `resources` field, which stays null until a language button is tapped —
        // that was making this text invisible on first app open.
        ppfBreakdownDetailTextView.text = getString(
            R.string.breakdown_detail_format,
            entry.year,
            currencyFormatter.format(entry.investedAmount),
            currencyFormatter.format(entry.returns),
            currencyFormatter.format(entry.totalValue)
        )
    }

    private fun buildCurrentPpfScenario(): SavedScenario {
        val annualInvestment = annualInvestmentTextInputEditText.text.toString()
        val tenureYears = tenureYearsTextInputEditText.text.toString()
        val interestRate = interestRateTextInputEditText.text.toString()

        val inputs = linkedMapOf(
            "Annual Investment" to "₹$annualInvestment",
            "Tenure" to "$tenureYears years",
            "Interest Rate" to "$interestRate%"
        )

        return SavedScenario(
            id = System.nanoTime(),
            calculatorType = CalculatorType.PPF,
            label = ScenarioFormatter.defaultLabel(CalculatorType.PPF.displayName),
            createdAtMillis = System.currentTimeMillis(),
            inputs = inputs,
            investedAmount = ppfLastInvested,
            returns = ppfLastReturns,
            totalValue = ppfLastTotal,
            yearlyBreakdown = ppfLastBreakdownList
        )
    }

    private fun onLanguageChange(s: String) {
        investedAmountMaterialHeading.text = resources?.getString(R.string.invested_Amount)
        maturityAmountMaterialHeading.text = resources?.getString(R.string.maturity_Amount)
        totalInterestMaterialHeading.text = resources?.getString(R.string.total_Interest)
        ppfButton.text = resources?.getString(R.string.calculate)
        annualInvestmentTextInputLayout.hint = resources?.getString(R.string.annual_Investment)
        tenureYearsTextInputLayout.hint = resources?.getString(R.string.tenure_years)
        interestRateTextInputLayout.hint = resources?.getString(R.string.interest_Rate)
        ppfCalculatorTitle.text = resources?.getString(R.string.ppf_Calculator)
        ppfBreakdownHeading.text = resources?.getString(R.string.yearly_breakdown)
        ppfLastSelectedBreakdown?.let { showPpfBreakdownDetail(it) }
    }
}