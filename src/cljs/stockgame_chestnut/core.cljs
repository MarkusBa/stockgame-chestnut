(ns stockgame-chestnut.core
  (:require [reagent.core  :as r]
            [stockgame-chestnut.components :as cp :refer [content]]
            [stockgame-chestnut.subs :as subs]                            ;;executed before
            [re-frame.core :as rf :refer [dispatch-sync]]))

(enable-console-print!)

(defn render-page []
  (r/render
    [content]
    (.-body js/document (getElementById "app"))))

(defn run []
  (dispatch-sync ^:flush-dom [:initialize 1])
  (render-page))

(run)
