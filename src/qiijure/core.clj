(ns qiijure.core
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [cheshire.core :as cheshire]
            [org.httpkit.client :as http])
  (:import (com.sun.tools.hat.internal.model Root)))

(def access-token (atom nil))

(defn set-access-token
  "アクセストークンを設定する."
  [token]
  (reset! access-token token))

(def endpoints
  "`resources/endpoints.edn`から取得したエンドポイント."
  (for [endpoint (edn/read-string (slurp "resources/endpoints.edn"))]
    (merge {:scheme "https"
            :host   "qiita.com"}
           endpoint)))

(defn- endpoint->url
  "エンドポイントをURLに変換する."
  ([{:keys [scheme host path]}] (str scheme "://" host path))
  ([{:keys [scheme host]} path] (str scheme "://" host path)))

(defn- merge-options
  "アクセストークンを設定している場合はオプションにマージする."
  [options]
  (if @access-token (merge {:headers {"Authorization" (str "Bearer " @access-token)}} options)
                    options))

(defmulti request
          "HTTPリクエスト（DELETE/GET/PATCH/POST/PUT）する."
          #(:method %))

(defmethod request :delete [{:keys [url params]}]
  (let [{:keys [body] :as response} @(http/delete url (merge-options {:query-params params}))]
    (assoc response :body (cheshire/parse-string body true))))

(defmethod request :get [{:keys [url params]}]
  (let [{:keys [body] :as response} @(http/get url (merge-options {:query-params params}))]
    (assoc response :body (cheshire/parse-string body true))))

(defmethod request :patch [{:keys [url params]}]
  (let [{:keys [body] :as response} @(http/patch url (merge-options {:form-params params}))]
    (assoc response :body (cheshire/parse-string body true))))

(defmethod request :post [{:keys [url params]}]
  (let [{:keys [body] :as response} @(http/post url (merge-options {:form-params params}))]
    (assoc response :body (cheshire/parse-string body true))))

(defmethod request :put [{:keys [url params]}]
  (let [{:keys [body] :as response} @(http/put url (merge-options {:form-params params}))]
    (assoc response :body (cheshire/parse-string body true))))

(defn- get-route-params
  "パスからルートパラメータを取得する."
  [path]
  (->> path
       (re-seq #":(\w+)")
       (map second)
       (map keyword)))

(defn- path-options-reducer
  "パスのルートパラメータを置換し,オプションからルートパラメータを削除する."
  [[path options] route-param]
  (if-let [replacement (get options route-param)]
    [(str/replace path (str route-param) (str replacement))
     (dissoc options route-param)]
    (throw (IllegalArgumentException. (str "Route parameter " route-param " is required.")))))

(defn- path->function-name
  "パスを関数名に変換する."
  [path method]
  (-> path
      (subs 8)
      (str/replace #"[^a-zA-Z0-9]+" "-")
      (as-> p (str (name method) "-" p))))

(doseq [endpoint endpoints]
  (let [method (:method endpoint)
        path (:path endpoint)
        doc (str "Calling the Qiita endpoint: " (str/upper-case (name method)) " " (endpoint->url endpoint)
                 "\n  adding `options-map` to the request.")
        metadata {:arglists '([& {:as options-map}]) :doc doc}
        function-name (path->function-name path method)]
    (intern *ns* (with-meta (symbol function-name) metadata)
            (fn [& [{:as options}]]
              (let [route-params (get-route-params path)
                    [reduced-path reduced-params] (reduce path-options-reducer [path options] route-params)]
                (request {:url    (endpoint->url endpoint reduced-path)
                          :method method
                          :params reduced-params}))))))
