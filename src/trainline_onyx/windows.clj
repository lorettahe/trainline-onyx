(ns trainline-onyx.windows)

(def windows
  [{:window/id          :make-sessions
    :window/task        :make-session-fn                    ;change later
    :window/type        :session
    :window/aggregation :onyx.windowing.aggregation/conj
    :window/window-key  :timestamp
    :window/session-key :user-id
    :window/timeout-gap [20 :minutes]}

   #_{:window/id          :make-longest-sessions
    :window/task        :clojure.core/identity              ;change later
    :window/type        :global
    :window/aggregation :trainline-onyx.aggregations/keep-longest-session}

   #_{:window/id          :make-most-frequency-songs
    :window/task        :clojure.core/identity              ;change later
    :window/type        :global
    :window/aggregation :trainline-onyx.aggregations/keep-most-frequent-songs}])
