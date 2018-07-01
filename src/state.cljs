(ns cygnus-www.state
  (:require-macros [cygnus-www.macros :refer [defui forall defpage]])
  (:require [cygnus-www.core :as core]
            [reagent.core :as reagent]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]))

(defonce page (reagent/atom #'core/index))

(defn current-page []
  [:div [@page]])
