(ns ^:figwheel-no-load cygnus-www.dev
  (:require
    [cygnus-www.core :as core]
    [devtools.core :as devtools]))

(enable-console-print!)

(devtools/install!)

(core/init!)
