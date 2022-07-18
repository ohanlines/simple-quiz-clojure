(ns simple-quiz-app.routes.problem
  (:require [hiccup.form :as hf]
            [compojure.core :refer :all]
            [ring.util.response :as resp]
            [simple-quiz-app.views.layout :as layout]))

;; === slurp sample edn file ==============
(def tipe-soal #{"math" "english" "verbal"})

(defn soal-item [tipe]
  (let [soal-data  (-> (str "resources/public/edn/" (tipe-soal tipe) ".edn")
                       slurp
                       read-string)
        all-data   (shuffle soal-data)
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
(def soal-item-atom (atom []))

;; === atom for indexing problems =========
(def index (atom 0))

;; === to store client answer =============
(def atom-answers (atom []))

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

(defn pembahasans [i soal]
  (get-in soal [:pembahasan i]))

(defn key-answer [i soal]
  (get-in soal [:jawaban i]))

;; === processing client answer ===========
(defn client-answer [i _]
  [:p (str "jawaban lo: " (get @atom-answers i))])

(defn check-client-answer [answer]
  (let [answers (do (swap! atom-answers conj answer)
                    @atom-answers)]
    {:n     (count answers)
     :score (->> (for [i (range (inc @index))] (key-answer i @soal-item-atom))
                 (map = @atom-answers)
                 (filter true?)
                 (count))}))

;; === pages ==============================
(defn problem-page [tipe]
  (layout/common
   (hf/form-to
    [:post (str "/quiz/" tipe)]
    (map #(eval (list % @index @soal-item-atom)) [problems options-for-problem])
    (hf/submit-button "submit"))))

(defn pembahasan-page [answer]
  (layout/common
   [:h1 (str "total benar = " (:score (check-client-answer answer)) "/8")]
   [:hr]
   (for [i (range (inc @index))]
     (map #(eval (list % i @soal-item-atom))
          [client-answer
           problems
           options-for-pembahasan
           pembahasans
           (fn [_ _] [:hr])]))
   (hf/form-to
    [:post "/pembahasan-page"]
    (-> (hf/submit-button "home")
        (assoc-in [1 :name] "to-home")))))

;; === routes =============================
(defroutes problem-routes
  (GET "/quiz/:tipe" [tipe]
    (do (reset! soal-item-atom (soal-item tipe))
        (problem-page tipe)))
  (POST "/quiz/:tipe" [answer tipe]
    (if (>= (:n (check-client-answer answer)) 8)
      (resp/redirect "/pembahasan-page")
      (do (swap! index inc)
          (case tipe 
            "verbal"  (resp/redirect "/quiz/verbal")
            "math"    (resp/redirect "/quiz/math")
            "english" (resp/redirect "/quiz/english")))))

  (GET "/pembahasan-page" [answer] (pembahasan-page answer))
  (POST "/pembahasan-page" [to-home]
    (if (= to-home "home")
      (do (reset! index 0)
          (reset! atom-answers [])
          (resp/redirect "/")))))