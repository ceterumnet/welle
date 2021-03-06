(ns clojurewerkz.welle.counters
  (:require [clojurewerkz.welle.core :refer [*riak-client*]]
            [clojurewerkz.welle.conversion :refer :all]
            [clojurewerkz.welle.kv :refer [default-retrier]])
  (:import [com.basho.riak.client.raw StoreMeta FetchMeta DeleteMeta RawClient RiakResponse]
           [com.basho.riak.client.cap Retrier DefaultRetrier ConflictResolver]))


;;
;; API
;;

(defn increment-counter
  "Increment counter in Riak.
   Available options:

  `:value` (default 1): value to increment by
   `:timeout`: query timeout
"
  [^String bucket-name ^String counter-name &{ :keys [w dw pw
                                                      ^long value
                                                       ^Boolean return-body
                                                      ^Integer timeout
                                                      ^Retrier retrier]
                                              :or {value 1
                                                   return-body true
                                                   retrier default-retrier}}]
  (let [^StoreMeta   md (to-store-meta w dw pw return-body nil nil timeout)
        ^Long value (or value 1)
        ^Long result (.attempt retrier ^Callable (fn []
                                                           (.incrementCounter *riak-client* bucket-name counter-name value md)))]
    result))

(defn fetch-counter
  "Fetches Riak counter.
   Available options:

   `:basic-quorum` (true or false): whether to return early in some failure cases (eg. when `:r` is 1 and you get 2 errors and a success `:basic-quorum` set to true would return an error)
   `:notfound-ok` (true or false): whether to treat notfounds as successful reads for the purposes of `:r`
   `:if-modified-vclock`: a vclock instance to use for conditional get. Only supported by Protocol Buffers transport.
   `:timeout`: query timeout
  "
  [^String bucket-name ^String counter &{:keys [r pr not-found-ok basic-quorum
                                                return-deleted-vclock
                                                if-modified-since if-modified-vclock
                                                ^Retrier retrier
                                                ^Integer timeout]
                                         :or {retrier default-retrier}}]
  (let [^FetchMeta    md  (to-fetch-meta r pr not-found-ok basic-quorum nil nil if-modified-since if-modified-vclock timeout)
        ^Long result (.attempt retrier ^Callable (fn []
                                                   (.fetchCounter *riak-client* bucket-name counter md)))]
    result))
