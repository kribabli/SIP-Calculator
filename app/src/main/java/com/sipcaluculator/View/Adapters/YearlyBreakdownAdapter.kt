package com.sipcaluculator.View.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sipcaluculator.Model.YearlyBreakdown
import com.sipcaluculator.R
import java.text.NumberFormat
import java.util.Locale

/**
 * Shows a year-by-year breakdown table (year, invested, returns, total)
 * for any of the three calculators (SIP, Lumpsum, PPF).
 */
class YearlyBreakdownAdapter(
    private var items: List<YearlyBreakdown> = emptyList()
) : RecyclerView.Adapter<YearlyBreakdownAdapter.ViewHolder>() {

    private val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    fun submitList(newItems: List<YearlyBreakdown>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_yearly_breakdown, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.yearTextView.text = item.year.toString()
        holder.investedTextView.text = currencyFormatter.format(item.investedAmount)
        holder.returnsTextView.text = currencyFormatter.format(item.returns)
        holder.totalTextView.text = currencyFormatter.format(item.totalValue)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val yearTextView: TextView = view.findViewById(R.id.breakdownYearTextView)
        val investedTextView: TextView = view.findViewById(R.id.breakdownInvestedTextView)
        val returnsTextView: TextView = view.findViewById(R.id.breakdownReturnsTextView)
        val totalTextView: TextView = view.findViewById(R.id.breakdownTotalTextView)
    }
}
