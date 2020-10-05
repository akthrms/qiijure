(ns qiijure.auth-test
  (:require [clojure.test :refer :all]
            [qiijure.auth :refer [generate-credentials]]))

(deftest test-generate-credentials
  (let [access-token "test"]
    (is {"Authorization" (str "Bearer " access-token)}
        (generate-credentials access-token))))
