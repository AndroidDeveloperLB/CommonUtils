package com.lb.common_utils

import java.text.*

fun Array<*>?.sizeSafe() = this?.size ?: 0
fun Collection<*>?.sizeSafe() = this?.size ?: 0
fun Map<*, *>?.sizeSafe() = this?.size ?: 0

fun <T, S> MutableMap<T, S>.putMultipleKeysToSameValue(value: S, vararg keys: T) {
    keys.forEach { key -> this[key] = value }
}

fun <T> Array<out T>.toArrayList() = ArrayList<T>(this.size).apply { addAll(this@toArrayList) }



fun ArrayList<String>.sortUsingCollator(collator: Collator = Collator.getInstance()) {
    if (size <= 1)
        return
    val hashMap = HashMap<String, CollationKey>(size)
    sortWith { o1, o2 ->
        val key1 = hashMap.getOrPut(o1) {
            collator.getCollationKey(o1)
        }
        val key2 = hashMap.getOrPut(o2) {
            collator.getCollationKey(o2)
        }
        key1.compareTo(key2)
    }
}

fun <T : Comparable<T>> ArrayList<T>.sortUsingCollator(collator: Collator = Collator.getInstance(), getStringValue: (input: T) -> String) {
    if (size <= 1)
        return
    val hashMap = HashMap<String, CollationKey>(size)
    sortWith { o1, o2 ->
        val o1Str = getStringValue(o1)
        val o2Str = getStringValue(o2)
        val key1 = hashMap.getOrPut(o1Str) {
            collator.getCollationKey(o1Str)
        }
        val key2 = hashMap.getOrPut(o2Str) {
            collator.getCollationKey(o2Str)
        }
        key1.compareTo(key2)
    }
}


object CollectionUtil {
    fun <T> isEmpty(arr: Array<T>?): Boolean {
        return arr == null || arr.isEmpty()
    }

    fun <T> isEmpty(collection: Collection<T>?): Boolean {
        return collection == null || collection.isEmpty()
    }

    fun <T> size(collection: Collection<T>?): Int {
        return collection?.size ?: 0
    }

    /**
     * returns true iff all of the items on each set exist on the other set .
     */
    fun <T> areSetsIdentical(set1: Set<T>?, set2: Set<T>?): Boolean {
        // both are the same , so return true
        if (set1 === set2)
            return true
        // size is different so return false
        if (size(set1) != size(set2))
            return false
        // both are empty , so return true
        if (isEmpty(set1))
            return true
        // size is the same , so compare items
        for (t in set1!!)
            if (!set2!!.contains(t))
                return false
        return true
    }

    /**
     * adds all items from src to dst
     */
    fun <T> addAll(src: Array<T>?, dst: MutableCollection<T>) {
        src?.let { java.util.Collections.addAll(dst, *it) }
    }
}
