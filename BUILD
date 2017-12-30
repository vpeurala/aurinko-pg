java_binary(
    name = "aurinko",
    main_class = "org.aurinkopg.Aurinko",
    runtime_deps = [":aurinko_lib"]
)

java_library(
    name = "aurinko_lib",
    runtime_deps = ["//src/main/java:aurinko_main_lib"]
)

java_test(
  name = "aurinko_all_tests",
  test_class = "org.aurinkopg.AurinkoTest",
  srcs = glob(["**/*.java"]),
  deps = ["@junit5_jupiter_api//jar", "@junit5_jupiter_engine//jar", "@junit5_jupiter_params//jar", "@junit_platform_commons//jar", "@junit_platform_engine//jar", "@junit_platform_launcher//jar", "@junit_platform_runner//jar", "@junit_platform_suite_api//jar", "@opentest4j//jar", "@postgresql//jar"]
)
