(ns stockgame_chestnut.test-runner
  (:require
   [cljs.test :refer-macros [run-tests]]
   [stockgame_chestnut.core-test]))

(enable-console-print!)

(defn runner []
  (if (cljs.test/successful?
       (run-tests
        'stockgame_chestnut.core-test))
    0
    1))
