package com.sipcaluculator.View

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.sipcaluculator.View.Fragments.CalculatorFragment
import com.sipcaluculator.View.Fragments.CompareFragment
import com.sipcaluculator.View.Fragments.HistoryFragment
import com.sipcaluculator.View.Fragments.SIPFragment
import com.sipcaluculator.Presenter.LanguageChange
import com.sipcaluculator.Presenter.LocaleHelper
import com.sipcaluculator.R
import com.sipcaluculator.View.Fragments.LumpsumFragment
import com.sipcaluculator.View.Fragments.PPFFragment
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var sipFragment: SIPFragment
    private lateinit var calculatorFragment: CalculatorFragment
    private lateinit var ppfFragment: PPFFragment
    private lateinit var lumpsumFragment: LumpsumFragment
    private lateinit var historyFragment: HistoryFragment
    private lateinit var englishButton: Button
    private lateinit var hindiButton: Button
    private lateinit var bengaliButton: Button
    private lateinit var marathiButton: Button

    private lateinit var calculatorBtn: Button
    private lateinit var sipBtn: Button
    private lateinit var lumpsumBtn: Button
    private lateinit var ppfBtn: Button
    private lateinit var historyBtn: Button

    override fun attachBaseContext(newBase: Context?) {
        val languageCode = getSavedLanguage(newBase)
        val context = LocaleHelper.setLocale(newBase!!, languageCode)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sipFragment = SIPFragment()
        calculatorFragment = CalculatorFragment()
        ppfFragment = PPFFragment()
        lumpsumFragment = LumpsumFragment()
        historyFragment = HistoryFragment()

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, sipFragment)
            .commit()

        val savedLanguage = getSavedLanguage(this) ?: "en"
        updateLanguage(savedLanguage)

        englishButton = findViewById(R.id.englishButton)
        hindiButton = findViewById(R.id.hindiButton)
        bengaliButton = findViewById(R.id.bengaliButton)
        marathiButton = findViewById(R.id.marathiButton)

        calculatorBtn = findViewById(R.id.calculatorBtn)
        sipBtn = findViewById(R.id.sipBtn)
        lumpsumBtn = findViewById(R.id.lumpsumBtn)
        ppfBtn = findViewById(R.id.ppfBtn)
        historyBtn = findViewById(R.id.historyBtn)

        englishButton.setOnClickListener {
            updateLanguage("en")
            setActiveButton(englishButton)
        }
        hindiButton.setOnClickListener {
            updateLanguage("hi")
            setActiveButton(hindiButton)
        }
        bengaliButton.setOnClickListener {
            updateLanguage("bn")
            setActiveButton(bengaliButton)
        }
        marathiButton.setOnClickListener {
            updateLanguage("mr")
            setActiveButton(marathiButton)
        }

        calculatorBtn.setOnClickListener {
            switchFragment(calculatorFragment)
            setActiveBtn(calculatorBtn)
        }
        sipBtn.setOnClickListener {
            switchFragment(sipFragment)
            setActiveBtn(sipBtn)
        }
        lumpsumBtn.setOnClickListener {
            switchFragment(lumpsumFragment)
            setActiveBtn(lumpsumBtn)
        }
        ppfBtn.setOnClickListener {
            switchFragment(ppfFragment)
            setActiveBtn(ppfBtn)
        }
        historyBtn.setOnClickListener {
            switchFragment(historyFragment)
            setActiveBtn(historyBtn)
        }

        val activeLanguageButton = when (savedLanguage) {
            "hi" -> hindiButton
            "bn" -> bengaliButton
            "mr" -> marathiButton
            else -> englishButton
        }
        setActiveButton(activeLanguageButton)
        setActiveBtn(sipBtn)
    }

    /** Called from [HistoryFragment] when the user picks exactly 2 scenarios to compare. */
    fun showCompareFragment(idA: Long, idB: Long) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, CompareFragment.newInstance(idA, idB))
            .addToBackStack(null)
            .commit()
    }

    private fun setActiveButton(activeButton: Button) {
        listOf(englishButton, hindiButton, bengaliButton, marathiButton).forEach { button ->
            button.setBackgroundResource(R.drawable.rect_bg)
            button.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.investedAmt)
            )
        }

        activeButton.setBackgroundResource(R.drawable.rect_bg_1)
        activeButton.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(this, R.color.colorBlueBorder_2)
        )
    }

    private fun setActiveBtn(activeButton: Button) {
        listOf(sipBtn, calculatorBtn, lumpsumBtn, ppfBtn, historyBtn).forEach { button ->
            button.setBackgroundResource(R.drawable.rect_bg)
            button.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.investedAmt)
            )
        }

        activeButton.setBackgroundResource(R.drawable.rect_bg_1)
        activeButton.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(this, R.color.colorBlueBorder_2)
        )
    }

    private fun saveLanguage(context: Context, languageCode: String) {
        val preferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        preferences.edit().putString("language_code", languageCode).apply()
    }

    private fun getSavedLanguage(context: Context?): String {
        val preferences = context?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return preferences?.getString("language_code", "en") ?: "en"
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun updateLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        saveLanguage(this, languageCode)
        LocaleHelper.setLocale(this, languageCode)

        supportFragmentManager.fragments.forEach { fragment ->
            if (fragment is LanguageChange) {
                fragment.onLanguageUpdate(languageCode)
            }
        }
    }
}