java_library(
    name = "aurinko_test_lib",
    srcs = glob(["**/*.java"]),
    deps = ["@postgresql//jar", "//src/main/java:aurinko_main_lib", "@junit4//jar"]
    );

java_test(
    name = "aurinko_test",
    runtime_deps = [":aurinko_test_lib"],
    stamp = 1,
    test_class = "org.aurinkopg.AurinkoTest"
    ); # TODO Remove test_class, use use_testrunner
