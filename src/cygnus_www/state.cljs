(ns cygnus-www.state
  (:require [reagent.core :as reagent]
            [secretary.core :as secretary :include-macros true]))

(defn default-page []
  [:div [:h2 "About cygnus-www"]
   [:div [:a {:href "/"} "go to the home page"]]])

(defonce page (reagent/atom #'default-page))

(defn current-page []
  [:div [@page]])
