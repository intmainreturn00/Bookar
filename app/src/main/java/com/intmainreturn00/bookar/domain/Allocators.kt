package com.intmainreturn00.bookar.domain

import kotlin.math.max


const val paperbackWidth = 0.1524f // 6''
const val paperbackHeight = 0.2286f // 9''

enum class AllocationType { GridType, TowersType }

sealed class Allocator {

    companion object {
        fun getInstance(t: AllocationType) = when (t) {
            AllocationType.GridType -> Grid
            AllocationType.TowersType -> Towers
        }
    }

    abstract fun allocate(books: List<Book>): List<ARBook>

    private fun makeDepth(pages: Int?): Float = (pages ?: 600) * 0.00006f + 0.002f

    protected fun makeSize(pages: Int?) = MyVector3(
        paperbackWidth + (-10..10).random() / 1000f,
        makeDepth(pages),
        paperbackHeight + (-10..10).random() / 1000f
    )

    protected fun makeAngle(baseRotation: Float = 0f) =
        MyQuaternion(0.0f, 1.0f, 0.0f, baseRotation + (-7..+7).random())
}


private object Grid : Allocator() {

    private val gridOffsets = listOf(
        Pair(-1.5f, -1.5f), Pair(-0.5f, -1.5f), Pair(0.5f, -1.5f), Pair(1.5f, -1.5f),
        Pair(-1.5f, -0.5f), Pair(-0.5f, -0.5f), Pair(0.5f, -0.5f), Pair(1.5f, -0.5f),
        Pair(-1.5f, 0.5f), Pair(-0.5f, 0.5f), Pair(0.5f, 0.5f), Pair(1.5f, 0.5f),
        Pair(-1.5f, 1.5f), Pair(-0.5f, 1.5f), Pair(0.5f, 1.5f), Pair(1.5f, 1.5f)
    )

    override fun allocate(books: List<Book>): List<ARBook> {
        val res = mutableListOf<ARBook>()
        var x: Float
        var z: Float
        var zLayer = 0
        var i = 0
        val elevationMap = MutableList(gridOffsets.size) { 0f }

        for (book in books) {

            x = gridOffsets[i].first * (paperbackWidth + 0.03f)
            z = gridOffsets[i].second * (paperbackHeight + 0.03f)

            res.add(
                ARBook(
                    size = makeSize(book.pages),
                    position = MyVector3(x, elevationMap[i], z),
                    rotation = makeAngle(180f),
                    bookModel = book
                )
            )

            elevationMap[i] += res[res.size - 1].size.y

            if (elevationMap[i] > 1.6f) {
                claimNLastTop(res, gridOffsets.size)
                return res
            }

            if (i == gridOffsets.size - 1) {
                zLayer++
                i = 0
            } else {
                i++
            }

        }

        claimNLastTop(res, gridOffsets.size)
        return res
    }

    private fun claimNLastTop(arbooks: List<ARBook>, n: Int) {
        for (i in (max(arbooks.size - n, 0)) until arbooks.size)
            arbooks[i].isTopBook = true
    }

}


object Towers : Allocator() {

    private val towerOffsets = listOf(
        Pair(-0.7f, -0.7f), Pair(0.7f, -0.7f), Pair(-0.7f, 0.7f), Pair(0f, 0f), Pair(0.7f, 0.7f)
    )

    override fun allocate(books: List<Book>): List<ARBook> {
        val res = mutableListOf<ARBook>()
        var x: Float
        var z: Float
        var towerNum = 0

        val elevationMap = MutableList(towerOffsets.size) { 0f }

        for (book in books) {

            x = towerOffsets[towerNum].first * (paperbackWidth + 0.03f)
            z = towerOffsets[towerNum].second * (paperbackHeight + 0.03f)

            res.add(
                ARBook(
                    size = makeSize(book.pages),
                    position = MyVector3(x, elevationMap[towerNum], z),
                    rotation = makeAngle(180f + 30f),
                    bookModel = book
                )
            )

            elevationMap[towerNum] += res[res.size - 1].size.y

            if (elevationMap[towerNum] > 1.5f - towerNum * 0.1f) {
                ++towerNum
                res[res.size - 1].isTopBook = true
            }

            if (towerNum == towerOffsets.size)
                return res


        }

        return res
    }
}