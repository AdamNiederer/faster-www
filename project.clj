(defproject cygnus-www "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.238"]
                 [reagent "0.7.0"]
                 [secretary "1.2.3"]
                 [venantius/accountant "0.2.4"
                  :exclusions [org.clojure/tools.reader]]]

  :plugins [;[lein-environ "1.1.0"]
            [deraen/lein-sass4clj "0.3.1"]
            [lein-cljsbuild "1.1.5"]
            [lein-figwheel "0.5.15"]
            [lein-asset-minifier "0.2.7"
             :exclusions [org.clojure/clojure]]]

  :min-lein-version "2.5.0"
  :clean-targets ^{:protect false}
  [:target-path
   [:cljsbuild :builds :app :compiler :output-dir]
   [:cljsbuild :builds :app :compiler :output-to]]

  :resource-paths ["public"]

  :minify-assets {:assets
                  {"public/css/site.min.css" "public/css/site.css"}}

  :sass {:source-paths ["public/sass"]
         :target-path "public/css"}

  :figwheel {:http-server-root "."
             :nrepl-port 7002
             :nrepl-middleware ["cemerick.piggieback/wrap-cljs-repl"]
             :css-dirs ["public/css"]}

  :cljsbuild {:builds {:app
                       {:source-paths ["src" "env/dev"]
                        :compiler
                        {:main "cygnus-www.dev"
                         :output-to "public/js/app.js"
                         :output-dir "public/js/out"
                         :asset-path "js/out"
                         :source-map true
                         :optimizations :none
                         :pretty-print  true}
                        :figwheel
                        {:on-jsload "cygnus-www.core/mount-root"
                         :open-urls ["http://localhost:3449/index.html"]}}
                       :release
                       {:source-paths ["src" "env/prod"]
                        :compiler
                        {:output-to "public/js/app.js"
                         :optimizations :advanced
                         :pretty-print false}}}}

  :aliases {"package" ["do" "clean" ["cljsbuild" "once" "release"]]
            "dev" ["pdo" ["sass4clj auto"] ["figwheel"]]}

  :profiles {:dev {:dependencies [[binaryage/devtools "0.9.7"]
                                  [figwheel-sidecar "0.5.15"]
                                  [org.clojure/tools.nrepl "0.2.13"]
                                  [com.cemerick/piggieback "0.2.2"]
                                  [org.slf4j/slf4j-nop "1.7.25"]]
                   :plugins [[lein-figwheel "0.5.14"]
                             [cider/cider-nrepl "0.15.1"]
                             [org.clojure/tools.namespace "0.3.0-alpha4"
                              :exclusions [org.clojure/tools.reader]]
                             [refactor-nrepl "2.3.1"
                              :exclusions [org.clojure/clojure]]
                             [deraen/lein-sass4clj "0.3.1"]]
                   :repl-options {:init-ns cygnus-www.repl
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}})
