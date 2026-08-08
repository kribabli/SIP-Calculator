package com.sipcaluculator.View.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sipcaluculator.Data.ScenarioRepository
import com.sipcaluculator.Model.SavedScenario
import com.sipcaluculator.R
import com.sipcaluculator.View.Adapters.YearlyBreakdownAdapter
import com.sipcaluculator.View.Widgets.GrowthChartView
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

/** Shows two saved scenarios side by side (stacked): their inputs, headline results, growth chart and full yearly table. */
class CompareFragment : Fragment() {

    companion object {
        private const val ARG_ID_A = "scenario_id_a"
        private const val ARG_ID_B = "scenario_id_b"

        fun newInstance(idA: Long, idB: Long): CompareFragment {
            val fragment = CompareFragment()
            fragment.arguments = Bundle().apply {
                putLong(ARG_ID_A, idA)
                putLong(ARG_ID_B, idB)
            }
            return fragment
        }
    }

    private val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_compare, container, false)

        val idA = arguments?.getLong(ARG_ID_A) ?: -1L
        val idB = arguments?.getLong(ARG_ID_B) ?: -1L

        val scenarioA = ScenarioRepository.getById(requireContext(), idA)
        val scenarioB = ScenarioRepository.getById(requireContext(), idB)

        if (scenarioA == null || scenarioB == null) {
            Toast.makeText(
                requireContext(),
                "One or both saved scenarios could not be found",
                Toast.LENGTH_SHORT
            ).show()
            parentFragmentManager.popBackStack()
            return view
        }

        bindPanel(
            view,
            scenarioA,
            typeId = R.id.compareATypeTextView,
            labelId = R.id.compareALabelTextView,
            inputsId = R.id.compareAInputsTextView,
            resultsId = R.id.compareAResultsTextView,
            chartId = R.id.compareAChartView,
            recyclerId = R.id.compareARecyclerView
        )
        bindPanel(
            view,
            scenarioB,
            typeId = R.id.compareBTypeTextView,
            labelId = R.id.compareBLabelTextView,
            inputsId = R.id.compareBInputsTextView,
            resultsId = R.id.compareBResultsTextView,
            chartId = R.id.compareBChartView,
            recyclerId = R.id.compareBRecyclerView
        )

        val diffTextView = view.findViewById<TextView>(R.id.compareDiffTextView)
        diffTextView.text = buildDiffSummary(scenarioA, scenarioB)

        return view
    }

    private fun bindPanel(
        root: View,
        scenario: SavedScenario,
        typeId: Int,
        labelId: Int,
        inputsId: Int,
        resultsId: Int,
        chartId: Int,
        recyclerId: Int
    ) {
        root.findViewById<TextView>(typeId).text = scenario.calculatorType.displayName
        root.findViewById<TextView>(labelId).text = scenario.label

        val inputsText = scenario.inputs.entries.joinToString("\n") { (label, value) -> "$label: $value" }
        root.findViewById<TextView>(inputsId).text = inputsText

        root.findViewById<TextView>(resultsId).text = getString(
            R.string.scenario_results_format,
            currencyFormatter.format(scenario.investedAmount),
            currencyFormatter.format(scenario.returns),
            currencyFormatter.format(scenario.totalValue)
        )

        root.findViewById<GrowthChartView>(chartId).setData(scenario.yearlyBreakdown)

        val recyclerView = root.findViewById<RecyclerView>(recyclerId)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = YearlyBreakdownAdapter(scenario.yearlyBreakdown)
    }

    private fun buildDiffSummary(scenarioA: SavedScenario, scenarioB: SavedScenario): String {
        val diff = scenarioA.totalValue - scenarioB.totalValue
        if (abs(diff) < 0.01) {
            return "Both scenarios end with the same total value: ${currencyFormatter.format(scenarioA.totalValue)}"
        }

        val (higher, lower, higherDiff) = if (diff > 0) {
            Triple(scenarioA, scenarioB, diff)
        } else {
            Triple(scenarioB, scenarioA, -diff)
        }

        return "${higher.label} ends with ${currencyFormatter.format(higherDiff)} more than ${lower.label}"
    }
}
