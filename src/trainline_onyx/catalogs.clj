(ns trainline-onyx.catalogs)

(defn build-catalog
  [batch-size]
  [#_{:onyx/name :read-file
    :onyx/plugin :onyx.plugin.core-async/input
    :onyx/type :input
    :onyx/medium :core.async
    :onyx/max-peers 1
    :onyx/batch-size batch-size
    :onyx/doc "Reads segments from a core.async channel"}

   {:onyx/name :parse-tsv
    :onyx/fn :trainline-onyx.functions/parse-tsv
    :onyx/type :function
    :onyx/batch-size batch-size}

   {:onyx/name :make-session-fn
    :onyx/fn :clojure.core/identity
    :onyx/type :function
    :onyx/uniqueness-key :event-id
    :onyx/batch-size batch-size}

   #_{:onyx/name :write-tsv
    :onyx/plugin :onyx.plugin.core-async/output
    :onyx/type :output
    :onyx/medium :core.async
    :onyx/batch-size batch-size
    :onyx/max-peers 1}

   #_{:onyx/name :create-sessions
    :onyx/fn :onyx-starter.functions.sample-functions/create-sessions
    :onyx/type :function
    :onyx/batch-timeout batch-timeout
    :onyx/batch-size batch-size}

   #_{:onyx/name :get-longest-sessions
    :onyx/fn :onyx-starter.functions.sample-functions/get-longest-sessions
    :onyx/type :function
    :onyx/batch-timeout batch-timeout
    :onyx/batch-size batch-size}

   #_{:onyx/name :get-played-songs
    :onyx/fn :onyx-starter.functions.sample-functions/get-played-songs
    :onyx/type :function
    :onyx/batch-timeout batch-timeout
    :onyx/batch-size batch-size}

   #_{:onyx/name :get-max-played-songs
    :onyx/fn :onyx-starter.functions.sample-functions/get-max-played-songs
    :onyx/type :function
    :onyx/batch-timeout batch-timeout
    :onyx/batch-size batch-size}

   #_{:onyx/name :write-tsv
    :onyx/plugin :onyx.plugin.core-async/output
    :onyx/type :output
    :onyx/medium :core.async
    :onyx/max-peers 1
    :onyx/batch-timeout batch-timeout
    :onyx/batch-size batch-size
    :onyx/doc "Writes segments to a core.async channel"}])
