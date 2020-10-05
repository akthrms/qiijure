(ns qiijure.endpoints-test
  (:require [clojure.test :refer :all]
            [qiijure.api :as qiijure]))

(deftest test-access-tokens
  (is (resolve 'qiijure/post-access-tokens))
  (is (resolve 'qiijure/delete-access-tokens-access-token)))

(deftest test-authenticated-user
  (is (resolve 'qiijure/get-authenticated-user))
  (is (resolve 'qiijure/get-authenticated-user-items)))

(deftest test-comments
  (is (resolve 'qiijure/delete-comments-comment-id))
  (is (resolve 'qiijure/get-comments-comment-id))
  (is (resolve 'qiijure/patch-comments-comment-id))
  (is (resolve 'qiijure/delete-comments-comment-id-thank))
  (is (resolve 'qiijure/put-comments-comment-id-thank)))

(deftest test-items
  (is (resolve 'qiijure/get-items))
  (is (resolve 'qiijure/post-items))
  (is (resolve 'qiijure/delete-items-item-id))
  (is (resolve 'qiijure/get-items-item-id))
  (is (resolve 'qiijure/patch-items-item-id))
  (is (resolve 'qiijure/get-items-item-id-comments))
  (is (resolve 'qiijure/post-items-item-id-comments))
  (is (resolve 'qiijure/delete-items-item-id-stock))
  (is (resolve 'qiijure/get-items-item-id-stock))
  (is (resolve 'qiijure/put-items-item-id-stock))
  (is (resolve 'qiijure/get-items-item-id-stockers)))

(deftest test-oauth
  (is (resolve 'qiijure/get-oauth-authorize)))

(deftest test-tags
  (is (resolve 'qiijure/get-tags))
  (is (resolve 'qiijure/get-tags-tag-id))
  (is (resolve 'qiijure/delete-tags-tag-id-following))
  (is (resolve 'qiijure/get-tags-tag-id-following))
  (is (resolve 'qiijure/put-tags-tag-id-following))
  (is (resolve 'qiijure/get-tags-tag-id-items)))

(deftest test-users
  (is (resolve 'qiijure/get-users))
  (is (resolve 'qiijure/get-users-user-id))
  (is (resolve 'qiijure/get-users-user-id-followees))
  (is (resolve 'qiijure/get-users-user-id-followers))
  (is (resolve 'qiijure/delete-users-user-id-following))
  (is (resolve 'qiijure/get-users-user-id-following))
  (is (resolve 'qiijure/put-users-user-id-following))
  (is (resolve 'qiijure/get-users-user-id-following-tags))
  (is (resolve 'qiijure/get-users-user-id-items))
  (is (resolve 'qiijure/get-users-user-id-stocks)))
