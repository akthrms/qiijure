(ns qiijure.auth)

(def access-token-env-key
  "環境変数キー."
  "QIITA_API_ACCESS_TOKEN")

(defn generate-credentials
  "認証を生成する."
  [access-token]
  {"Authorization" (str "Bearer " access-token)})

(defn env->credentials
  "環境変数を認証に変換する."
  []
  (let [access-token (System/getenv access-token-env-key)]
    (generate-credentials access-token)))
