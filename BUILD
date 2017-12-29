java_binary(
  name = "aurinko",
  srcs = glob(["**/*.java"]),
  main_class = "org.aurinkopg.Aurinko",
  deps = ["@postgresql//jar"]
)

java_test(
  name = "aurinko_all_tests",
  deps = ["@junit5_jupiter_api//jar", "@postgresql//jar"],
  srcs = glob(["**/*.java"]),
  test_class = "org.aurinkopg.AurinkoTest"
)
