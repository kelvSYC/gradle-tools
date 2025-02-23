package com.kelvsyc.gradle.google.cloud.storage

import com.google.auth.oauth2.ServiceAccountCredentials
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

/**
 * Presents a [RegularFile] as a set of Google [ServiceAccountCredentials].
 */
val RegularFile.asServiceAccountCredentials
    get() = ServiceAccountCredentials.fromStream(asFile.inputStream())

/**
 * Presents a [RegularFile] provider as a set of Google [ServiceAccountCredentials].
 */
val Provider<RegularFile>.asServiceAccountCredentials
    get() = map(RegularFile::asServiceAccountCredentials)
