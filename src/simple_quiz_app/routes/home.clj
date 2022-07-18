(ns simple-quiz-app.routes.home
  (:require [hiccup.form :as hf]
            [compojure.core :refer :all]
            [ring.util.response :as resp]
            [simple-quiz-app.views.layout :as layout]
            [simple-quiz-app.routes.problem :refer :all]))

(defn home []
  (layout/common
   [:p "Hello World"]
   (hf/form-to
    [:post "/"]
    (-> (hf/submit-button "Mulai!!!")
        (assoc-in [1 :name] "mulai")))))

(defn quiz-page []
  (layout/common
   (hf/form-to
    [:post "/quiz"]
    (for [tipe ["math" "english" "verbal"]]
      (-> (hf/submit-button tipe)
          (assoc-in [1 :name] "tipe-soal"))))))

(defroutes home-routes
  (GET "/" [] (home))
  (POST "/" [mulai]
    (if (= mulai "Mulai!!!")
      (resp/redirect "/quiz")))
  
  (GET "/quiz" [] (quiz-page))
  (POST "/quiz" [tipe-soal]
    (case tipe-soal
      "math"    (resp/redirect "/quiz/math")
      "verbal"  (resp/redirect "/quiz/verbal")
      "english" (resp/redirect "/quiz/english")
      )))