package com.kelvsyc.kotlin.guava.io

import com.google.common.io.FileWriteMode
import com.google.common.io.Files
import java.io.File
import java.nio.charset.Charset

fun File.asByteSource() = Files.asByteSource(this)

fun File.asByteSink(vararg modes: FileWriteMode) = Files.asByteSink(this, *modes)

fun File.asCharSource(charset: Charset = Charset.defaultCharset()) = Files.asCharSource(this, charset)

fun File.asCharSink(charset: Charset = Charset.defaultCharset(), vararg modes: FileWriteMode) = Files.asCharSink(this, charset, *modes)
