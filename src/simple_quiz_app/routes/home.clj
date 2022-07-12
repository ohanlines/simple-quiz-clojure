(ns simple-quiz-app.routes.home
  (:require [hiccup.form :as hf]
            [hiccup.element :as he]
            [compojure.core :refer :all]
            [ring.util.response :as resp]
            [simple-quiz-app.views.layout :as layout]))

;; === slurp sample edn file ==============
(def soal-data ((comp read-string slurp) "resources/public/edn/sample.edn"))

(defn soal-item []
  (let [all-data   (shuffle soal-data)
        get-data   (fn [key-xs] (->> all-data
                                     (map #(get-in % key-xs))
                                     (concat)
                                     (vec)))
        problem    (get-data [:soal :soal-text])
        option     (get-data [:soal :options])
        jawaban    (get-data [:soal :jawaban])
        pembahasan (get-data [:bahas])
        problem-id (get-data [:problem-id])]
    {:problem    problem
     :option     option
     :jawaban    jawaban
     :pembahasan pembahasan
     :problem-id problem-id}))

;; === to store soal item =================
(def soal-item-atom (atom (soal-item)))

;; === atom for indexing problems =========
(def index (atom 0))

;; === to store client answer =============
(def atom-answers (atom []))

(defn client-answer [answer]
  (let [answers (do (swap! atom-answers conj answer)
                    @atom-answers)
        n       (count answers)]
    n))

;; === soal content =======================
(defn problems [i soal]
  (get-in soal [:problem i]))

(defn options-for-problem [i soal]
  (let [alphabet    (mapv str "ABCDEFGHIJKLMNOPQRSTUVWXYZ")
        answer-text (->> [:option i]
                         (get-in soal)
                         (mapv #(% 1)))
        opt-paired  (->> (interleave alphabet answer-text)
                         (partition 2)
                         (mapv vec))]
    (for [[alp txt] opt-paired]
      (-> "answer"
          (hf/radio-button nil alp)
          (conj txt [:br])))))

(defn options-for-pembahasan [i soal]
  (let [bold-the-true (fn [xs]
                        (if (true? (xs 0))
                          (update xs 1 #(str "<b>" % "</b>"))
                          xs))
        answer-text   (->> [:option i]
                           (get-in soal)
                           (map bold-the-true)
                           (mapv #(% 1)))]
    [:ol {:type "A"}
     (for [i answer-text]
       [:li i])]))

;; (defn pembahasans [i soal]
;;   (get-in soal [i :bahas]))

;; (defn problem-ids [i soal]
;;   (get-in soal [i :problem-id]))

;; (for [[alp txt] opt-paired]
;;   (-> "answer"
;;       (hf/radio-button nil alp)
;;       (conj txt [:br])))

;; (mapv #(eval (list % @index soal-data-shuffle))
;;       [problems options pembahasans problem-ids])



;; === pages ==============================
(defn home []
  (layout/common
   (hf/form-to
    [:post "/"]
    (map #(eval (list % @index @soal-item-atom)) [problems options-for-problem])
    (hf/submit-button "submit"))))

(defn pembahasan-page []
  (layout/common
   [:h (str "total benar = "  "/2")]
   (for [i (range (inc @index))]
     (options-for-pembahasan i @soal-item-atom))
   (hf/form-to
    [:post "/pembahasan-page"]
    (-> (hf/submit-button "home")
        (assoc-in [1 :name] "to-home")))))

;; === routes =============================
(defroutes home-routes
  (GET "/" [] (home))
  (POST "/" [answer]
    (if (>= (client-answer answer) 8)
      (resp/redirect "/pembahasan-page")
      (do (swap! index inc)
          (resp/redirect "/"))))

  (GET "/pembahasan-page" [] (pembahasan-page))
  (POST "/pembahasan-page" [to-home]
    (if (= to-home "home")
      (do (reset! index 0)
          (reset! atom-answers [])
          (reset! soal-item-atom (soal-item))
          (resp/redirect "/"))))
  )
