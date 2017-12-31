load("@name_falgout_jeffrey_junit5//java:junit5.bzl", "JUNIT5_ARTIFACT_ID_PATTERNS", "JUNIT5_COMPONENTS",
     "JUNIT5_GROUP_IDS", "JUNIT5_RUNTIME_DEPS", "JUNIT5_TEST_DEPS", "TEST_SIZES", "junit5_java_libraries",
     "junit5_java_library", "junit5_maven_dependencies", "junit5_maven_dependency", "junit5_test",
     "junit5_test_library", "junit5_test_suite", "junit5_test_suites"
     )

java_library(
    name = "aurinko_test_lib",
    srcs = glob(["**/*.java"]),
    deps = ["@junit5_jupiter_api//jar", "@junit5_jupiter_engine//jar", "@junit5_jupiter_params//jar",
            "@junit_platform_commons//jar", "@junit_platform_engine//jar", "@junit_platform_launcher//jar",
            "@junit_platform_runner//jar", "@junit_platform_suite_api//jar", "@opentest4j//jar", "@postgresql//jar",
            "//src/main/java:aurinko_main_lib", "@junit4//jar"]
    )

java_test(
    name = "aurinko_test",
    runtime_deps = [":aurinko_test_lib"],
    stamp = 1,
    test_class = "org.aurinkopg.AurinkoTest"
    ) # TODO Remove test_class, use use_testrunner
