(ns cygnus-www.new.prod
  (:require
    [cygnus-www.new.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
