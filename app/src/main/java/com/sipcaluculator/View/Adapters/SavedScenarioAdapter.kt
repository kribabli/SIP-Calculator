package com.sipcaluculator.View.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sipcaluculator.Model.SavedScenario
import com.sipcaluculator.R
import java.text.NumberFormat
import java.util.Locale

/** Lists saved scenarios in History: shows a summary, and lets the user select (for compare), share, or delete each one. */
class SavedScenarioAdapter(
    private var items: List<SavedScenario> = emptyList(),
    private val isSelected: (Long) -> Boolean,
    private val onToggleSelect: (SavedScenario, Boolean) -> Unit,
    private val onShare: (SavedScenario) -> Unit,
    private val onDelete: (SavedScenario) -> Unit
) : RecyclerView.Adapter<SavedScenarioAdapter.ViewHolder>() {

    private val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    fun submitList(newItems: List<SavedScenario>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_saved_scenario, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val scenario = items[position]

        holder.typeTextView.text = scenario.calculatorType.displayName
        holder.labelTextView.text = scenario.label
        holder.summaryTextView.text = holder.itemView.context.getString(
            R.string.scenario_summary_format,
            currencyFormatter.format(scenario.investedAmount),
            currencyFormatter.format(scenario.totalValue)
        )

        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = isSelected(scenario.id)
        holder.checkBox.setOnCheckedChangeListener { _, checked ->
            onToggleSelect(scenario, checked)
        }

        holder.shareButton.setOnClickListener { onShare(scenario) }
        holder.deleteButton.setOnClickListener { onDelete(scenario) }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBox: CheckBox = view.findViewById(R.id.scenarioCompareCheckBox)
        val typeTextView: TextView = view.findViewById(R.id.scenarioTypeTextView)
        val labelTextView: TextView = view.findViewById(R.id.scenarioLabelTextView)
        val summaryTextView: TextView = view.findViewById(R.id.scenarioSummaryTextView)
        val shareButton: ImageButton = view.findViewById(R.id.scenarioShareButton)
        val deleteButton: ImageButton = view.findViewById(R.id.scenarioDeleteButton)
    }
}
