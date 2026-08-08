package com.sipcaluculator.Model

/** Which calculator a saved scenario came from. */
enum class CalculatorType(val displayName: String) {
    SIP("SIP"),
    LUMPSUM("Lumpsum"),
    PPF("PPF")
}
