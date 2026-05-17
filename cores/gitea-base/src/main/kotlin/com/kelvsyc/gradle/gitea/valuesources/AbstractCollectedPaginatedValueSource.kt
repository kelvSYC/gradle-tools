package com.kelvsyc.gradle.gitea.valuesources

/**
 * Paginated Gitea API ValueSource that collects all pages into a [List].
 *
 * All pages are fetched sequentially and returned as a complete list. For use cases
 * requiring early termination or partial consumption, extend [AbstractPaginatedValueSource]
 * directly and implement [transform] with the desired [Sequence] operation.
 */
abstract class AbstractCollectedPaginatedValueSource<T : Any, P : AbstractPaginatedValueSource.PaginatedParameters>
    : AbstractPaginatedValueSource<T, List<T>, P>() {

    override fun transform(sequence: Sequence<T>): List<T> = sequence.toList()
}

