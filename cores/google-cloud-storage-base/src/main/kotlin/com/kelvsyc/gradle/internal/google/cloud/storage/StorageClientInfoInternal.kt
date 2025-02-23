package com.kelvsyc.gradle.internal.google.cloud.storage

import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import com.kelvsyc.gradle.google.cloud.storage.StorageClientInfo

abstract class StorageClientInfoInternal : StorageClientInfo, ServiceClientInfoInternal<Storage> {
    override fun createClient(): Storage {
        return StorageOptions.newBuilder().apply {
            setProjectId(projectId.get())
            if (credentials.isPresent) {
                setCredentials(credentials.get())
            }
        }.build().service
    }
}
