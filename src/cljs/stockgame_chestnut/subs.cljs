(ns stockgame-chestnut.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as rf :refer [register-sub]]
            [stockgame-chestnut.components :as cp :refer [items]]
            [stockgame-chestnut.handlers :as handlers]
            ))

(enable-console-print!)

;; subscriptions

;; TODO are those necessary?
(defn- reg! [kw]
  (register-sub
    kw
    (fn [db [_]]
      (reaction
        (let [valu (get-in @db [kw])]
          valu)))))


(reg! :symbols)
(reg! :stocks)
(reg! :items)
(reg! :input-symbol)
(reg! :input-stock)
(reg! :amount)
(reg! :symbol)
(reg! :is-order)
(reg! :idplayer)

(register-sub
  :current-page
  (fn [db [_]]
    (reaction
      (let [current-page (get-in @db [:current-page])
            page (if (nil? current-page) items current-page)]
        page))))

(reg! :counter)

(reg! :chart)

(comment
  (register-sub
    :sym
    (fn [db [chart & rest]]
      (reaction
        (let [ret (get-in @db [:chart :sym])]
          ret))))
  )


(comment
  (register-sub
    :chart
    (fn [db [chart & rest]]
      (reaction
        (let [ret (get-in @db `[:chart ~@rest])]
          ret))))

  )

