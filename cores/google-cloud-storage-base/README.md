# kelvSYC Gradle Tools - Google Cloud Storage Base

This plugin contains Gradle tools that can be used to integrate with Google Cloud Stoarge.

## Usage
Google Cloud Storage Base extends the Clients Base by adding support for GCS.

```kotlin
plugins {
    id("com.kevlsyc.gradle.google-cloud-storage-base")
}
```

## Components
The client info type of a Google `Storage` client is a `StorageClientInfo`. To register a client:

```kotlin
serviceClients.registerGoogleCloudStorageClient("myClient") {
    projectId.set(...)
    credentials.set(...)
}
```

### `BatchDownloadFromGCS`
The `BatchDownloadFromGCS` task downloads a collection of artifacts from Google Cloud Storage.

```kotlin
tasks.register<BatchDownloadFromGCS>("myTask") {
    clientName.set("myClient")
    
    registerArtifact("myArtifact") {
        bucketName.set(...)
        blobName.set(...)
        outputFile.set(...)
    }
}
```

The `clientName` must be a registered `StorageClientInfo` from the Clients Base service. Artifact names are not used in
the task, and is useful only if intending to wire the output of this task to the inputs of other tasks. The
`outputFiles` property can be used for this purpose.

All registered artifacts are downloaded in a single batched request, and the task fails if any registered artifact
cannot be downloaded.

If you have a client supplied from elsewhere, you can use the `AbstractBatchDownloadFromGCS` task, which is otherwise
identical, except that the underlying client is supplied through the `client` property.

### `AbstractGCSValueSource`
`AbstractGCSValueSource` is a `ValueSource` type, used in creating `Provider` instances. This class is used to create
`Provider` instances whose values are obtained from GCS.

```kotlin
abstract class MyValueSource : AbstractGCSValueSource<String, AbstractGCSValueSource.Parameters> {
    override fun doObtain(input: ByteArray) {
        // ...
    }
}
```

Note that the retrieved blob contents are stored in memory before being converted to the desired data type.

As with other `ValueSource` instances, you can then obtain providers using:

```kotlin
val provider = providers.of(MyValueSource::class) {
    clientName.set(...)
    bucketName.set(...)
    blobName.set(...)
}
```

These `Provider` instances will only make one call to GCS the first time `get()` is called; the result will be cached
for all subsequent calls to `get()`. The `doObtain()` function is guaranteed to be called once, whenever `get()` is
called the first time.
