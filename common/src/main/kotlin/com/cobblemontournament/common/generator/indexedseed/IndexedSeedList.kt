package com.cobblemontournament.common.generator.indexedseed

import com.someguy.collections.SortType
import org.slf4j.helpers.Util

data class IndexedSeedList(private var list: List<IndexedSeed> = mutableListOf()) {

    constructor(
        indexedSeeds: Collection<IndexedSeed>,
        sortType: SortType = SortType.INDEX_ASCENDING
    ): this () {
        val tempList = mutableListOf<IndexedSeed>()
        for (indexedSeed in indexedSeeds) {
            tempList.add(IndexedSeed(index = indexedSeed.index, seed = indexedSeed.seed))
        }
        this.list = tempList
        when (sortType) {
            SortType.INDEX_ASCENDING -> sortByIndexAscending()
            SortType.INDEX_DESCENDING -> sortByIndexDescending()
            SortType.VALUE_ASCENDING -> sortBySeedAscending()
            SortType.VALUE_DESCENDING -> sortBySeedDescending()
            else -> return
        }
    }

    var sortType = SortType.UNKNOWN
        private set

    fun size() = list.size

    fun deepCopy(): List<IndexedSeed> {
        val result = mutableListOf<IndexedSeed>()
        list.forEach { result.add(IndexedSeed(it.index, it.seed)) }
        return result
    }

    fun getSeed(index: Int) = list.firstOrNull { it.seed == index }?.seed

    fun getIndex( seed: Int) = list.firstOrNull { it.seed == seed }?.index

    private fun sortByIndexAscending() {
        list =  list.sortedBy { it.index }
        sortType = SortType.INDEX_ASCENDING
    }

    private fun sortByIndexDescending() {
        list =  list.sortedByDescending { it.index }
        sortType = SortType.INDEX_DESCENDING
    }

    fun sortBySeedAscending() {
        list =  list.sortedBy { it.seed }
        sortType = SortType.VALUE_ASCENDING
    }

    private fun sortBySeedDescending() {
        list = list.sortedByDescending { it.seed }
        sortType = SortType.VALUE_DESCENDING
    }

    fun print() = list.forEach { Util.report(it.toString()) }

}
