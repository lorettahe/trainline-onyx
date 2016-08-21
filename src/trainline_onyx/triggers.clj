(ns trainline-onyx.triggers
  (:require [clojure.core.async :as async]
            [trainline-onyx.lifecycles :as lifecycles]))

(def session-ch (async/chan 1000))

(defn write-to-ch-factory
  [ch]
  (fn [event window-id lower-bound upper-bound state]
    (let [state-count (count state)]
      (async/>!! ch {:count  state-count
                     :tracks (reduce
                               (fn [acc event]
                                 (conj acc (:track event)))
                               [] state)}))))

(def write-to-session-ch!
  (write-to-ch-factory session-ch))

(defn build-triggers
  []
  [{:trigger/window-id  :make-sessions
    :trigger/refinement :onyx.refinements/accumulating
    :trigger/on         :onyx.triggers/watermark
    :trigger/sync       ::write-to-session-ch!}

   #_{:trigger/window-id  :make-longest-sessions
    :trigger/refinement :onyx.refinements/accumulating
    :trigger/on         :onyx.triggers/watermark
    :trigger/sync       ::write-to-longest-session-ch!}

   #_{:trigger/window-id :make-most-frequency-songs
    :trigger/refinement :onyx.refinements/accumulating
    :trigger/on :onyx.triggers/watermark
    :trigger/sync ::write-to-most-frequent-songs-ch!}])
