package com.sipcaluculator.Data

import android.content.Context
import com.sipcaluculator.Model.CalculatorType
import com.sipcaluculator.Model.SavedScenario
import com.sipcaluculator.Model.YearlyBreakdown
import org.json.JSONArray
import org.json.JSONObject

/**
 * Local storage for saved calculation scenarios (used by History and Compare).
 *
 * Deliberately uses SharedPreferences + org.json instead of Room: the app has
 * no database/DI/coroutines infrastructure today, the data is small (a
 * handful of scenarios, each with at most a few dozen yearly rows), and this
 * keeps the dependency footprint unchanged.
 */
object ScenarioRepository {

    private const val PREFS_NAME = "saved_scenarios_prefs"
    private const val KEY_SCENARIOS = "scenarios_json"

    fun getAll(context: Context): List<SavedScenario> {
        return readAll(context).sortedByDescending { it.createdAtMillis }
    }

    fun getById(context: Context, id: Long): SavedScenario? =
        readAll(context).find { it.id == id }

    fun save(context: Context, scenario: SavedScenario) {
        val current = readAll(context).toMutableList()
        current.add(scenario)
        writeAll(context, current)
    }

    fun delete(context: Context, id: Long) {
        val current = readAll(context).filterNot { it.id == id }
        writeAll(context, current)
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun readAll(context: Context): List<SavedScenario> {
        val json = prefs(context).getString(KEY_SCENARIOS, null) ?: return emptyList()
        return try {
            parseScenarios(json)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun writeAll(context: Context, scenarios: List<SavedScenario>) {
        val array = JSONArray()
        scenarios.forEach { array.put(toJson(it)) }
        prefs(context).edit().putString(KEY_SCENARIOS, array.toString()).apply()
    }

    private fun toJson(scenario: SavedScenario): JSONObject {
        val obj = JSONObject()
        obj.put("id", scenario.id)
        obj.put("calculatorType", scenario.calculatorType.name)
        obj.put("label", scenario.label)
        obj.put("createdAtMillis", scenario.createdAtMillis)

        val inputsObj = JSONObject()
        scenario.inputs.forEach { (key, value) -> inputsObj.put(key, value) }
        obj.put("inputs", inputsObj)

        obj.put("investedAmount", scenario.investedAmount)
        obj.put("returns", scenario.returns)
        obj.put("totalValue", scenario.totalValue)

        val breakdownArray = JSONArray()
        scenario.yearlyBreakdown.forEach { entry ->
            val entryObj = JSONObject()
            entryObj.put("year", entry.year)
            entryObj.put("investedAmount", entry.investedAmount)
            entryObj.put("totalValue", entry.totalValue)
            breakdownArray.put(entryObj)
        }
        obj.put("yearlyBreakdown", breakdownArray)

        return obj
    }

    private fun parseScenarios(json: String): List<SavedScenario> {
        val array = JSONArray(json)
        val result = mutableListOf<SavedScenario>()

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)

            val inputsObj = obj.optJSONObject("inputs") ?: JSONObject()
            val inputs = mutableMapOf<String, String>()
            val keys = inputsObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                inputs[key] = inputsObj.getString(key)
            }

            val breakdownArray = obj.optJSONArray("yearlyBreakdown") ?: JSONArray()
            val breakdown = mutableListOf<YearlyBreakdown>()
            for (j in 0 until breakdownArray.length()) {
                val entryObj = breakdownArray.getJSONObject(j)
                breakdown.add(
                    YearlyBreakdown(
                        year = entryObj.getInt("year"),
                        investedAmount = entryObj.getDouble("investedAmount"),
                        totalValue = entryObj.getDouble("totalValue")
                    )
                )
            }

            result.add(
                SavedScenario(
                    id = obj.getLong("id"),
                    calculatorType = CalculatorType.valueOf(obj.getString("calculatorType")),
                    label = obj.getString("label"),
                    createdAtMillis = obj.getLong("createdAtMillis"),
                    inputs = inputs,
                    investedAmount = obj.getDouble("investedAmount"),
                    returns = obj.getDouble("returns"),
                    totalValue = obj.getDouble("totalValue"),
                    yearlyBreakdown = breakdown
                )
            )
        }

        return result
    }
}
