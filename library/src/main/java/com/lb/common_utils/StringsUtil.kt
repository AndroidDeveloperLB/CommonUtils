package com.lb.common_utils

import android.annotation.SuppressLint
import android.app.Application
import android.provider.Settings
import androidx.annotation.IntRange
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.*
import java.util.*

/**2.0f->"2"
 * 2f->"2"
 * -2.0f->"-2"
 * */
fun Float.toStringWithoutDecimalPointIfPossible( ): String {
    val i = toInt()
    if (this == i.toFloat())
        return "$i"
    return "$this"
}

fun String?.toJSONObject(): JSONObject? {
    if (this == null)
        return null
    try {
        return JSONObject(this)
    } catch (_: JSONException) {
    }
    return null
}

fun String?.toJSONArray(): JSONArray? {
    if (this == null)
        return null
    try {
        return JSONArray(this)
    } catch (_: JSONException) {
    }
    return null
}

object StringsUtil {
    private val defaultNormalizationRegex: Regex by lazy {
        "[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+".toRegex()
    }

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

    /**removes Diacritic signs from text to be able to search them easier. Works only on some languages. Example : "ā" becomes "a"
     * Warning: has a rare crash on some devices (PatternSyntaxException) https://issuetracker.google.com/issues/310376033#comment3*/
    fun normalizeIfNeeded(
        string: CharSequence,
        form: Normalizer.Form = Normalizer.Form.NFD,
        regex: Regex = defaultNormalizationRegex
    ): CharSequence {
        if (Normalizer.isNormalized(string, form))
            return string
        val normalized = Normalizer.normalize(string, form)
        return normalized.replace(regex, "")
    }

    private val defaultBytesFormatter = object : BytesFormatter {
        val numberFormat = NumberFormat.getNumberInstance(Locale.ROOT).also {
            it.maximumFractionDigits = 2
            it.minimumFractionDigits = 0
        }

        private fun formatByUnit(
            formattedNumber: String,
            threePowerIndex: Int,
            isMetric: Boolean
        ): String {
            val sb = StringBuilder(formattedNumber.length + 4)
            sb.append(formattedNumber)
            val unitsToUse = "B${if (isMetric) "k" else "K"}MGTPE"
            sb.append(unitsToUse[threePowerIndex])
            if (threePowerIndex > 0)
                if (isMetric) sb.append('B') else sb.append("iB")
            return sb.toString()
        }

        override fun onFormatLong(
            valueToFormat: Long,
            unitPowerIndex: Int,
            isMetric: Boolean
        ): String {
            return formatByUnit(String.format("%,d", valueToFormat), unitPowerIndex, isMetric)
        }

        override fun onFormatDouble(
            valueToFormat: Double,
            unitPowerIndex: Int,
            isMetric: Boolean
        ): String {
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
        @IntRange(from = 0L) bytesToFormat: Long,
        bytesFormatter: BytesFormatter = defaultBytesFormatter,
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

/**used for Admob test ads :
 *  val deviceIds = arrayListOf(AdRequest.DEVICE_ID_EMULATOR)
 *  getDeviceHashedId(context)?.let { deviceIds.add(it.uppercase(Locale.ROOT)) }
 *  MobileAds.setRequestConfiguration(RequestConfiguration.Builder().setTestDeviceIds(deviceIds).build())
 *
 *  But also for ads consent testing:
 *
 *  val debugSettings = ConsentDebugSettings.Builder(app)
 *           .setDebugGeography(ConsentDebugSettings.DebugGeography....)
 *           .addTestDeviceHashedId(getDeviceHashedId(app)!!.uppercase())
 *           .build()
 * */
    @SuppressLint("HardwareIds")
    fun getDeviceHashedId(context: Application): String? {
        val md5 = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        try {
            val md = MessageDigest.getInstance("MD5")
            val array = md.digest(md5.toByteArray())
            val sb = StringBuilder()
            for (i in array.indices)
                sb.append(Integer.toHexString(array[i].toInt() and 0xFF or 0x100).substring(1, 3))
            //            Log.d("AppLog", "getDeviceIdForAdMobTestAds:$sb")
            return "$sb"
        } catch (_: NoSuchAlgorithmException) {
        }
        return null
    }
}
