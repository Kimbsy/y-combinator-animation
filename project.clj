(defproject y-combinator-animation "0.1.0"
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [quip "2.0.4"]]
  :main ^:skip-aot y-combinator-animation.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
