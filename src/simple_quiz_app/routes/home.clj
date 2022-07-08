(ns simple-quiz-app.routes.home
  (:require [hiccup.form :as hf]
            [compojure.core :refer :all]
            [noir.response :refer [redirect]]
            [simple-quiz-app.views.layout :as layout]))


(def soal-item ((read-string (slurp "src/simple_quiz_app/models/sample.edn")) 0))

(defn pembahasan []
  )

(defn home []
  (layout/common
   [:p "ini soal"]
   (hf/form-to
    [:post "/"]
    (let [alphabet    (mapv str "ABCDEFGHIJKLMNOPQRSTUVWXYZ")
          answer-text (->> [:soal :options]
                           (get-in soal-item)
                           (mapv #(% 1)))
          opt-paired  (mapv vec (partition 2 (interleave alphabet answer-text)))]
      (for [[alp txt] opt-paired]
       (-> "answer"
           (hf/radio-button nil alp)
           (conj txt [:br]))))
    (hf/submit-button "submit"))))

(defroutes home-routes
  (GET "/" [] (home))
  )
