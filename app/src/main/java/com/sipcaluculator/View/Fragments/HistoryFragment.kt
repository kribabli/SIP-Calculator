package com.sipcaluculator.View.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sipcaluculator.Data.ScenarioFormatter
import com.sipcaluculator.Data.ScenarioRepository
import com.sipcaluculator.Model.SavedScenario
import com.sipcaluculator.R
import com.sipcaluculator.View.Adapters.SavedScenarioAdapter
import com.sipcaluculator.View.MainActivity

/** Lists every saved calculation. Lets the user share or delete any of them, and pick exactly two to compare. */
class HistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyTextView: TextView
    private lateinit var compareButton: Button

    private val selectedIds = linkedSetOf<Long>()
    private lateinit var adapter: SavedScenarioAdapter
    private var currentScenarios: List<SavedScenario> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        recyclerView = view.findViewById(R.id.historyRecyclerView)
        emptyTextView = view.findViewById(R.id.historyEmptyTextView)
        compareButton = view.findViewById(R.id.historyCompareButton)

        adapter = SavedScenarioAdapter(
            isSelected = { id -> selectedIds.contains(id) },
            onToggleSelect = { scenario, checked -> onToggleSelect(scenario, checked) },
            onShare = { scenario -> shareScenario(scenario) },
            onDelete = { scenario -> deleteScenario(scenario) }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        compareButton.setOnClickListener {
            if (selectedIds.size == 2) {
                val (idA, idB) = selectedIds.toList()
                (activity as? MainActivity)?.showCompareFragment(idA, idB)
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.select_two_to_compare),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        refreshList()
        return view
    }

    private fun onToggleSelect(scenario: SavedScenario, checked: Boolean) {
        if (checked) {
            if (selectedIds.size >= 2) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.select_two_to_compare),
                    Toast.LENGTH_SHORT
                ).show()
                // Re-bind so the checkbox that was just tapped past the limit reverts to unchecked.
                adapter.submitList(currentScenarios)
                return
            }
            selectedIds.add(scenario.id)
        } else {
            selectedIds.remove(scenario.id)
        }
        updateCompareButton()
    }

    private fun shareScenario(scenario: SavedScenario) {
        val shareText = ScenarioFormatter.buildShareText(scenario)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share)))
    }

    private fun deleteScenario(scenario: SavedScenario) {
        ScenarioRepository.delete(requireContext(), scenario.id)
        selectedIds.remove(scenario.id)
        refreshList()
    }

    private fun refreshList() {
        currentScenarios = ScenarioRepository.getAll(requireContext())
        adapter.submitList(currentScenarios)
        emptyTextView.visibility = if (currentScenarios.isEmpty()) View.VISIBLE else View.GONE
        recyclerView.visibility = if (currentScenarios.isEmpty()) View.GONE else View.VISIBLE
        updateCompareButton()
    }

    private fun updateCompareButton() {
        compareButton.isEnabled = selectedIds.size == 2
        compareButton.text = if (selectedIds.size == 2) {
            getString(R.string.compare)
        } else {
            getString(R.string.compare_selected_format, selectedIds.size)
        }
    }
}
