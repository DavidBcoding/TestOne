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
       [nav-link "#/home" "Home" :home]
       [nav-link "#/about" "About" :about]
       [nav-link "#/youtube" "Double Fidget Spinner" :youtube]]]]))

(defn about-page []
  [:section.section>div.container>div.content
   [:img {:src "/img/warning_clojure.png"}]])

(defn home-page []
 (def str1 "Hello")
 (def age 40)

 [:div.content.box
    [:p
    [:p str1 + ", I hope everyone is having a great Monday let's get busy"
    [:p "^^I'm really proud of that last line, because it's been a while, and I concacted that (Hello) off the top of my head
    it's cool if you are not impressed!"
    [:p
    [:p "Wow I tripped over syntax a lot here, and then poof you rolled back way too far without a save. "
    [:p
    [:p:math (+ 400 400) ;;allows line breaks with the equation
    [:p "  [:p:math (+ 400 400) - is that last line that got me to hard code an equation,
     but the razzle dazzle happens in the next comment box."
    [:p "I'm guessing nobody wants to see my code in browser, but if you knew my struggles you'd appreciate the code in the webpage lol."
    [:p "Yes, I just typed lol at my own joke"
 [:div1.content.box
     [:p "Now that you know a little about me let's get some of your info"
     [:p "Nice to meet you I'm " + age " by the way, in case you get shy when it's your turn to be forth coming."
     [:p "Round of applause for me I declared age, and concacted that last line too."
 [:div2.content.box
  [:p
  [:p "Hello written out"
  [:p "will this break line"
  [:p str1
  [:p "Wow I tripped over syntax a lot here, and then poof you rolled back way too far without a save.
  Hopefully I completed the assignment fingers crossed extra credit time"
  [:p "-"
  [:p:math (+ 400 400) ;;allows comments
  ]]]]]]]]]]]]]]]]]]]]]]])

(def pages
  {:home #'home-page
   :about #'about-page})

(defn page []
  [(pages (:page @session))])

;; -------------------------
;; Routes

(def router
  (reitit/router
    [["/" :home]
     ["/about" :about]]))

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
