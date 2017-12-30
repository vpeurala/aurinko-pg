# Dependencies for the whole project.

# org.opentest4j
maven_jar(
  name = "opentest4j",
  artifact = "org.opentest4j:opentest4j:1.0.0",
  sha1 = "6f09c598e9ff64bf0ce2fa7e7de49a99ba83c0b4"
);

# org.junit.platform
maven_jar(
  name = "junit_platform_commons",
  artifact = "org.junit.platform:junit-platform-commons:1.0.2",
  sha1 = "e348a3a67940694f0718d04f9b0784b3dfb7819b"
);

maven_jar(
  name = "junit_platform_engine",
  artifact = "org.junit.platform:junit-platform-engine:1.0.2",
  sha1 = "ae5d9b8c44d78da636e4e1e00a907fe0fb7bcb8f"
);

maven_jar(
  name = "junit_platform_suite_api",
  artifact = "org.junit.platform:junit-platform-suite-api:1.0.2",
  sha1 = "b91a9bea6dc6f89ca802522c11601e143efdcd08"
);

maven_jar(
  name = "junit_platform_runner",
  artifact = "org.junit.platform:junit-platform-runner:1.0.2",
  sha1 = "330e92ff9f207ced68db099429ef8c6a0d0fbac1"
);

maven_jar(
  name = "junit_platform_launcher",
  artifact = "org.junit.platform:junit-platform-launcher:1.0.2",
  sha1 = "7cd136827fbfe3e6301b95f0340d654ad54feaa4"
);

# org.junit.jupiter
maven_jar(
  name = "junit5_jupiter_api",
  artifact = "org.junit.jupiter:junit-jupiter-api:5.0.2",
  sha1 = "04024737a023e98dc6c9cbbd89fe0dd83975df65"
);

maven_jar(
  name = "junit5_jupiter_engine",
  artifact = "org.junit.jupiter:junit-jupiter-engine:5.0.2",
  sha1 = "e57de973ac81662ec2b9b225df6c882a641fabe6"
);

maven_jar(
  name = "junit5_jupiter_params",
  artifact = "org.junit.jupiter:junit-jupiter-params:5.0.2",
  sha1 = "ccd504f1897e1f8813513efdf994acd644ad3693"
);

# org.postgresql
maven_jar(
  name = "postgresql",
  artifact = "org.postgresql:postgresql:42.1.4",
  sha1 = "1c7788d16b67d51f2f38ae99e474ece968bf715a"
);

# https://github.com/JeffreyFalgout/bazel-junit5
git_repository(
    name = "name_falgout_jeffrey_junit5",
    remote = "https://github.com/JeffreyFalgout/bazel-junit5.git",
    tag = "v0.3.0"
)

load("@name_falgout_jeffrey_junit5//java:junit5.bzl", "junit5_maven_dependencies", "junit5_test_suites")

