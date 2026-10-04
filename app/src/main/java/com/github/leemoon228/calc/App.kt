package com.github.leemoon228.calc

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.leemoon228.calc.ui.theme.CalcTheme
import java.math.BigDecimal
import java.math.RoundingMode

private enum class CalcOperator {
    ADD,
    SUBTRACT,
    MULTIPLY,
    DIVIDE;

    val symbol: String
        get() = when (this) {
            ADD -> "+"
            SUBTRACT -> "-"
            MULTIPLY -> "*"
            DIVIDE -> "/"
        }
}

private fun formatNumber(value: Double): String {
    if (!value.isFinite()) return "Error"

    val number = BigDecimal.valueOf(value)
        .setScale(10, RoundingMode.HALF_UP)
        .stripTrailingZeros()
        .toPlainString()
        .let { if (it == "-0") "0" else it }

    if (number.length <= MAX_DISPLAY_LENGTH) return number

    val sign = if (number.startsWith("-")) "-" else ""
    val digits = number.removePrefix(sign)
    return sign + digits.take(MAX_DISPLAY_LENGTH - sign.length).trimEnd('.')
}

private fun calculate(a: Double, b: Double, operator: CalcOperator): Double {
    return when (operator) {
        CalcOperator.ADD -> a + b
        CalcOperator.SUBTRACT -> a - b
        CalcOperator.MULTIPLY -> a * b
        CalcOperator.DIVIDE -> if (b == 0.0) Double.NaN else a / b
    }
}

private data class CalculatorButton(
    val label: String,
    val action: () -> Unit
)

private const val MAX_DISPLAY_LENGTH = 10

@Composable
fun App(modifier: Modifier = Modifier) {
    var display by rememberSaveable { mutableStateOf("0") }
    var expression by rememberSaveable { mutableStateOf("") }
    var firstOperand by rememberSaveable { mutableStateOf(0.0) }
    var hasFirstOperand by rememberSaveable { mutableStateOf(false) }
    var pendingOperation by rememberSaveable { mutableStateOf<CalcOperator?>(null) }
    var isNewInput by rememberSaveable { mutableStateOf(true) }

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    fun clearAll() {
        display = "0"
        expression = ""
        firstOperand = 0.0
        hasFirstOperand = false
        pendingOperation = null
        isNewInput = true
    }

    fun clearCurrentInput() {
        display = "0"
        if (pendingOperation == null) {
            expression = ""
            firstOperand = 0.0
            hasFirstOperand = false
        }
        isNewInput = true
    }

    fun onDigitClick(digit: String) {
        if (display == "Error") {
            display = digit
            isNewInput = false
            return
        }

        val candidate = if (isNewInput) {
            if (digit == "0") "0" else digit
        } else {
            if (display == "0") digit else display + digit
        }

        if (candidate.length > MAX_DISPLAY_LENGTH) return
        display = candidate
        isNewInput = false
    }

    fun onDotClick() {
        if (display == "Error") {
            display = "0."
            isNewInput = false
            return
        }

        if (isNewInput) {
            display = "0."
            isNewInput = false
            return
        }

        if (!display.contains('.') && display.length < MAX_DISPLAY_LENGTH) {
            display += "."
        }
    }

    fun toggleSign() {
        if (display == "Error" || display == "0") return

        val nextValue = if (display.startsWith("-")) display.removePrefix("-") else "-$display"
        if (nextValue.length > MAX_DISPLAY_LENGTH) return

        display = nextValue
        isNewInput = false
    }

    fun applyOperator(operator: CalcOperator) {
        if (display == "Error") return

        if (hasFirstOperand && pendingOperation != null && isNewInput) {
            pendingOperation = operator
            expression = "${formatNumber(firstOperand)} ${operator.symbol}"
            return
        }

        val currentValue = display.toDoubleOrNull() ?: return

        if (hasFirstOperand && pendingOperation != null) {
            val result = calculate(firstOperand, currentValue, pendingOperation!!)
            if (!result.isFinite()) {
                display = "Error"
                expression = ""
                firstOperand = 0.0
                hasFirstOperand = false
                pendingOperation = null
                isNewInput = true
                return
            }

            firstOperand = result
            expression = "${formatNumber(result)} ${operator.symbol}"
            display = "0"
            pendingOperation = operator
            isNewInput = true
            return
        }

        firstOperand = currentValue
        hasFirstOperand = true
        pendingOperation = operator
        expression = "${formatNumber(currentValue)} ${operator.symbol}"
        display = "0"
        isNewInput = true
    }

    fun calculateResult() {
        if (!hasFirstOperand || pendingOperation == null || display == "Error") return

        val currentValue = display.toDoubleOrNull() ?: return
        val result = calculate(firstOperand, currentValue, pendingOperation!!)

        if (!result.isFinite()) {
            display = "Error"
            expression = ""
            firstOperand = 0.0
            hasFirstOperand = false
            pendingOperation = null
            isNewInput = true
            return
        }

        expression =
            "${formatNumber(firstOperand)} ${pendingOperation!!.symbol} ${formatNumber(currentValue)} ="
        display = formatNumber(result)
        firstOperand = result
        hasFirstOperand = true
        pendingOperation = null
        isNewInput = true
    }

    val portraitButtons = listOf(
        listOf(
            CalculatorButton("C") { clearAll() },
            CalculatorButton("CE") { clearCurrentInput() },
            CalculatorButton("±") { toggleSign() },
            CalculatorButton("/") { applyOperator(CalcOperator.DIVIDE) }
        ),
        listOf(
            CalculatorButton("7") { onDigitClick("7") },
            CalculatorButton("8") { onDigitClick("8") },
            CalculatorButton("9") { onDigitClick("9") },
            CalculatorButton("*") { applyOperator(CalcOperator.MULTIPLY) }
        ),
        listOf(
            CalculatorButton("4") { onDigitClick("4") },
            CalculatorButton("5") { onDigitClick("5") },
            CalculatorButton("6") { onDigitClick("6") },
            CalculatorButton("-") { applyOperator(CalcOperator.SUBTRACT) }
        ),
        listOf(
            CalculatorButton("1") { onDigitClick("1") },
            CalculatorButton("2") { onDigitClick("2") },
            CalculatorButton("3") { onDigitClick("3") },
            CalculatorButton("+") { applyOperator(CalcOperator.ADD) }
        ),
        listOf(
            CalculatorButton("00") { onDigitClick("00") },
            CalculatorButton("0") { onDigitClick("0") },
            CalculatorButton(".") { onDotClick() },
            CalculatorButton("=") { calculateResult() }
        )
    )

    val landscapeButtons = listOf(
        listOf(
            CalculatorButton("7") { onDigitClick("7") },
            CalculatorButton("8") { onDigitClick("8") },
            CalculatorButton("9") { onDigitClick("9") },
            CalculatorButton("C") { clearAll() },
            CalculatorButton("CE") { clearCurrentInput() }
        ),
        listOf(
            CalculatorButton("4") { onDigitClick("4") },
            CalculatorButton("5") { onDigitClick("5") },
            CalculatorButton("6") { onDigitClick("6") },
            CalculatorButton("*") { applyOperator(CalcOperator.MULTIPLY) },
            CalculatorButton("-") { applyOperator(CalcOperator.SUBTRACT) }
        ),
        listOf(
            CalculatorButton("1") { onDigitClick("1") },
            CalculatorButton("2") { onDigitClick("2") },
            CalculatorButton("3") { onDigitClick("3") },
            CalculatorButton("/") { applyOperator(CalcOperator.DIVIDE) },
            CalculatorButton("+") { applyOperator(CalcOperator.ADD) }
        ),
        listOf(
            CalculatorButton("0") { onDigitClick("0") },
            CalculatorButton("00") { onDigitClick("00") },
            CalculatorButton(".") { onDotClick() },
            CalculatorButton("±") { toggleSign() },
            CalculatorButton("=") { calculateResult() }
        )
    )

    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    )

    val buttonRows = if (isLandscape) landscapeButtons else portraitButtons

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        SelectionContainer {
            Text(
                text = expression.ifEmpty { " " },
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                fontSize = 22.sp,
                maxLines = 1
            )
        }

        SelectionContainer {
            Text(
                text = display,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("result"),
                textAlign = TextAlign.End,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = if (isLandscape) 42.sp else 52.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            buttonRows.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { button ->
                        Button(
                            onClick = button.action,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            colors = buttonColors,
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Text(
                                text = button.label,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    CalcTheme {
        App()
    }
}