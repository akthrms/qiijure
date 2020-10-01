(ns qiijure.core
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [cheshire.core :as cheshire]
            [org.httpkit.client :as http]))

(def endpoints
  "resources/endpoints.edn から取得したエンドポイント."
  (for [endpoint (edn/read-string (slurp "resources/endpoints.edn"))]
    (merge {:scheme "https"
            :host   "qiita.com"}
           endpoint)))

(defn- endpoint->url
  "エンドポイントをURLに変換する."
  ([{:keys [scheme host path]}] (str scheme "://" host path))
  ([{:keys [scheme host]} path] (str scheme "://" host path)))

(defmulti request
          "HTTPリクエスト（DELETE/GET/PATCH/POST/PUT）する."
          #(:method %))

(defmethod request :delete [{:keys [url options]}]
  (let [{:keys [body] :as response} @(http/delete url {:query-params options})]
    (assoc response :body (cheshire/parse-string body true))))

(defmethod request :get [{:keys [url options]}]
  (let [{:keys [body] :as response} @(http/get url {:query-params options})]
    (assoc response :body (cheshire/parse-string body true))))

(defmethod request :patch [{:keys [url options]}]
  (let [{:keys [body] :as response} @(http/patch url {:form-params options})]
    (assoc response :body (cheshire/parse-string body true))))

(defmethod request :post [{:keys [url options]}]
  (let [{:keys [body] :as response} @(http/post url {:form-params options})]
    (assoc response :body (cheshire/parse-string body true))))

(defmethod request :put [{:keys [url options]}]
  (let [{:keys [body] :as response} @(http/put url {:form-params options})]
    (assoc response :body (cheshire/parse-string body true))))

(defn- get-root-params
  "パスからルートパラメータを取得する."
  [path]
  (->> path
       (re-seq #":(\w+)")
       (map second)
       (map keyword)))

(defn- path-options-reducer
  "パスのルートパラメータを置換し,オプションからルートパラメータを削除する."
  [[path options] root-param]
  [(str/replace path (str root-param) (str (root-param options)))
   (dissoc options root-param)])

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
        function-name (path->function-name path method)]
    (intern *ns* (symbol function-name)
            (fn [& [{:as options}]]
              (let [root-params (get-root-params path)
                    [reduced-path reduced-options] (reduce path-options-reducer [path options] root-params)]
                (request {:url     (endpoint->url endpoint reduced-path)
                          :method  method
                          :options reduced-options}))))))
