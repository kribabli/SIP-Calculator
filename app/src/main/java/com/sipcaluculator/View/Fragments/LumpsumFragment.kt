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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import java.text.NumberFormat
import java.util.Locale

class LumpsumFragment : Fragment(), LanguageChange {
    private lateinit var principalAmountEditText: TextInputEditText
    private lateinit var rateOfInterestEditText: TextInputEditText
    private lateinit var tenureEditText: TextInputEditText
    private lateinit var calculateButton: Button
    private lateinit var totalInvestedAmountTV: TextView
    private lateinit var estimatedReturnsTV: TextView
    private lateinit var totalAmountTV: TextView
    private var resources: Resources? = null
    private var context: Context? = null
    private lateinit var lumpsumCalculatorTitle: TextView

    private lateinit var principalAmountLayout: TextInputLayout
    private lateinit var rateOfInterestLayout: TextInputLayout
    private lateinit var tenureLayout: TextInputLayout

    private lateinit var totalInvestedAmount: MaterialTextView
    private lateinit var estimatedReturns: MaterialTextView
    private lateinit var totalAmount: MaterialTextView

    private lateinit var lumpsumBreakdownHeading: MaterialTextView
    private lateinit var lumpsumGrowthChartView: GrowthChartView
    private lateinit var lumpsumBreakdownRecyclerView: RecyclerView
    private lateinit var lumpsumBreakdownDetailTextView: MaterialTextView
    private lateinit var lumpsumSaveButton: Button
    private lateinit var lumpsumShareButton: Button
    private val lumpsumBreakdownAdapter = YearlyBreakdownAdapter()
    private var lumpsumLastSelectedBreakdown: YearlyBreakdown? = null
    private var lumpsumLastInvested: Double = 0.0
    private var lumpsumLastReturns: Double = 0.0
    private var lumpsumLastTotal: Double = 0.0
    private var lumpsumLastBreakdownList: List<YearlyBreakdown> = emptyList()

    override fun onLanguageUpdate(languageCode: String) {
        context = LocaleHelper.setLocale(requireContext(), languageCode)
        resources = context?.resources

        onLanguageChange(languageCode)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_lumpsum, container, false)
        principalAmountEditText = view.findViewById(R.id.principalAmountEditText)
        rateOfInterestEditText = view.findViewById(R.id.rateOfInterestEditText)
        tenureEditText = view.findViewById(R.id.tenureEditText)
        calculateButton = view.findViewById(R.id.calculateButton)
        totalInvestedAmountTV = view.findViewById(R.id.totalInvestedAmountTV)
        estimatedReturnsTV = view.findViewById(R.id.estimatedReturnsTV)
        totalAmountTV = view.findViewById(R.id.totalAmountTV)
        lumpsumCalculatorTitle = view.findViewById(R.id.lumpsumCalculatorTitle)
        principalAmountLayout = view.findViewById(R.id.principalAmountLayout)
        rateOfInterestLayout = view.findViewById(R.id.rateOfInterestLayout)
        tenureLayout = view.findViewById(R.id.tenureLayout)
        totalInvestedAmount = view.findViewById(R.id.totalInvestedAmount)
        estimatedReturns = view.findViewById(R.id.estimatedReturns)
        totalAmount = view.findViewById(R.id.totalAmount)

        lumpsumBreakdownHeading = view.findViewById(R.id.lumpsumBreakdownHeading)
        lumpsumGrowthChartView = view.findViewById(R.id.lumpsumGrowthChartView)
        lumpsumBreakdownRecyclerView = view.findViewById(R.id.lumpsumBreakdownRecyclerView)
        lumpsumBreakdownDetailTextView = view.findViewById(R.id.lumpsumBreakdownDetailTextView)
        lumpsumSaveButton = view.findViewById(R.id.lumpsumSaveButton)
        lumpsumShareButton = view.findViewById(R.id.lumpsumShareButton)
        lumpsumBreakdownRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        lumpsumBreakdownRecyclerView.adapter = lumpsumBreakdownAdapter
        lumpsumGrowthChartView.onPointSelected = { entry ->
            lumpsumLastSelectedBreakdown = entry
            showLumpsumBreakdownDetail(entry)
        }

        lumpsumSaveButton.setOnClickListener {
            ScenarioRepository.save(requireContext(), buildCurrentLumpsumScenario())
            Toast.makeText(
                requireContext(),
                getString(R.string.scenario_saved),
                Toast.LENGTH_SHORT
            ).show()
        }

        lumpsumShareButton.setOnClickListener {
            val shareText = ScenarioFormatter.buildShareText(buildCurrentLumpsumScenario())
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            startActivity(Intent.createChooser(intent, getString(R.string.share)))
        }

        context = LocaleHelper.setLocale(requireContext(), "en")

        principalAmountEditText.setText("25000")
        rateOfInterestEditText.setText("12")
        tenureEditText.setText("10")

        calculateLumpsum()

        calculateButton.setOnClickListener {
            calculateLumpsum()
        }
        return view
    }

    private fun calculateLumpsum() {
        val principalAmount = principalAmountEditText.text.toString().toDoubleOrNull()
        val rateOfInterest = rateOfInterestEditText.text.toString().toDoubleOrNull()
        val tenure = tenureEditText.text.toString().toDoubleOrNull()

        if (principalAmount == null || rateOfInterest == null || tenure == null) {
            Toast.makeText(requireContext(), "Please enter valid inputs", Toast.LENGTH_SHORT).show()
            return
        }

        val rate = rateOfInterest / 100

        val maturityAmount = principalAmount * Math.pow(1 + rate, tenure)
        val totalInterest = maturityAmount - principalAmount

        totalInvestedAmountTV.text = "₹%.2f".format(principalAmount)
        estimatedReturnsTV.text = "₹%.2f".format(totalInterest)
        totalAmountTV.text = "₹%.2f".format(maturityAmount)

        lumpsumLastInvested = principalAmount
        lumpsumLastReturns = totalInterest
        lumpsumLastTotal = maturityAmount

        val breakdown = calculateYearlyBreakdown(principalAmount, rateOfInterest, tenure.toInt())
        lumpsumLastBreakdownList = breakdown
        lumpsumGrowthChartView.setData(breakdown)
        lumpsumBreakdownAdapter.submitList(breakdown)
        if (breakdown.isEmpty()) {
            lumpsumLastSelectedBreakdown = null
            lumpsumBreakdownDetailTextView.text = ""
        }
    }

    private fun calculateYearlyBreakdown(
        principalAmount: Double,
        rateOfInterest: Double,
        tenureYears: Int
    ): List<YearlyBreakdown> {
        val rate = rateOfInterest / 100
        return (1..tenureYears).map { year ->
            val totalValue = principalAmount * Math.pow(1 + rate, year.toDouble())
            YearlyBreakdown(year, principalAmount, totalValue)
        }
    }

    private fun showLumpsumBreakdownDetail(entry: YearlyBreakdown) {
        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        // Uses the fragment's own getString (always available once attached) instead of the
        // nullable `resources` field, which stays null until a language button is tapped —
        // that was making this text invisible on first app open.
        lumpsumBreakdownDetailTextView.text = getString(
            R.string.breakdown_detail_format,
            entry.year,
            currencyFormatter.format(entry.investedAmount),
            currencyFormatter.format(entry.returns),
            currencyFormatter.format(entry.totalValue)
        )
    }

    private fun buildCurrentLumpsumScenario(): SavedScenario {
        val principal = principalAmountEditText.text.toString()
        val rate = rateOfInterestEditText.text.toString()
        val tenure = tenureEditText.text.toString()

        val inputs = linkedMapOf(
            "Principal Amount" to "₹$principal",
            "Rate of Interest" to "$rate%",
            "Tenure" to "$tenure years"
        )

        return SavedScenario(
            id = System.nanoTime(),
            calculatorType = CalculatorType.LUMPSUM,
            label = ScenarioFormatter.defaultLabel(CalculatorType.LUMPSUM.displayName),
            createdAtMillis = System.currentTimeMillis(),
            inputs = inputs,
            investedAmount = lumpsumLastInvested,
            returns = lumpsumLastReturns,
            totalValue = lumpsumLastTotal,
            yearlyBreakdown = lumpsumLastBreakdownList
        )
    }

    private fun onLanguageChange(s: String) {
        lumpsumCalculatorTitle.text = resources?.getString(R.string.lumpsum_Calculator)
        principalAmountLayout.hint = resources?.getString(R.string.principal_Amount)
        rateOfInterestLayout.hint = resources?.getString(R.string.rate_Of_Interest)
        tenureLayout.hint = resources?.getString(R.string.tenure_years)
        calculateButton.text = resources?.getString(R.string.calculate)
        totalInvestedAmount.text = resources?.getString(R.string.invested_amount)
        estimatedReturns.text = resources?.getString(R.string.estimated_returns)
        totalAmount.text = resources?.getString(R.string.total_amount)
        lumpsumBreakdownHeading.text = resources?.getString(R.string.yearly_breakdown)
        lumpsumLastSelectedBreakdown?.let { showLumpsumBreakdownDetail(it) }
    }
}