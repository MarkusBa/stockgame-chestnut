(ns stockgame-chestnut.server
  (:require [clojure.java.io :as io]
            [stockgame-chestnut.dev :refer [is-dev? inject-devmode-html browser-repl start-figwheel]]
            [stockgame-chestnut.database :as db]
            [clj-http.client :as client]
            [clojure.tools.logging :as log]
            [clojure.data.json :as json]
            [clojure.set :as cljset :refer [subset?]]
            [clojure.string :as cljstr :refer [split]]
            [clojure.test :as ct :refer [is with-test]]
            [compojure.core :refer [GET PUT DELETE POST defroutes]]
            [compojure.route :refer [resources]]
            [net.cgrand.enlive-html :refer [deftemplate]]
            [net.cgrand.reload :refer [auto-reload]]
            [ring.middleware.reload :as reload]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [environ.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]])
  (:import (java.sql Date)
           (java.lang Math)))

(defn date-writer [key value]
  (if (= key :ts)
    (str (java.sql.Date. (.getTime value)))
    value))

(defn generate-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json"}
   :body (pr-str data)})

(defn symbol-from-yahoo [searchstring]
  (let [response (cljstr/replace (:body (client/get (str "http://d.yimg.com/autoc.finance.yahoo.com/autoc?query=" searchstring
                                                         "&callback=YAHOO.Finance.SymbolSuggest.ssCallback")))
                                 #"YAHOO.Finance.SymbolSuggest.ssCallback\((.*?)\)" "$1")]
    (log/info "symbol-from-yahoo: " response)
    response))

(defn getsymbol [{:keys [query] :as params}]
  (generate-response (symbol-from-yahoo query)))

(defn stock-from-yahoo [symbole]
  (let [symbollist (cljstr/join "," (map #(str "\"" % "\"") symbole))
        url "http://query.yahooapis.com/v1/public/yql"
        q-param (str "select * from yahoo.finance.quotes where symbol in(" symbollist ")"  )
        env-param "http://datatables.org/alltables.env"
        format-param "json"
        response (client/get url {:query-params {"q" q-param "env" env-param "format" format-param}} )]
    (log/info "stock-from-yahoo:" response)
    (:body response)))

;;(rt/prices-from-yahoo ["YHOO" "PAH3.DE"]  true)
(with-test
  (defn prices-from-yahoo [symbole sell]
    (let [yhoostocks (get (get (get (json/read-str (stock-from-yahoo symbole))"query") "results")"quote")
          stocks (if (and (not (nil? symbole)) (= 1 (count symbole))) (vector yhoostocks) yhoostocks)
          prices (if sell
                   (map #(vector (get % "symbol") (get % "Bid")) stocks)
                   (map #(vector (get % "symbol") (get % "Ask")) stocks))]
      prices))
  (is (= #{"YHOO" "PAH3.DE"} (reduce #(conj %1 (first %2)) #{} (prices-from-yahoo ["YHOO" "PAH3.DE"]  true))))
  (is (= "YHOO" (first (first (prices-from-yahoo ["YHOO"]  false))))))

(defn items [{:keys [idplayer] :as params}]
  (let [itemsmap (db/get-items idplayer)
        symbols-to-prices (if (= 1 (count itemsmap)) {} (into {} (prices-from-yahoo (filter #(not (= % "CASH")) (map :symbol itemsmap)) true)))
        itemsrich (map #(assoc % :currentprice (get symbols-to-prices (:symbol %))) itemsmap)]
    (generate-response (json/write-str itemsrich :value-fn date-writer))))

(defn name->symbols [companyname]
  (log/info "name->symbols " companyname)
  (map #(get % "symbol") (get (get (json/read-str (symbol-from-yahoo companyname)) "ResultSet") "Result")))

(defn getstock [{:keys [symbole companyname] :as params}]
  (let [actualsymbols (if (cljstr/blank? symbole)
                        (name->symbols companyname)
                        (cljstr/split symbole #"%20"))]
    (log/info "actualsymbols: " actualsymbols)
    (generate-response (stock-from-yahoo actualsymbols))))

(defn order [{:keys [ordersymbol amount idplayer] :as params}]
  (let [price (second (first (prices-from-yahoo [ordersymbol] false)))]
    (log/info "order: " ordersymbol " " amount " " idplayer)
    (generate-response (db/order ordersymbol (Double/parseDouble amount) (Double/parseDouble price) idplayer))))

(defn sell [{:keys [sellsymbol amount idplayer] :as params}]
  (let [price (second (first (prices-from-yahoo [sellsymbol] true)))]
    (log/info "sell: " sellsymbol " " amount " " idplayer)
    (generate-response (db/sell sellsymbol (Double/parseDouble amount) (Double/parseDouble price) idplayer))))

;; TODO remove workaround to show tops 100 elements
;;TODO move to client
(defn cleanup-history [csv]
  (let [stock-values (map #(assoc {} "y" (second (split % #","))) (drop 1 (split csv #"\n")))
        size (count stock-values)
        indexed (map-indexed (fn [idx itm] (assoc itm "z" idx "x" (- size (+ idx 1)))) stock-values)
        lesselements (if (< size 100)
                       indexed
                       (let [n (int (Math/ceil (/ size 100)))]
                         (filter #(= 0 (mod (get % "z") n)) indexed)))]
    (log/info "haho" lesselements)
    lesselements))

;;(rt/history-from-yahoo "BAS.DE" 0 1 2000 0 31 2010 "w")
(defn history-from-yahoo [sym a b c d e f g]
  (let [response (:body (client/get "http://ichart.yahoo.com/table.csv"
                                    {:query-params {:s sym
                                                    :a a
                                                    :b b
                                                    :c c
                                                    :d d
                                                    :e e
                                                    :f f
                                                    :g g
                                                    :ignore ".csv"}}))
        afterwards (cleanup-history response)]
    (log/info "history from yahoo: " response)
    (json/write-str afterwards)))


(defn gethistory [{:keys [sym a b c d e f g] :as params}]
  (generate-response (history-from-yahoo sym a b c d e f g)))



(deftemplate page (io/resource "index.html") []
  [:body] (if is-dev? inject-devmode-html identity))
(comment
  (defroutes routes
             (GET "/" [] (index))
             (GET "/symbol" {params :params}
               (getsymbol params))
             (GET "/history" {params :params}
               (gethistory params))
             (POST "/order" {params :params}
               (order params))
             (POST "/sell" {params :params}
               (sell params))
             (GET "/stock" {params :params}
               (getstock params))
             (GET "/items" {params :params} (items params))
             (route/files "/" {:root "resources/public"}))
  )


(defroutes routes
  (resources "/")
  (resources "/react" {:root "react"})
  (GET "/*" req (page)))

(def http-handler
  (if is-dev?
    (reload/wrap-reload (wrap-defaults #'routes api-defaults))
    (wrap-defaults routes api-defaults)))

(defn run-web-server [& [port]]
  (let [port (Integer. (or port (env :port) 10555))]
    (print "Starting web server on port" port ".\n")
    (run-jetty http-handler {:port port :join? false})))

(defn run-auto-reload [& [port]]
  (auto-reload *ns*)
  (start-figwheel))

(defn run [& [port]]
  (when is-dev?
    (run-auto-reload))
  (run-web-server port))

(defn -main [& [port]]
  (run port))
