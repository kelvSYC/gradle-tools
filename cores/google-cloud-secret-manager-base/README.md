# google-cloud-secret-manager-base

Gradle plugin providing base support for Google Cloud Secret Manager.

Plugin ID: `com.kelvsyc.gradle.google-cloud-secret-manager-base`

## Prerequisites

Apply the `clients-base` plugin (applied automatically by this plugin).

## Client Registration

Register a Secret Manager client via `ClientsBaseExtension`:

```kotlin
the<ClientsBaseExtension>().service.get()
    .registerIfAbsent<SecretManagerClientInfo>("myClient") {
        projectId.set("my-gcp-project")
        // credentials.set(...) // optional; omit to use application default credentials
    }
```

## ValueSource

### `SecretManagerValueSource`

Retrieves a secret version payload as a UTF-8 string.

| Parameter    | Type                | Description                                              |
|--------------|---------------------|----------------------------------------------------------|
| `service`    | `ClientsBaseService`| The clients-base service instance.                       |
| `clientName` | `String`            | Name of the registered `SecretManagerClientInfo` client. |
| `projectId`  | `String`            | GCP project ID.                                          |
| `secretId`   | `String`            | Secret ID.                                               |
| `versionId`  | `String` (optional) | Secret version. Defaults to `"latest"`.                  |

Returns `null` if the secret cannot be retrieved.
