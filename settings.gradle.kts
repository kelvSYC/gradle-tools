rootProject.name = "gradle-tools"

includeBuild("cores/aws-codeartifact-java-base")
includeBuild("cores/aws-s3-java-base")
includeBuild("cores/clients-base")
includeBuild("cores/git-core")
includeBuild("cores/google-cloud-storage-base")
includeBuild("cores/gradle-extensions")

includeBuild("aggregation")
