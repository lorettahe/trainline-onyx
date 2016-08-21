(ns trainline-onyx.core
  (:require [onyx.api :as onyx]
            [onyx.job :refer [add-task]]
            [onyx.tasks.core-async :as ca]
            [onyx.tasks.seq :as seq]
            [onyx.plugin.core-async :refer [take-segments! get-core-async-channels]]
            [clojure.core.async :as async]
            [trainline-onyx.catalogs :refer [build-catalog]]
            [trainline-onyx.lifecycles :refer [bind-inputs! build-lifecycles collect-outputs!]]
            [trainline-onyx.windows :refer [windows]]
            [trainline-onyx.functions]
            [trainline-onyx.triggers :refer [build-triggers session-ch]]))

(defn std-out [config event-map]
  ;; `config` is the `monitoring-config` var.
  (prn event-map))

(def monitoring-config
  {:monitoring :custom
   :zookeeper-write-log-entry std-out
   :zookeeper-read-log-entry std-out
   :zookeeper-write-catalog std-out
   :zookeeper-write-workflow std-out
   :zookeeper-write-flow-conditions std-out
   :zookeeper-force-write-chunk std-out
   :zookeeper-read-catalog std-out
   :zookeeper-read-lifecycles std-out
   :zookeeper-gc-log-entry std-out})

(def workflow
  [[:read-file :parse-tsv]
   [:parse-tsv :write-tsv]])

(def id (java.util.UUID/randomUUID))

(def env-config
  {:zookeeper/address "127.0.0.1:2188"
   :zookeeper/server? true
   :zookeeper.server/port 2188
   :onyx.bookkeeper/server? true
   :onyx.bookkeeper/local-quorum? true
   :onyx.bookkeeper/local-quorum-ports [3196 3197 3198]
   :onyx/tenancy-id id})

(def peer-config
  {:zookeeper/address "127.0.0.1:2188"
   :onyx/tenancy-id id
   :onyx.peer/job-scheduler :onyx.job-scheduler/balanced
   :onyx.messaging/impl :aeron
   :onyx.messaging/peer-port 40200
   :onyx.messaging/bind-addr "localhost"})

(def batch-size 10)

(def capacity 1000)

(defn -main
  [& args]
  (let [env (onyx/start-env env-config monitoring-config)
        peer-group (onyx/start-peer-group peer-config)
        n-peers (count (set (mapcat identity workflow)))
        v-peers (onyx/start-peers n-peers peer-group)
        lifecycles (build-lifecycles)
        base-job {:workflow       workflow
                  :catalog        (build-catalog batch-size)
                  :lifecycles     []
                  :task-scheduler :onyx.task-scheduler/balanced}
        job (-> base-job
                (add-task (seq/buffered-file-reader :read-file "test.tsv"
                                                    {:onyx/batch-size 10 :onyx/batch-timeout 1000}))
                (add-task (ca/output :write-tsv {:onyx/batch-size 10 :onyx/batch-timeout 1000})))]

    (onyx/submit-job
      peer-config
      job)
    (println "job submitted")

    (let [{:keys [write-tsv]} (get-core-async-channels job)]
      (loop []
        (when-let [segment (async/<!! write-tsv)]
          (when (not= segment :done)
            (clojure.pprint/pprint segment)
            (recur)))))
    (println "after collect-outputs")

    #_(when-let [session (async/<!! session-ch)]
        (println session))

    (doseq [v-peer v-peers]
      (onyx.api/shutdown-peer v-peer))

    (onyx.api/shutdown-peer-group peer-group)

    (onyx.api/shutdown-env env)

    (shutdown-agents)))
