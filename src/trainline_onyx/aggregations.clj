(ns trainline-onyx.aggregations)

(defn init-longest-session-fn
  [window]
  {:session-lengths {}
   :no-of-sessions 0
   :sessions {}})

(defn longest-session-aggregate-fn
  [window state segment]
  (let [no-of-tracks (count (:tracks segment))]
    (if (< (:no-of-sessions state) 50)
      [:add-session {:session-length no-of-tracks
                     :session-id (:session-id segment)
                     :session (:tracks segment)}]
      (let [current-min-length (apply min (keys (:session-lengths state)))]
        (if (> no-of-tracks current-min-length)
          [:purge-then-add {:purge-session-length (first ((:session-lengths state) current-min-length))
                            :add-session {:session-length no-of-tracks
                                          :session-id (:session-id segment)
                                          :session (:tracks-segment)}}])))))

(defn assoc-or-conj
  [m k v]
  (if (m k)
    (update m k #(conj % v))
    (assoc m k [v])))

(defn add-session
  [state value]
  (-> state
      (update :session-lengths assoc-or-conj (:session-length value) (:session-id value))
      (update :no-of-sessions inc)
      (update :sessions assoc (:session-id value) (:session value))))

(defn longest-session-application-fn
  [window state [changelog-type value]]
  (case changelog-type
    :add-session
    (add-session state value)
    :purge-then-add
    (let [purge-session-id (first ((:session-lengths state) (:purge-session-length value)))]
      (-> state
          (update-in [:session-lengths (:purge-session-length value)] #(subvec % 1))
          (update :sessions dissoc purge-session-id)
          (add-session (:add-session value))))))

(def keep-longest-session
  {:aggregation/init init-longest-session-fn
   :aggregation/create-state-update longest-session-aggregate-fn
   :aggregation/apply-state-update longest-session-application-fn})


