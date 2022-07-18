(ns simple-quiz-app.views.layout
  (:require [hiccup.page :refer [html5 include-css]]))

(defn common [& body]
  (html5
    [:head
     [:title "Welcome to simple-quiz-app"]
     (include-css "/css/screen.css")]
    [:body body]))