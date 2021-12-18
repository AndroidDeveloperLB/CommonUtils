package com.lb.common_utils

fun Array<*>?.sizeSafe() = this?.size ?: 0
fun Collection<*>?.sizeSafe() = this?.size ?: 0
fun Map<*, *>?.sizeSafe() = this?.size ?: 0

fun <T, S> MutableMap<T, S>.putMultipleKeysToSameValue(value: S, vararg keys: T) {
    keys.forEach { key -> this[key] = value }
}
//fun Array<out T>.toArrayList=ArrayList<T>(arr.size).apply { addAll(arr) }
fun <T> Array<out T>.toArrayList() = ArrayList<T>(this.size).apply { addAll(this@toArrayList) }

object CollectionUtil {
    //    fun <T> getArrayListFrom(arr: Array<T>): ArrayList<T> = ArrayList<T>(arr.size).apply { addAll(arr) }
    fun <T> isEmpty(arr: Array<T>?): Boolean {
        return arr == null || arr.isEmpty()
    }

    fun <T> isEmpty(collection: Collection<T>?): Boolean {
        return collection == null || collection.isEmpty()
    }
    //
    //    fun <T, S> isEmpty(map: Map<T, S>?): Boolean {
    //        return map == null || map.isEmpty()
    //    }
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
    //
    //    fun toString(objects: Array<Any>?): String {
    //        if (objects == null)
    //            return "null"
    //        val sb = StringBuilder("{")
    //        var isFirst = true
    //        for (`object` in objects) {
    //            if (!isFirst)
    //                sb.append(',')
    //            else
    //                isFirst = false
    //            sb.append(`object`)
    //        }
    //        sb.append('}')
    //        return sb.toString()
    //    }
    //
    //    fun toString(collection: Collection<*>?): String {
    //        if (collection == null)
    //            return "null"
    //        val sb = StringBuilder("{")
    //        var isFirst = true
    //        for (`object` in collection) {
    //            if (!isFirst)
    //                sb.append(',')
    //            else
    //                isFirst = false
    //            sb.append(`object`)
    //        }
    //        sb.append('}')
    //        return sb.toString()
    //    }
    /**
     * adds all items from src to dst
     */
    fun <T> addAll(src: Array<T>?, dst: MutableCollection<T>) {
        src?.let { java.util.Collections.addAll(dst, *it) }
    }
    //
    //    fun addAll(src: Collection<Long>?, dst: LongArray) {
    //        if (src == null)
    //            return
    //        var i = 0
    //        for (l in src)
    //            dst[i++] = l
    //    }
    //
    //    fun addAll(src: LongArray?, dst: MutableCollection<Long>) {
    //        if (src == null)
    //            return
    //        for (l in src)
    //            dst.add(l)
    //    }
    //
    //    fun addAll(src: IntArray?, dst: MutableCollection<Int>) {
    //        if (src == null)
    //            return
    //        for (l in src)
    //            dst.add(l)
    //    }
    //    /**
    //     * adds all items from src to dst
    //     */
    //
    //    fun <T> addAll(src: Collection<T>?, dst: MutableCollection<T>) {
    //        if (src != null)
    //            dst.addAll(src)
    //    }
}
