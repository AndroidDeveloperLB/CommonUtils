package com.lb.common_utils

import androidx.annotation.UiThread
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceScreen
import androidx.preference.children
import androidx.recyclerview.widget.RecyclerView
import kotlin.sequences.forEach

/**this is used just for hiding the icons of all preferences, and also auto-hide/show of preference categories based on their children*/
abstract class PreferenceFragmentCompatEx(val hideIcons: Boolean = true,
                                          val autoHideAndShowPreferenceCategoriesChildren: Boolean = true) : PreferenceFragmentCompat() {
    private var preferenceListAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>? = null
    private val preferenceAdapterDataObserver = object : RecyclerView.AdapterDataObserver() {
        @UiThread
        override fun onChanged() {
            updatePreferenceCategoryVisibilityIfNeeded()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            updatePreferenceCategoryVisibilityIfNeeded()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            updatePreferenceCategoryVisibilityIfNeeded()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            updatePreferenceCategoryVisibilityIfNeeded()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            updatePreferenceCategoryVisibilityIfNeeded()
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            updatePreferenceCategoryVisibilityIfNeeded()
        }

        private fun updatePreferenceCategoryVisibilityIfNeeded() {
            preferenceScreen.children.filterIsInstance(PreferenceCategory::class.java)
                    .forEach { preferenceCategory: PreferenceCategory ->
                        val shouldBeVisible =
                                preferenceCategory.children.indexOfFirst { it.isVisible } >= 0
                        if (preferenceCategory.isVisible != shouldBeVisible)
                            preferenceCategory.isVisible = shouldBeVisible
                    }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (autoHideAndShowPreferenceCategoriesChildren) {
            preferenceListAdapter?.unregisterAdapterDataObserver(preferenceAdapterDataObserver)
            preferenceListAdapter = null
        }
    }

    override fun onCreateAdapter(preferenceScreen: PreferenceScreen): RecyclerView.Adapter<*> {
        val adapter = super.onCreateAdapter(preferenceScreen)
        if (autoHideAndShowPreferenceCategoriesChildren) {
            adapter.registerAdapterDataObserver(preferenceAdapterDataObserver)
            this.preferenceListAdapter = adapter
        }
        return adapter
    }

    private fun applyOperationsForAllPreferences(preference: Preference) {
        //        https://stackoverflow.com/a/51568782/878126 https://stackoverflow.com/q/15853773/878126
        preference.isIconSpaceReserved = false
        preference.isSingleLineTitle = false
        if (preference is PreferenceGroup)
            for (i in 0 until preference.preferenceCount)
                applyOperationsForAllPreferences(preference.getPreference(i))
    }

    override fun setPreferenceScreen(preferenceScreen: PreferenceScreen?) {
        if (hideIcons)
            preferenceScreen?.let { applyOperationsForAllPreferences(it) }
        super.setPreferenceScreen(preferenceScreen)
    }

}
