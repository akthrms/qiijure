(ns qiijure.api
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [cheshire.core :as cheshire]
            [org.httpkit.client :as http]))

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

(defmulti request
          "HTTPリクエスト（DELETE/GET/PATCH/POST/PUT）する."
          #(:method %))

(defmethod request :delete [{:keys [url headers params]}]
  (let [{:keys [body] :as response} @(http/delete url {:headers headers :query-params params})]
    (assoc response :body (cheshire/parse-string body true))))

(defmethod request :get [{:keys [url headers params]}]
  (let [{:keys [body] :as response} @(http/get url {:headers headers :query-params params})]
    (assoc response :body (cheshire/parse-string body true))))

(defmethod request :patch [{:keys [url headers params]}]
  (let [{:keys [body] :as response} @(http/patch url {:headers headers :form-params params})]
    (assoc response :body (cheshire/parse-string body true))))

(defmethod request :post [{:keys [url headers params]}]
  (let [{:keys [body] :as response} @(http/post url {:headers headers :form-params params})]
    (assoc response :body (cheshire/parse-string body true))))

(defmethod request :put [{:keys [url headers params]}]
  (let [{:keys [body] :as response} @(http/put url {:headers headers :form-params params})]
    (assoc response :body (cheshire/parse-string body true))))

(defn- get-route-params
  "パスからルートパラメータを取得する."
  [path]
  (->> path
       (re-seq #":(\w+)")
       (map second)
       (map keyword)))

(defn- path-params-reducer
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
        metadata {:arglists '([& {:keys [credentials params] :as options-map}]) :doc doc}
        function-name (path->function-name path method)]
    (intern *ns* (with-meta (symbol function-name) metadata)
            (fn [& [{:keys [credentials params]}]]
              (let [route-params (get-route-params path)
                    [reduced-path reduced-params] (reduce path-params-reducer [path params] route-params)]
                (request {:url     (endpoint->url endpoint reduced-path)
                          :method  method
                          :headers credentials
                          :params  reduced-params}))))))
