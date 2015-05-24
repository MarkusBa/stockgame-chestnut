(ns stockgame-chestnut.handlers
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require  [cognitect.transit :as transit]
             [cljs-http.client :as http]
             [stockgame-chestnut.definitions :refer [initial-state]]
             [cljs.core.async :refer [<!]]
             [re-frame.core  :refer [dispatch
                                     register-handler]]))

(enable-console-print!)

(def json-reader (transit/reader :json))

;;helper functions for cleaning the arriving json
(defn read-items-response [response]
  (let [temp1 (:body response)
        temp (transit/read json-reader temp1)]
    temp))

(defn read-symbol-response [response]
  (let [temp1 (:body response)
        temp (transit/read json-reader temp1)
        text (get (get temp "ResultSet") "Result")]
    (println "type: " (type text))
    text))

(defn read-stock-response [response]
  (let [temp1 (:body response)
        temp (transit/read json-reader temp1)
        text (get (get (get temp "query") "results")"quote")]
    (if (vector? text) text (vector text))))

(defn read-history-response [response]
  (let [temp1 (:body response)
        _ (println response)
        temp (transit/read json-reader temp1)]
    ;;(println temp)
    temp
    ))

;;handlers

(register-handler
  :initialize
  (fn [db [_ idplayer]]
    (dispatch [:itemquery idplayer])
    (merge db initial-state)))

(register-handler
  :itemquery
  (fn [db [_ idplayer]]
    (go (let [response (<! (http/get "items" {:query-params {"idplayer" idplayer}}))]
          (dispatch [:input-changed :items (read-items-response response)])))
    db))

(register-handler
  :orderquery
  (fn [db [_ idplayer ordersymbol orderamount]]
    (go (let [response (<! (http/post "order" {:form-params {"idplayer" idplayer "ordersymbol" ordersymbol "amount" orderamount}}))]
          (println response)
          (dispatch  [:itemquery idplayer])))
    db))

(register-handler
  :sellquery
  (fn [db [_ idplayer sellsymbol sellamount]]
    (go (let [response (<! (http/post "sell" {:form-params {"idplayer" idplayer "sellsymbol" sellsymbol "amount" sellamount}}))]
          (println response)
          (dispatch  [:itemquery idplayer])))
    db))

(register-handler
  :symbolquery
  (fn [db [_ param]]
    (go (let [response (<! (http/get "symbol" {:query-params {"query" param}}))]
          (dispatch [:input-changed :symbols (read-symbol-response response)])))
    db))

(register-handler
  :stockquery
  (fn [db [_ param]]
    (go (let [response (<! (http/get "stock" {:query-params {"companyname" param}}))]
          (dispatch ^:flush-dom [:input-changed :stocks (read-stock-response response)])))
    db))

(register-handler
  :historyquery
  (fn [db [_ sym a b c d e f g]]
    (println sym a b c d e f g)
    (go (let [response (<! (http/get "history" {:query-params {"sym" sym "a" a "b" b "c" c "d" d "e" e "f" f "g" g}}))]

          (dispatch [:assoc-in-db [:chart :history] (read-history-response response)])))
    db))

;; TODO refactor away
(register-handler
  :handle-actual-search
  (fn [db [_ correspkeyword querykey]]
    (let [param (correspkeyword db)]
      (dispatch [querykey param]))))

(register-handler
  :handle-timeout
  (fn [db [_ correspkeyword querykey]]
    (let [timeout (:timeout db)]
      (if timeout ((.-clearTimeout js/window) timeout)))
    (assoc db :timeout
              ((.-setTimeout js/window)
                (fn [] (dispatch [:handle-actual-search correspkeyword querykey]))
                600  ))))


(register-handler
  :handle-search
  (fn [db [_ text correspkeyword querykey]]
    (dispatch [:handle-timeout correspkeyword querykey])
    (assoc db correspkeyword text)))

(register-handler
  :input-changed
  (fn [db [_ inputkey text]]
    (assoc db inputkey text)))

(register-handler
  :assoc-in-db
  (fn [db [_ inputvector text]]
    (println inputvector text)
    (assoc-in db inputvector text)))

(register-handler
  :input-changed-force
  (fn [db [_ inputkey text]]
    ;;TODO
    (println inputkey text)
    (assoc db inputkey text)
    db))

(declare render-page)

(register-handler
  :nothing
  (fn [db [_]]
    db))