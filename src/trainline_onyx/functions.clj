(ns trainline-onyx.functions
  (:require [clojure-csv.core]
            [clj-time.format :as f]))

(def timestamp-format (f/formatters :date-time-no-ms))

(defn make-track-id
  [tokens]
  (if (seq (aget tokens 4))
    (aget tokens 4)
    (if (seq (aget tokens 2))
      (str (aget tokens 2) "\t" (aget tokens 5))
      (str (aget tokens 3) "\t" (aget tokens 5)))))

(defn parse-tsv
  [{:keys [line]}]
  (println line)
  (let [tokens (.split line "\t")]
    {:event-id (java.util.UUID/randomUUID)
     :user-id (first tokens)
     :timestamp (f/parse timestamp-format (second tokens))
     :track (make-track-id tokens)}))
