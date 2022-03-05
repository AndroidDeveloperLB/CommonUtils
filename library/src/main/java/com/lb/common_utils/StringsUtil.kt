package com.lb.common_utils

import androidx.annotation.IntRange
import java.text.NumberFormat
import java.util.*

object StringsUtil {
    interface BytesFormatter {
        /**called when the type of the result to format is Long. Example: 123KB
         * @param unitPowerIndex the unit-power we need to format to. Examples: 0 is bytes, 1 is kb, 2 is mb, etc...
         * available units and their order: B,K,M,G,T,P,E
         * @param isMetric true if each kilo==1000, false if kilo==1024
         * */
        fun onFormatLong(valueToFormat: Long, unitPowerIndex: Int, isMetric: Boolean): String

        /**called when the type of the result to format is Double. Example: 1.23KB
         * @param unitPowerIndex the unit-power we need to format to. Examples: 0 is bytes, 1 is kb, 2 is mb, etc...
         * available units and their order: B,K,M,G,T,P,E
         * @param isMetric true if each kilo==1000, false if kilo==1024
         * */
        fun onFormatDouble(valueToFormat: Double, unitPowerIndex: Int, isMetric: Boolean): String
    }

    val defaultBytesFormatter = object : BytesFormatter {
        val numberFormat = NumberFormat.getNumberInstance(Locale.ROOT).also {
            it.maximumFractionDigits = 2
            it.minimumFractionDigits = 0
        }

        private fun formatByUnit(formattedNumber: String, threePowerIndex: Int, isMetric: Boolean): String {
            val sb = StringBuilder(formattedNumber.length + 4)
            sb.append(formattedNumber)
            val unitsToUse = "B${if (isMetric) "k" else "K"}MGTPE"
            sb.append(unitsToUse[threePowerIndex])
            if (threePowerIndex > 0)
                if (isMetric) sb.append('B') else sb.append("iB")
            return sb.toString()
        }

        override fun onFormatLong(valueToFormat: Long, unitPowerIndex: Int, isMetric: Boolean): String {
            return formatByUnit(String.format("%,d", valueToFormat), unitPowerIndex, isMetric)
        }

        override fun onFormatDouble(valueToFormat: Double, unitPowerIndex: Int, isMetric: Boolean): String {
            //alternative for using numberFormat :
            //val formattedNumber = String.format("%,.2f", valueToFormat).let { initialFormattedString ->
            //    if (initialFormattedString.contains('.'))
            //        return@let initialFormattedString.dropLastWhile { it == '0' }
            //    else return@let initialFormattedString
            //}
            return formatByUnit(numberFormat.format(valueToFormat), unitPowerIndex, isMetric)
        }
    }

    /**
     * formats the bytes to a human readable format, by providing the values to format later in the unit that we've found best to fit it
     *
     * @param isMetric true if each kilo==1000, false if kilo==1024
     * */
    fun bytesIntoHumanReadable(
        @IntRange(from = 0L) bytesToFormat: Long, bytesFormatter: BytesFormatter = defaultBytesFormatter,
        isMetric: Boolean = true
    ): String {
        val units = if (isMetric) 1000L else 1024L
        if (bytesToFormat < units)
            return bytesFormatter.onFormatLong(bytesToFormat, 0, isMetric)
        var bytesLeft = bytesToFormat
        var unitPowerIndex = 0
        while (unitPowerIndex < 6) {
            val newBytesLeft = bytesLeft / units
            if (newBytesLeft < units) {
                val byteLeftAsDouble = bytesLeft.toDouble() / units
                val needToShowAsInteger =
                    byteLeftAsDouble == (bytesLeft / units).toDouble()
                ++unitPowerIndex
                if (needToShowAsInteger) {
                    bytesLeft = newBytesLeft
                    break
                }
                return bytesFormatter.onFormatDouble(byteLeftAsDouble, unitPowerIndex, isMetric)
            }
            bytesLeft = newBytesLeft
            ++unitPowerIndex
        }
        return bytesFormatter.onFormatLong(bytesLeft, unitPowerIndex, isMetric)
    }
}