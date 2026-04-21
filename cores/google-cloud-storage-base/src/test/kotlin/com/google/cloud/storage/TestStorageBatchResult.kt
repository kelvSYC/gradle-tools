package com.google.cloud.storage

/**
 * Test-only subclass of [StorageBatchResult] that exposes [success] and [error] for use in unit tests.
 */
class TestStorageBatchResult : StorageBatchResult<Blob>() {
    public override fun success(result: Blob?) = super.success(result)
    public override fun error(error: StorageException) = super.error(error)
}
