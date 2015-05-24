(ns stockgame-chestnut.definitions
  )

(def historyKeyVals
  (partition 2 ["Value" "y"]))

(def history-keywords
  [:sym :a :b :c :d :e :f :g])

(def itemKeyVals
  (partition 2 ["Symbol" "symbol"
                "Amount" "amount"
                "Price" "price"
                "Current price" "currentprice"
                "Date" "ts"]))

(def symbolKeyVals
  (partition 2 ["Symbol" "symbol"
                "Name" "name"
                "Exchange" "exchDisp"
                "Type" "typeDisp"]))

(def stockKeyVals
  (partition 2 ["Symbol" "symbol"
                "StockExchange" "StockExchange"
                "Ask" "Ask"
                "Bid" "Bid"
                "AverageDailyVolume" "AverageDailyVolume"
                "BookValue" "BookValue"
                "Currency" "Currency"
                "Change" "Change"
                "YearLow" "YearLow"
                "YearHigh" "YearHigh"
                "MarketCapitalization" "MarketCapitalization"
                "PercentChangeFromYearLow" "PercentChangeFromYearLow"
                "100dayMovingAverage" "HundreddayMovingAverage"
                "50dayMovingAverage" "FiftydayMovingAverage"
                "DividendYield" "DividendYield"
                "Notes" "Notes"
                "PEGRatio" "PEGRatio"
                "ExDividendDate" "ExDividendDate"]))

;;TODO 2010 -> 2015
(def initial-state
  {:idplayer 1
   :chart {:sym "BAS.DE"
           :a 0
           :b 1
           :c 2000
           :d 3
           :e 31
           :f 2010
           :g "w"
           :history []}
   :counter 0
   :timeout nil
   :is-order true
   :current-page nil
   :symbol nil
   :amount nil
   :items nil
   :input-stock nil
   :stocks nil
   :input-symbol nil
   :symbols [{"symbol" "SZG.SG", "name" "SALZGITTER", "exch" "STU", "type" "S", "exchDisp" "Stuttgart", "typeDisp" "Equity"}
             {"symbol" "SZG.MU", "name" "SALZGITTER", "exch" "MUN", "type" "S", "exchDisp" "Munich", "typeDisp" "Equity"}]})

