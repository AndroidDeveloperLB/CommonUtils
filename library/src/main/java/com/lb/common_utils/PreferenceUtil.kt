package com.lb.common_utils

import android.content.Context
import androidx.annotation.*
import androidx.preference.*
import org.json.*
import java.util.*

fun PreferenceFragmentCompat.findPreference(@StringRes prefKey: Int): Preference =
    findPreference(getString(prefKey))!!

object PreferenceUtil {
    fun prepareListPreference(
        fragment: PreferenceFragmentCompat,
        prefKeyId: Int, //
        @ArrayRes entriesId: Int,
        @ArrayRes valuesId: Int,
        @StringRes defaultValueId: Int,
        listener: OnListPreferenceChosenListener?
    ): ListPreference {
        val entries = fragment.resources.getStringArray(entriesId)
        val values = fragment.resources.getStringArray(valuesId)
        return prepareListPreference(fragment, prefKeyId, entries, values, defaultValueId, listener)
    }

    fun prepareListPreference(
        fragment: PreferenceFragmentCompat,
        @StringRes prefKeyId: Int,
        entries: Array<String>,
        values: Array<String>,
        @StringRes defaultValueId: Int,
        listener: OnListPreferenceChosenListener?
    ): ListPreference {
        val defaultValue = fragment.resources.getString(defaultValueId)
        return prepareListPreference(fragment, prefKeyId, entries, values, defaultValue, listener)
    }

    fun prepareListPreference(
        fragment: PreferenceFragmentCompat,
        @StringRes prefKeyId: Int,
        entries: Array<String>,
        values: Array<String>,
        defaultValue: String?,
        listener: OnListPreferenceChosenListener?
    ): ListPreference {
        val prefKey = fragment.getString(prefKeyId)
        val pref = fragment.findPreference<ListPreference>(prefKey)
        val currentValue = PreferenceManager.getDefaultSharedPreferences(fragment.activity!!)
            .getString(prefKey, null)
        pref!!.setDefaultValue(defaultValue)
        pref.summary = "%s"
        if (currentValue == null)
            pref.value = defaultValue
        pref.entryValues = values
        pref.entries = entries
        pref.setOnPreferenceChangeListener { _, newValue ->
            val newValueStr = newValue.toString()
            return@setOnPreferenceChangeListener listener?.onChosenPreference(prefKey, newValueStr) ?: true
        }
        return pref
    }

    // enum
    inline fun <reified EnumType : Enum<EnumType>> getEnumPref(
        context: Context,
        @StringRes prefKeyResId: Int,
        @StringRes prefDefaultValueResId: Int
    ): EnumType {
        val value = getStringPref(context, prefKeyResId, prefDefaultValueResId)
        return value?.runCatching { enumValueOf<EnumType>(this) }
            ?.getOrNull() ?: enumValueOf(context.getString(prefDefaultValueResId))
    }

    inline fun <reified EnumType : Enum<EnumType>> getEnumPref(
        context: Context,
        @StringRes prefKeyResId: Int,
        prefDefaultValue: EnumType
    ): EnumType {
        val value = getStringPref(context, prefKeyResId, prefDefaultValue.name)
        return value?.runCatching { enumValueOf<EnumType>(this) }
            ?.getOrNull() ?: prefDefaultValue
    }

    fun <EnumType : Enum<EnumType>> putEnumPref(
        context: Context,
        @StringRes prefKeyResId: Int,
        enumValue: EnumType?
    ) {
        putStringPref(context, prefKeyResId, enumValue?.name)
    }
    // enumset
    fun <EnumType : Enum<EnumType>> getEnumSetPref(
        context: Context,
        @StringRes prefKeyResId: Int,
        @StringRes prefDefaultValueResId: Int,
        enumClass: Class<EnumType>
    ): EnumSet<EnumType> {
        val value = getStringPref(context, prefKeyResId, prefDefaultValueResId)
        val result = EnumSet.noneOf(enumClass)
        if (value.isNullOrEmpty())
            return EnumSet.noneOf(enumClass)
        val split = value.split(',').dropLastWhile { it.isEmpty() }.toTypedArray()
        for (str in split)
            if (str.trim { it <= ' ' }.isNotEmpty())
                result.add(java.lang.Enum.valueOf(enumClass, str))
        return result
    }

    fun <EnumType : Enum<EnumType>> putEnumCollectionPref(
        context: Context,
        @StringRes prefKeyResId: Int,
        enumCollection: Collection<EnumType>?
    ) {
        val valToPut: String? = when {
            enumCollection.isNullOrEmpty() -> null
            enumCollection.size == 1 -> enumCollection.iterator().next().name
            else -> {
                val sb = StringBuilder()
                for (enumVal in enumCollection)
                    sb.append(enumVal.name).append(',')
                sb.toString()
            }
        }
        putStringPref(context, prefKeyResId, valToPut)
    }

    //enum list
    fun <EnumType : Enum<EnumType>> getEnumListPref(
        context: Context, @StringRes prefKeyResId: Int, @StringRes prefDefaultValueResId: Int,
        enumClass: Class<EnumType>
    ): List<EnumType> {
        val value = getStringPref(context, prefKeyResId, prefDefaultValueResId)
        val result = ArrayList<EnumType>()
        if (value.isNullOrEmpty())
            return result
        val split = value.split(',').dropLastWhile { it.isEmpty() }.toTypedArray()
        for (str in split)
            if (str.trim { it <= ' ' }.isNotEmpty())
                result.add(java.lang.Enum.valueOf(enumClass, str))
        return result
    }

    //enum list
    fun <EnumType : Enum<EnumType>> getEnumListPref(
        context: Context,
        @StringRes prefKeyResId: Int,
        enumClass: Class<EnumType>
    ): List<EnumType>? {
        if (!hasPreference(context, prefKeyResId))
            return null
        val value = getStringPref(context, prefKeyResId, 0)
        val result = ArrayList<EnumType>()
        if (value.isNullOrEmpty())
            return result
        val split = value.split(',').dropLastWhile { it.isEmpty() }.toTypedArray()
        for (str in split)
            if (str.trim { it <= ' ' }.isNotEmpty())
                result.add(java.lang.Enum.valueOf(enumClass, str))
        return result
    }

    // string
    fun getStringPref(context: Context, prefKey: String, defaultValue: String?): String? =
        PreferenceManager.getDefaultSharedPreferences(context)
            .getString(prefKey, defaultValue)

    fun getStringPref(
        context: Context,
        @StringRes prefKeyResId: Int,
        @StringRes prefDefaultValueResId: Int
    ): String? {
        val prefKey = context.getString(prefKeyResId)
        val defaultValue = if (prefDefaultValueResId == 0) null else context.resources.getString(
            prefDefaultValueResId
        )
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(prefKey, defaultValue)
    }

    fun getStringPref(
        context: Context,
        @StringRes prefKeyResId: Int,
        defaultValue: String?
    ): String? {
        val prefKey = context.getString(prefKeyResId)
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(prefKey, defaultValue)
    }

    fun putStringPref(context: Context, @StringRes prefKeyResId: Int, newValue: String?) {
        val prefKey = context.getString(prefKeyResId)
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences.edit().putString(prefKey, newValue).apply()
    }

    // boolean
    fun getBooleanPref(
        context: Context,
        @StringRes prefKeyResId: Int,
        @BoolRes prefDefaultValueResId: Int
    ): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            context.getString(prefKeyResId),
            context.resources.getBoolean(prefDefaultValueResId)
        )
    }

    fun getBooleanPref(
        context: Context,
        prefKey: String,
        @BoolRes prefDefaultValueResId: Int
    ): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(prefKey, context.resources.getBoolean(prefDefaultValueResId))
    }

    fun getBooleanPref(context: Context, prefKey: String, defaultValue: Boolean): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(prefKey, defaultValue)
    }
    // boolean
    fun getBooleanPref(
        context: Context,
        @StringRes prefKeyResId: Int,
        defaultValue: Boolean
    ): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(context.getString(prefKeyResId), defaultValue)
    }

    fun putBooleanPref(context: Context, @StringRes prefKeyResId: Int, newValue: Boolean) {
        val prefKey = context.getString(prefKeyResId)
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences.edit().putBoolean(prefKey, newValue).apply()
    }

    // int
    fun getIntPrefOrDefaultIntFromResId(
        context: Context,
        @StringRes prefKeyResId: Int,
        @IntegerRes prefDefaultValueResId: Int
    ): Int {
        val prefKey = context.getString(prefKeyResId)
        val defaultValue = if (prefDefaultValueResId == 0) -1 else context.resources.getInteger(
            prefDefaultValueResId
        )
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(prefKey, defaultValue)
    }

    fun getIntPref(context: Context, @StringRes prefKeyResId: Int, defaultValue: Int): Int {
        val prefKey = context.getString(prefKeyResId)
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(prefKey, defaultValue)
    }

    /**
     * returns the int pref, or -1 if not available
     */
    fun getIntPref(context: Context, @StringRes prefKeyResId: Int): Int? {
        val prefKey = context.getString(prefKeyResId)
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return if (!preferences.contains(prefKey)) null else preferences.getInt(prefKey, -1)
    }

    fun putIntPref(context: Context, @StringRes prefKeyResId: Int, newValue: Int) {
        val prefKey = context.getString(prefKeyResId)
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences.edit().putInt(prefKey, newValue).apply()
    }

    fun putLongPref(context: Context, @StringRes prefKeyResId: Int, newValue: Long) {
        val prefKey = context.getString(prefKeyResId)
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences.edit().putLong(prefKey, newValue).apply()
    }

    fun getLongPref(context: Context, @StringRes prefKeyResId: Int): Long? {
        val prefKey = context.getString(prefKeyResId)
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return if (!preferences.contains(prefKey)) null else preferences.getLong(prefKey, -1)
    }

    // dimen
    fun getDimenAsStringPref(
        context: Context,
        @StringRes prefKeyResId: Int,
        prefDefaultValueResId: Int
    ): Float {
        val prefKey = context.getString(prefKeyResId)
        val res = context.resources
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val string = preferences.getString(prefKey, null)
            ?: return res.getDimension(prefDefaultValueResId) / res.displayMetrics.density
        return java.lang.Float.parseFloat(string)
    }

    fun putDimenAsStringPref(context: Context, @StringRes prefKeyResId: Int, newValue: Float) {
        putStringPref(context, prefKeyResId, newValue.toString())
    }

    // string set
    fun putStringCollection(
        context: Context,
        @StringRes prefKeyResId: Int,
        newValue: Collection<String>?
    ) {
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        val key = context.getString(prefKeyResId)
        if (newValue == null)
            editor.remove(key).apply()
        else
            editor.putString(key, JSONArray(newValue).toString()).apply()
    }

    fun getStringSet(context: Context, @StringRes prefKeyResId: Int): Set<String>? {
        val key = context.getString(prefKeyResId)
        val str = PreferenceManager.getDefaultSharedPreferences(context).getString(key, null)
            ?: return null
        try {
            val jsonArray = JSONArray(str)
            val result = HashSet<String>()
            for (i in 0 until jsonArray.length())
                result.add(jsonArray.getString(i))
            return result
        } catch (e: JSONException) {
            e.printStackTrace()
            PreferenceManager.getDefaultSharedPreferences(context).edit().remove(key).apply()
        }

        return null
    }

    // preference existence
    fun hasPreference(context: Context, @StringRes prefKeyResId: Int): Boolean {
        val prefKey = context.getString(prefKeyResId)
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.contains(prefKey)
    }

    // preference deletion
    fun removePreference(context: Context, @StringRes prefKeyResId: Int) {
        val prefKey = context.getString(prefKeyResId)
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences.edit().remove(prefKey).apply()
    }

    fun removePreference(context: Context, prefKey: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().remove(prefKey).apply()
    }

    fun buildPreferenceParentTree(preferenceScreen: PreferenceScreen): Map<Preference, PreferenceGroup> {
        val result = HashMap<Preference, PreferenceGroup>()
        val curParents = Stack<PreferenceGroup>()
        curParents.add(preferenceScreen)
        while (!curParents.isEmpty()) {
            val parent = curParents.pop()
            val childCount = parent.preferenceCount
            for (i in 0 until childCount) {
                val child = parent.getPreference(i)
                result[child] = parent
                if (child is PreferenceGroup)
                    curParents.push(child)
            }
        }
        return result
    }

    fun interface OnListPreferenceChosenListener {
        fun onChosenPreference(key: String, value: String): Boolean
    }
}
