package com.kelvsyc.gradle.google.cloud.functions

import com.google.cloud.functions.v2.FunctionServiceClient

/**
 * Test-only [FunctionServiceClientBuildService] that returns a pre-supplied mock client.
 *
 * Set [mockClient] before retrieving the client; the same instance is returned on every call.
 */
abstract class MockFunctionServiceClientBuildService : FunctionServiceClientBuildService() {
    override fun createClient(): FunctionServiceClient =
        checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: FunctionServiceClient? = null
    }
}
