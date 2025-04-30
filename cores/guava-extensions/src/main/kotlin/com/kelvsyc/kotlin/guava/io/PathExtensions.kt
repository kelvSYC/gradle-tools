package com.kelvsyc.kotlin.guava.io

import com.google.common.io.MoreFiles
import java.nio.charset.Charset
import java.nio.file.OpenOption
import java.nio.file.Path

fun Path.asByteSource(vararg options: OpenOption) = MoreFiles.asByteSource(this, *options)

fun Path.asByteSink(vararg options: OpenOption) = MoreFiles.asByteSink(this, *options)

fun Path.asCharSource(charset: Charset = Charset.defaultCharset(), vararg options: OpenOption) = MoreFiles.asCharSource(this, charset, *options)

fun Path.asCharSink(charset: Charset = Charset.defaultCharset(), vararg options: OpenOption) = MoreFiles.asCharSink(this, charset, *options)
