package com.sipcaluculator.View.Fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.sipcaluculator.Presenter.LanguageChange
import com.sipcaluculator.Presenter.LocaleHelper
import com.sipcaluculator.R
import java.math.BigInteger
import java.util.Locale

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private lateinit var tvSec: TextView
private lateinit var tvMain: TextView
private lateinit var buttons: List<Button>

class CalculatorFragment : Fragment(), LanguageChange {
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
        val view = inflater.inflate(R.layout.fragment_calculator, container, false)
        tvSec = view.findViewById(R.id.idTVSecondary)
        tvMain = view.findViewById(R.id.idTVprimary)

        buttons = listOf(
            view.findViewById(R.id.b1),
            view.findViewById(R.id.b2),
            view.findViewById(R.id.b3),
            view.findViewById(R.id.b4),
            view.findViewById(R.id.b5),
            view.findViewById(R.id.b6),
            view.findViewById(R.id.b7),
            view.findViewById(R.id.b8),
            view.findViewById(R.id.b9),
            view.findViewById(R.id.b0),
            view.findViewById(R.id.bdot),
            view.findViewById(R.id.bplus),
            view.findViewById(R.id.bminus),
            view.findViewById(R.id.bmul),
            view.findViewById(R.id.bdiv),
            view.findViewById(R.id.bbrac1),
            view.findViewById(R.id.bbrac2),
            view.findViewById(R.id.bpi),
            view.findViewById(R.id.bsin),
            view.findViewById(R.id.bcos),
            view.findViewById(R.id.btan),
            view.findViewById(R.id.blog),
            view.findViewById(R.id.bln),
            view.findViewById(R.id.bsqrt),
            view.findViewById(R.id.binv),
            view.findViewById(R.id.bfact),
            view.findViewById(R.id.bsquare)
        )

        context = LocaleHelper.setLocale(requireContext(), "en")

        buttons.forEach { button ->
            button.setOnClickListener {
                val value = (it as Button).text.toString()
                appendToMainDisplay(value)
            }
        }

        view.findViewById<Button>(R.id.bequal).setOnClickListener {
            calculateResult()
        }

        view.findViewById<Button>(R.id.bac).setOnClickListener {
            tvMain.text = ""
            tvSec.text = ""
        }

        view.findViewById<ImageView>(R.id.bc).setOnClickListener {
            val str = tvMain.text.toString()
            if (str.isNotEmpty()) {
                tvMain.text = str.dropLast(1)
            }
        }

        view.findViewById<Button>(R.id.bfact).setOnClickListener {
            handleFactorial()
        }

        view.findViewById<Button>(R.id.bsqrt).setOnClickListener {
            handleSquareRoot()
        }

        view.findViewById<Button>(R.id.bsquare).setOnClickListener {
            handleSquare()
        }

        view.findViewById<Button>(R.id.bpi).setOnClickListener {
            appendToMainDisplay("3.14159")
            tvSec.text = "π"
        }

        view.findViewById<Button>(R.id.bmul).setOnClickListener {
            appendToMainDisplay("*")
        }

        view.findViewById<Button>(R.id.bdiv).setOnClickListener {
            appendToMainDisplay("/")
        }

        view.findViewById<Button>(R.id.bbrac1).setOnClickListener {
            appendToMainDisplay("(")
        }

        view.findViewById<Button>(R.id.bbrac2).setOnClickListener {
            appendToMainDisplay(")")
        }
        return view
    }

    @SuppressLint("SetTextI18n")
    private fun appendToMainDisplay(value: String) {
        val currentText = tvMain.text.toString()
        if (value == "." && currentText.isNotEmpty() && currentText.last() == '.') {
            return
        }
        if (currentText.isNotEmpty() && value in "+-*/" && currentText.last() in "+-*/") {
            return
        }
        tvMain.text = (tvMain.text.toString() + value)
    }

    private fun isBalanced(expression: String): Boolean {
        var count = 0
        for (char in expression) {
            when (char) {
                '(' -> count++
                ')' -> {
                    count--
                    if (count < 0) return false
                }
            }
        }
        return count == 0
    }

    private fun addImplicitMultiplication(expression: String): String {
        val builder = StringBuilder()
        var i = 0

        while (i < expression.length) {
            val currentChar = expression[i]

            if (currentChar.isDigit() || currentChar == '.') {
                builder.append(currentChar)

                while (i + 1 < expression.length && (expression[i + 1].isDigit() || expression[i + 1] == '.')) {
                    i++
                    builder.append(expression[i])
                }

                if (i + 1 < expression.length && expression.substring(i + 1).startsWith("pi")) {
                    builder.append("*pi")
                    i += 2
                    continue
                }

                i++
                continue
            }

            if (expression.substring(i).startsWith("pi")) {
                if (builder.isNotEmpty() && (builder.last().isDigit() || builder.last() == ')')) {
                    builder.append("*")
                }
                builder.append("pi")
                i += 2
                continue
            }

            builder.append(currentChar)
            i++
        }

        return builder.toString()
    }

    private fun calculateResult() {
        var expression = tvMain.text.toString()

        if (expression.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a valid expression", Toast.LENGTH_SHORT)
                .show()
            return
        }

        if (!isBalanced(expression)) {
            Toast.makeText(requireContext(), "Unbalanced parentheses", Toast.LENGTH_SHORT).show()
            return
        }

        expression = addImplicitMultiplication(expression)
        expression = expression.replace("pi", Math.PI.toString())

        try {
            val result = evaluateMethod(expression)
            val formattedResult = String.format("%.5f", result)

            tvMain.text = formattedResult
            tvSec.text = expression
        } catch (e: ArithmeticException) {
            Toast.makeText(requireContext(), "Math Error", Toast.LENGTH_SHORT).show()
            tvMain.text = ""
            tvSec.text = ""
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Invalid Input", Toast.LENGTH_SHORT).show()
            tvMain.text = ""
            tvSec.text = ""
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleFactorial() {
        try {
            val value = tvMain.text.toString().toInt()
            if (value < 0) {
                Toast.makeText(
                    requireContext(),
                    "Factorial not defined for negative numbers",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val fact = factorial(value)
                tvMain.text = fact.toString()
                tvSec.text = "$value!"
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Invalid Input", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSquareRoot() {
        try {
            val value = tvMain.text.toString().toDouble()
            val sqrt = Math.sqrt(value)
            tvMain.text = sqrt.toString()
            tvSec.text = "√($value)"
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Invalid Input", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleSquare() {
        try {
            val value = tvMain.text.toString().toDouble()
            val square = value * value
            tvMain.text = square.toString()
            tvSec.text = "$value²"
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Invalid Input", Toast.LENGTH_SHORT).show()
        }
    }

    private fun factorial(n: Int): BigInteger {
        return if (n == 0) BigInteger.ONE else (1..n).map { it.toBigInteger() }
            .reduce(BigInteger::multiply)
    }

    private fun evaluateMethod(expression: String): Double {
        return object : Any() {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < expression.length) expression[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val result = parseExpression()
                if (pos < expression.length) throw RuntimeException("Unexpected: ${ch.toChar()}")
                return result
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    when {
                        eat('+'.code) -> x += parseTerm()
                        eat('-'.code) -> x -= parseTerm()
                        else -> return x
                    }
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    when {
                        eat('*'.code) -> x *= parseFactor()
                        eat('/'.code) -> {
                            val denominator = parseFactor()
                            if (denominator == 0.0) throw ArithmeticException("Division by zero")
                            x /= denominator
                        }

                        eat('!'.code) -> {
                            if (x != x.toInt()
                                    .toDouble()
                            ) throw RuntimeException("Factorial not defined for non-integers")
                            x = factorial(x.toInt())
                        }

                        else -> return x
                    }
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor()
                if (eat('-'.code)) return -parseFactor()

                var x: Double
                val startPos = pos

                if (eat('('.code)) {
                    x = parseExpression()
                    eat(')'.code)
                } else if (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) {
                    while (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) nextChar()
                    x = expression.substring(startPos, pos).toDouble()
                } else if (ch >= 'a'.code && ch <= 'z'.code) {
                    while (ch >= 'a'.code && ch <= 'z'.code) nextChar()
                    val func = expression.substring(startPos, pos)
                    x = parseFactor()
                    x = when (func) {
                        "sqrt" -> Math.sqrt(x)
                        "sin" -> Math.sin(Math.toRadians(x))
                        "cos" -> Math.cos(Math.toRadians(x))
                        "tan" -> Math.tan(Math.toRadians(x))
                        "log" -> Math.log10(x)
                        "ln" -> Math.log(x)
                        else -> throw RuntimeException("Unknown function: $func")
                    }
                } else {
                    throw RuntimeException("Unexpected: ${ch.toChar()}")
                }

                if (eat('^'.code)) x = Math.pow(x, parseFactor())
                return x
            }

            fun factorial(n: Int): Double {
                if (n < 0) throw ArithmeticException("Factorial not defined for negative numbers")
                return (1..n).fold(1.0) { acc, i -> acc * i }
            }
        }.parse()
    }

    override fun onAttach(context: Context) {
        super.onAttach(LocaleHelper.setLocale(context, Locale.getDefault().language))
    }

    private fun onLanguageChange(s: String) {

    }
}