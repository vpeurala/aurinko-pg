java_binary(
    name = "aurinko",
    main_class = "org.aurinkopg.Aurinko",
    runtime_deps = [":aurinko_lib"]
    );

java_library(
    name = "aurinko_lib",
    runtime_deps = ["//src/main/java:aurinko_main_lib"]
    );

java_test(
    name = "aurinko_all_tests",
    test_class = "org.aurinkopg.AurinkoTest",
    srcs = glob(["**/*.java"]),
    deps = ["@junit4//jar", "@postgresql//jar"]
    );

