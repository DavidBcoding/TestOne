(ns sample1.core
  (:require
    [reagent.core :as r]
    [reagent.dom :as rdom]
    [goog.events :as events]
    [goog.history.EventType :as HistoryEventType]
    [markdown.core :refer [md->html]]
    [sample1.ajax :as ajax]
    [ajax.core :refer [GET POST]]
    [reitit.core :as reitit]
    [clojure.string :as string])
  (:import goog.History))

(defonce session (r/atom {:page :home}))

(defn nav-link [uri title page]
  [:a.navbar-item
   {:href   uri
    :class (when (= page (:page @session)) "is-active")}
   title])

(defn navbar [] 
  (r/with-let [expanded? (r/atom false)]
    [:nav.navbar.is-info>div.container
     [:div.navbar-brand
      [:a.navbar-item {:href "/" :style {:font-weight :bold}} "Welcome to David's Sample 1"]
      [:span.navbar-burger.burger
       {:data-target :nav-menu
        :on-click #(swap! expanded? not)
        :class (when @expanded? :is-active)}
       [:span][:span][:span]]]
     [:div#nav-menu.navbar-menu
      {:class (when @expanded? :is-active)}
      [:div.navbar-start
       [nav-link "#/" "Home" :home]
       [nav-link "#/about" "About" :about]
       [nav-link "#/make" "Make Entries" :make]]]] )) ;; Monday morning add
(defn make-page []
  [:div.content.box
   [:p "This page will allow you to go against the Magic page, and gather your entries"

   ]])
(defn about-page []
  [:section.section>div.container>div.content
   [:div.content.box
      [:p "Chris your equation is 1+ 1, and your answer is 2"
      [:p "Austin your equation is 1+ 2, and your answer is 3"
      [:p "Neel your equation is 1+ 3, and your answer is 4"
      [:p "Emil your equation is 1+ 4, and your answer is 5"
      [:p "Derek your equation is 1+ 5, and your answer is 6"
      [:p "Daniel your equation is 1+ 6, and your answer is 7"
      [:p "Amparo your equation is 1+ 7, and your answer is 8"
      [:p "Mike your equation is 1+ 8, and your answer is 9"
      [:p "Robert your equation is 1+ 9, and your answer is 10"
      [:p "Jeff your equation is 1+ 10, and your answer is 11"
   [:div.content.box
   [:img {:src "/img/warning_clojure.png"}]] ]]]]]]]]]]]])

(defn home-page []
 (def str1 "Hello")
 (def age 40)

 [:div.content.box
    [:p
    [:p str1 + ", I hope everyone is having a great Monday let's get busy"
    [:p "^^I'm really proud of that last line, because it's been a while, and I concacted that (Hello) off the top of my head.
    it's cool if you are not impressed #MommaIMadeIt"
    [:p "Below are a few examples of addition hard coded."
    [:p
    [:p:math (+ 50 50)
    [:p
    [:p:math (+ 100 100)]
    [:p
    [:p:math (+ 100 200)
    [:p
    [:p:math (+ 100 300)
    [:p
    [:p:math (+ 400 400)
    [:p "[:p:math (+ 400 400) - is that last line that got me to hard code an equation,
     but the razzle dazzle happens in the next comment box."

 [:p:div1.content.box
   [:p "Now that you know a little about me let's get some of your info:"
   [:p "Select an option below to have your math equations either read from your mind
        or make your own entries."
   [:p [:a.button.is-primary
          [nav-link "#/about" "Mind Reader"]
   [:p [:a.button.is-primary
          [nav-link "#/make" "Make Entries"]

 ]]]]]]]]]]]]]]]]]]]]]])

(def pages
  {:home #'home-page
   :about #'about-page
   :new #'make-page})

(defn page []
  [(pages (:page @session))])

;; -------------------------
;; Routes

(def router
  (reitit/router
    [["/" :home]
     ["/about" :about]
     ["/make" :new]]))

(defn match-route [uri]
  (->> (or (not-empty (string/replace uri #"^.*#" "")) "/")
       (reitit/match-by-path router)
       :data
       :name))
;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      HistoryEventType/NAVIGATE
      (fn [^js/Event.token event]
        (swap! session assoc :page (match-route (.-token event)))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn fetch-docs! []
  (GET "/docs" {:handler #(swap! session assoc :docs %)}))

(defn ^:dev/after-load mount-components []
  (rdom/render [#'navbar] (.getElementById js/document "navbar"))
  (rdom/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (ajax/load-interceptors!)
  (fetch-docs!)
  (hook-browser-navigation!)
  (mount-components))
