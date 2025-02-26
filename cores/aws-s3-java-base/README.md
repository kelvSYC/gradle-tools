# kelvSYC Gradle Tools - AWS S3 Java Base

This plugin contains Gradle tools that can be used to integrate with Amazon Simple Storage Service (S3), using the
AWS SDK for Java.

## Usage
AWS S3 Java Base extends the Clients Base by adding support for Amazon S3 clients.

```kotlin
plugins {
    id("com.kevlsyc.gradle.aws-s3-java-base")
}
```

## Components
AWS S3 Java Base provides three types of client infos:

* `S3ClientInfo`, corresponding to the synchronous `S3Client` type
* `S3AsyncClientInfo`, corresponding to the asynchronous `S3AsyncClient` type
* `S3TransferManagerClientInfo`, corresponding to the `S3TransferManager` client type

To register a client:

```kotlin
serviceClients.registerAwsS3JavaClient("myClient") {
    region.set(...)
    credentials.set(...)
}
```

Note that it is recommended that, when registering an `S3TransferManagerClientInfo`, that the underlying `S3AsyncClient`
itself be a registered client. This should allow for both clients to be properly cleaned up at the end of a build.

```kotlin
val client = serviceClients.registerAwsS3AsyncJavaClient("myBaseClient") {
    // ...
}
serviceClients.registerAwsS3TransferManagerJavaClient("myClient") {
    baseClient.set(client)
}
```

### `AbstractS3ValueSource`
`AbstractS3ValueSource` is a `ValueSource` type, used in creating `Provider` instances. This class is used to create
`Provider` instances whose values are obtained from Amazon S3.

```kotlin
abstract class MyValueSource : AbstractS3ValueSource<String, AbstractS3ValueSource.Parameters> {
    override fun doObtain(input: ResponseBytes<GetObjectResponse>) {
        // ...
    }
}
```

Note that the retrieved object contents are stored in memory before being converted to the desired data type.

As with other `ValueSource` instances, you can then obtain providers using:

```kotlin
val provider = providers.of(MyValueSource::class) {
    client.set(...)
    bucket.set(...)
    key.set(...)
}
```

Note that it is not required that the supplied client be a registered client, though it is recommended.

These `Provider` instances will only make one call to AWS the first time `get()` is called; the result will be cached
for all subsequent calls to `get()`. The `doObtain()` function is guaranteed to be called once, whenever `get()` is
called the first time.
