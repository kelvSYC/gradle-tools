rootProject.name = "gradle-tools"

// Gradle components
includeBuild("cores/artifactory-base")
includeBuild("cores/aws-codeartifact-java-base")
includeBuild("cores/aws-codeartifact-kotlin-base")
includeBuild("cores/aws-imds-java-base")
includeBuild("cores/aws-imds-kotlin-base")
includeBuild("cores/aws-java-extensions")
includeBuild("cores/aws-kotlin-extensions")
includeBuild("cores/aws-s3-java-base")
includeBuild("cores/aws-s3-kotlin-base")
includeBuild("cores/aws-secrets-manager-java-base")
includeBuild("cores/aws-secrets-manager-kotlin-base")
includeBuild("cores/aws-ses-java-base")
includeBuild("cores/aws-ses-kotlin-base")
includeBuild("cores/aws-sns-java-base")
includeBuild("cores/aws-sns-kotlin-base")
includeBuild("cores/aws-sqs-java-base")
includeBuild("cores/aws-sqs-kotlin-base")
includeBuild("cores/clients-base")
includeBuild("cores/commons-lang-extensions")
includeBuild("cores/git-core")
includeBuild("cores/google-cloud-artifact-registry-base")
includeBuild("cores/google-cloud-storage-base")
includeBuild("cores/gradle-extensions")

// Kotlin extension components
includeBuild("extensions/commons-numbers-extensions")
includeBuild("extensions/guava-extensions")

includeBuild("aggregation")
