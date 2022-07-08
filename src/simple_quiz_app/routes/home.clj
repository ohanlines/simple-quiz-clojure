(ns simple-quiz-app.routes.home
  (:require [hiccup.form :as hf]
            [compojure.core :refer :all]
            [ring.util.response :as resp]
            [simple-quiz-app.views.layout :as layout]))


(def soal-item ((read-string (slurp "src/simple_quiz_app/models/sample.edn")) 0))

(def atom-answers (atom []))

(defn client-answer [answer]
  (let [answers    (do (swap! atom-answers conj answer)
                       @atom-answers)
        n-answer   (count answers)]
    n-answer))

(defn pembahasan-page []
  (layout/common
   [:h "halo pembahasan"]))

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
  (POST "/" [answer]
    (if (>= (client-answer answer) 2)
      (do (reset! atom-answers [])
          (resp/redirect "/pembahasan-page"))
      (resp/redirect "/")))
  
  (GET "/pembahasan-page" [] (pembahasan-page)))
