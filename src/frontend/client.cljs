(ns frontend.client
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.dom :as gdom]
            [cljs.core.async :as async :refer [<! >! put! chan]]
            [clojure.string :as string]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom])
  (:import [goog Uri]
           [goog.net Jsonp]))

(enable-console-print!)

(def ESCAPE_KEY 27)
(def ENTER_KEY 13)
(def base-url
  "http://en.wikipedia.org/w/api.php?action=opensearch&format=json&search=")


(defmulti read om/dispatch)

(defmethod read :search/results
  [{:keys [state ast] :as env} k {:keys [query]}]
  (merge
    {:value (get @state k [])}
    (when-not (or (string/blank? query)
                  (< (count query) 3))
      {:search ast})))


(defn submit [c {:keys [db/id todo/title] :as props} e]
  (let [edit-text (string/trim (or (om/get-state c :edit-text) ""))]

    (om/transact! c
                  (cond-> '[(todo/cancel-edit)]
                    (= :temp id)
                    (conj '(todos/delete-temp))
                    (not (string/blank? edit-text))
                    (into
                     `[(search/update {:db/id ~id :search/query ~edit-text})
                       '[:queries/by-id ~id]])))
    (doto e (.preventDefault) (.stopPropagation))))

(defn edit [c {:keys [db/id todo/title] :as props}]
  (om/transact! c `[(todo/edit {:db/id ~id})])
  (om/update-state! c merge {:needs-focus true :edit-text title}))

(defn key-down [c props e]
  (condp == (.-keyCode e)
    ESCAPE_KEY
      (do
        (om/transact! c '[(app/cancel-edit)])
        (om/update-state! c assoc :edit-text "")
        (doto e (.preventDefault) (.stopPropagation)))
    ENTER_KEY
      (submit c props e)
    nil))

(defn change [c e]
  (om/set-query! c
                 {:params {:query (.. e -target -value)}}))

(defn jsonp
  ([uri] (jsonp (chan) uri))
  ([c uri]
   (let [gjsonp (Jsonp. (Uri. uri))]
     (.send gjsonp nil #(put! c %))
     c)))

(defn result-list [results]
  (dom/ul #js {:key "result-list"}
    (map #(dom/li nil %) results)))

(defn search-field [c query]
  (dom/input
   #js {:key "search-field"
        :value query
        :onChange #(change c %)}))

(defui AutoCompleter
  static om/IQueryParams
  (params [_]
          {:query ""})

  static om/IQuery
  (query [_]
         '[(:search/results {:query ?query})])

  Object
  (render [this]
          (let [{:keys [search/results]} (om/props this)]
            (dom/div nil
                     (dom/h2 nil "Autocompleter")
                     (cond->
                         [(search-field this (:query (om/get-params this)))]
                         (seq results) (conj (result-list results)))))))

(defn search-loop [ch]
  (go
    (loop [[query cb] (<! ch)]
      (let [[_ results] (<! (jsonp (str base-url query)))]
        (cb {:search/results results}))
      (recur (<! ch)))))

(defn send-to-chan [ch]
  (fn [{:keys [search]} cb]
    (when search
      (let [{[search] :children} (om/query->ast search)
            query (get-in search [:params :query])]
        (put! ch [query cb])))))

(def send-chan (chan))

(def reconciler
  (om/reconciler
    {:state   {:search/results []}
     :parser  (om/parser {:read read})
     :send    (send-to-chan send-chan)
     :remotes [:remote :search]}))

(search-loop send-chan)

(om/add-root! reconciler AutoCompleter
  (gdom/getElement "app"))
