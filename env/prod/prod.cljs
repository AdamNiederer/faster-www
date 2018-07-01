(ns cygnus-www.prod
  (:require
    [cygnus-www.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
