package com.kelvsyc.gradle

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.sequences.shouldBeEmpty
import io.kotest.matchers.sequences.shouldHaveSize
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import org.gradle.api.Named
import org.gradle.kotlin.dsl.namedDomainObjectList
import org.gradle.testfixtures.ProjectBuilder

class DomainObjectContainerExtensionsSpec : FunSpec() {
    interface NamedItem : Named

    init {
        test("asSequence empty") {
            val project = ProjectBuilder.builder().build()
            val collection = project.objects.namedDomainObjectList(NamedItem::class)

            val sequence = collection.asSequence()

            sequence.shouldBeEmpty()
        }

        test("asSequence element") {
            val project = ProjectBuilder.builder().build()
            val collection = project.objects.namedDomainObjectList(NamedItem::class)
            val item = mockk<NamedItem>(relaxed = true)
            every { item.name } returns "item"
            collection.add(item)

            val sequence = collection.asSequence()

            sequence.shouldHaveSize(1)
            val provider = sequence.first()
            provider.name shouldBeEqual item.name
            provider.get() shouldBeSameInstanceAs item
        }
    }
}
