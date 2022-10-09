(ns spotify-auth.core
  (:require [org.httpkit.server :as server]
            [org.httpkit.client :as client]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer :all]
            [ring.util.response :refer (redirect)]
            [clojure.pprint :as pp]
            [clojure.string :as str]
            [clojure.data.json :as json]
            [cemerick.url :refer (url)]
            [dotenv.core :as dotenv]
            [random-string.core]
            [base64-clj.core :as base64])
  (:use [random-string.core :only [string] :rename {string random-string}])
  (:gen-class))

(def env (atom dotenv/env))

(defn get-env []
  @env)

(def SPOTIFY_CLIENT_ID (:SPOTIFY_CLIENT_ID (get-env)))
(def SPOTIFY_CLIENT_SECRET (:SPOTIFY_CLIENT_SECRET (get-env)))
(def SPOTIFY_REDIRECT_URI (:SPOTIFY_REDIRECT_URI (get-env)))

(def auth_code_url
  (->
   (url "https://accounts.spotify.com/authorize")
   (assoc
    :query {:response_type "code"
            :redirect_uri SPOTIFY_REDIRECT_URI
            :client_id SPOTIFY_CLIENT_ID
            :scope "playlist-modify-private"
            :state (random-string 12)})
   (str)))


(def AUTH-BASIC-TOKEN
  (base64/encode
   (str
    SPOTIFY_CLIENT_ID
    ":"
    SPOTIFY_CLIENT_SECRET)))

(defn get-spotify-access-token [code]
  @(client/request
    {:url "https://accounts.spotify.com/api/token"
     :method :post
     :headers {"Authorization" (str "Basic " AUTH-BASIC-TOKEN) "Content-Type" "application/form-data"}
     :form-params {"code" code
                   "redirect_uri"  SPOTIFY_REDIRECT_URI
                   "grant_type" "authorization_code"}
     :body (json/write-str {"code" code
                            "redirect_uri"  SPOTIFY_REDIRECT_URI
                            "grant_type" "authorization_code"})}))

; Hello page
(defn hello [req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "Hello World"})

(defn auth-start [req]
  (redirect auth_code_url))

; Auth flow callback
(defn auth-callback [req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (->>
             (str
              "Access code response:"  (:body (get-spotify-access-token (:code (:params req))))))})

(defroutes app-routes
  (GET "/" [] hello)
  (GET "/start" [] auth-start)
  (GET "/callback" [] auth-callback)
  (route/not-found "Error, page not found!"))

(defn -main
  "This is our main entry point"
  [& args]
  (let [port (Integer/parseInt (or (:PORT (get-env)) "3000"))]
    ; Run the server with Ring.defaults middleware
    (server/run-server (wrap-defaults #'app-routes site-defaults) {:port port})
    ; Run the server without ring defaults
    ;(server/run-server #'app-routes {:port port})
    (println (str "Running webserver at http://127.0.0.1:" port "/"))))

