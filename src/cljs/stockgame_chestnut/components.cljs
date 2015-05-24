(ns stockgame-chestnut.components
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [stockgame-chestnut.definitions :as cd :refer [itemKeyVals
                                             historyKeyVals
                                             symbolKeyVals
                                             stockKeyVals]]
            [reagent.core  :as r]
            [re-frame.core :as rf :refer [subscribe
                                          dispatch
                                          dispatch-sync
                                          register-sub]]))

;; ----components without subscriptions-----

(defn tablizer [items keyVals]
  (let [headers (map first itemKeyVals)]
    [:table
     ;;heading
     [:tr
      (for [[k v] keyVals]
        [:th
         k])]
     (for [item items]
       ^{:key item} [:tr
                     (for [[k v] keyVals]
                       [:td
                        (get item v)])])]))

(defn lister [items keyVals]
  [:ul
   (for [item items]
     ^{:key item} [:li
                   [:ul.datalist
                    (for [[k v] keyVals]
                      ^{:key k}
                      [:li
                       [:span {:style {:display "inline-block" :width "250px" :text-align "left"}} k ]
                       [:span {:style {:display "inline-block" :text-align "left"}} (get item v)]])]])])

(defn tableview [tablename listkeyword keyVals items]
  [:div
   [:h1 tablename]
   [tablizer items keyVals]])

(defn listview [listname listkeyword keyVals items]
  [:div
   [:h1 listname]
   [lister items keyVals]])

(defn home [my-data]
  (fn [my-data]
    (println "rendering home" my-data)
    [:div [:h1 "Chart"]
     [:div#d3-node [:svg {:style {:width "1200" :height "600"}}]]
     ]))

(defn home-did-mount [my-data]
  (println "did-mount" my-data)
  (.addGraph js/nv (fn []
                     (let [chart (.. js/nv -models lineChart
                                     (margin #js {:left 100})
                                     (useInteractiveGuideline true)
                                     (transitionDuration -1)
                                     (showLegend true)
                                     (showYAxis true)
                                     (showXAxis false))]
                       (.. chart -xAxis
                           (axisLabel "x-axis")
                           (tickFormat (.format js/d3 ",r")))
                       (.. chart -yAxis
                           (tickFormat (.format js/d3 ",r")))

                       (.. js/d3 (select "#d3-node svg")
                           (datum (clj->js [{:values  my-data
                                             :color "red"
                                             }]))
                           (call chart))))))

(defn mychart [_]
  (let [chart (subscribe [:chart])
        my-data (reaction (:history @chart))]
    (println "mychart" @my-data)
    (r/create-class {:reagent-render #(home @my-data)
                     :display-name  "my-chart"
                     :component-did-update (fn []
                                             ;;(.. js/document (getElementById "d3-node") -firstChild remove)
                                             ;;(println my-data)
                                             (home-did-mount @my-data))
                     :component-did-mount #(home-did-mount @my-data)})))


;; ----components--------

;;TODO rename items keyword

(defn atom-input-blur [place oldvalue atomkeyword querykey]
  [:input {:type "text"
           :placeholder place
           :value oldvalue
           :on-change #(let [text (-> % .-target .-value)]
                        (dispatch [:handle-search text atomkeyword querykey]))}])

(defn symbols []
  (let [input-symbol (subscribe [:input-symbol])
        symbolslist  (subscribe [:symbols])]
    (fn []
      [:div
       [atom-input-blur "symbol" @input-symbol :input-symbol :symbolquery]
       [tableview "Symbols" :symbols symbolKeyVals @symbolslist]])))

(defn stocks []
  (let [input-stock (subscribe [:input-stock])
        stockslist  (subscribe [:stocks])]
    (fn []
      [:div
       [atom-input-blur "company-name" @input-stock :input-stock :stockquery]
       [tableview "Stock" :stocks stockKeyVals @stockslist]])))

(defn atom-input [place oldvalue path-to-value]
  [:input {:type "text"
           :placeholder place
           :value oldvalue
           :on-change #(let [text (-> % .-target .-value)]
                        (dispatch [:assoc-in-db path-to-value text]))}])

(defn items []
  (let [amount (subscribe [:amount])
        items (subscribe [:items])
        idplayer (subscribe [:idplayer])
        is-order (subscribe [:is-order])
        smbl (subscribe [:symbol])]
    (fn []
      [:div
       [:div
        [:h2 (if @is-order "Order" "Sell")]
        [:input {:type "checkbox"
                 :checked @is-order
                 :on-change #(dispatch [:input-changed :is-order (not @is-order)])}
         "Order"] [:br]
        [atom-input "symbol" @smbl [:symbol]] [:br]
        [atom-input "amount" @amount [:amount]] [:br]
        [:input {:type "button" :value "Commit"
                 :on-click #(dispatch
                             [(if @is-order :orderquery :sellquery) @idplayer @smbl @amount])}]]
       [tableview "Items" :items itemKeyVals @items]])))

;;TODO refactor for less repetitions
(defn history []
  (let [chart (subscribe [:chart])
        his (reaction (:history @chart))
        sym (reaction (:sym @chart))
        a (reaction (:a @chart))
        b (reaction (:b @chart))
        c (reaction (:c @chart))
        d (reaction (:d @chart))
        e (reaction (:e @chart))
        f (reaction (:f @chart))
        g (reaction (:g @chart))]
    (fn []
      [:div
       [:div
        [:h2 "History"]
        [atom-input "symbol" @sym [:chart :sym]] [:br]
        [atom-input "from-month" @a [:chart :a]] [:br]
        [atom-input "from-day" @b [:chart :b]] [:br]
        [atom-input "from-year" @c [:chart :c]] [:br]
        [atom-input "to-month" @d [:chart :d]] [:br]
        [atom-input "to-day" @e [:chart :e]] [:br]
        [atom-input "to-year" @f [:chart :f]] [:br]
        [atom-input "stepsize" @g [:chart :g]] [:br]
        [:input {:type "button" :value "Submit"
                 :on-click #(dispatch
                             [:historyquery @sym @a @b @c @d @e @f @g])}]
        [mychart nil @his]
        ]
       ])))

(defn pageToKeyword [page current-page]
  (if (= page current-page) :div.navelement :div.navelement-link))


(defn actual-content []
  (let [page (subscribe [:current-page])]
    (fn []
      [:div.all
       [:div.navigation
        [(pageToKeyword items @page)
         {:on-click #(dispatch ^:flush-dom [:input-changed :current-page items])}
         "Portfolio"]
        [(pageToKeyword stocks @page)
         {:on-click #(dispatch  ^:flush-dom [:input-changed :current-page stocks])}
         "Stocks"]
        [(pageToKeyword symbols @page)
         {:on-click #(dispatch ^:flush-dom [:input-changed :current-page symbols])}
         "Symbols"]
        [(pageToKeyword history @page)
         {:on-click #(dispatch ^:flush-dom [:input-changed :current-page history])}
         "History"]
        ]
       [:br]
       [:div.content
        [@page]
        ]])))

(defn content []
  [:div
   [actual-content]])