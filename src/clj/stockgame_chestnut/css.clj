(ns stockgame-chestnut.css
  (:require [garden.core :refer [css]]
            [garden.units :refer [px em percent]]))

;; (require '[html.css :as cs] :reload)
;; (cs/generateCSS)

(def all
  [:.all {:overflow "auto"}])

(def content
  [:.content {:margin-left "20px" :margin-top "40px"}])

(def ul
  [:.datalist {:list-style-type "none"}])

(def left
  [:.left {:float "left"}])

(def right
  [:.right {:float "right"}])

(def th
  [:th {:background-color "#ccc"}])

(def td
  [:td {:background-color "#eee"}])

(def navelement-link
  [:.navelement-link
   {:float "left"
    :color "blue"
    :cursor "pointer"
    :border "1px"
    :margin "5px"
    :font-weight "bold"}])



(def navelement
  [:.navelement
   {:float "left"
    :color "black"
    :cursor "default"
    :border "1px"
    :margin "5px"
    :font-weight "bold"}])

(defn generateCSS []
  (css
    {:pretty-print true :output-to "resources/public/main.css"}
    ul
    all
    content
    left
    right
    th
    td
    navelement-link
    navelement))
