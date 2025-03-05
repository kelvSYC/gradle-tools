rootProject.name = "gradle-tools"

includeBuild("cores/aws-codeartifact-java-base")
includeBuild("cores/aws-imds-java-base")
includeBuild("cores/aws-s3-java-base")
includeBuild("cores/aws-java-extensions")
includeBuild("cores/clients-base")
includeBuild("cores/git-core")
includeBuild("cores/google-cloud-artifact-registry-base")
includeBuild("cores/google-cloud-storage-base")
includeBuild("cores/gradle-extensions")

includeBuild("aggregation")
