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
;;Adding button function and placement
;; Initial of app-data for 0 as place holder
(def app-data (r/atom {:x 0 :y 0 :total 0}))

;; Updates the value of total in app-data
(defn swap [val]
      (swap! app-data assoc
             :total val)
      (js/console.log "The value from plus API is" (str (:total @app-data)))); Value comes out in console

;; Calls the math API for a specific operation and x and y values
(defn math [params operation]
      (POST (str "/api/math/" operation)
            {:headers {"accept" "application/transit-json"}
             :params  @params
             :handler #(swap (:total %))}))

;; Function for hard coded math API for Year + 1
(defn getAdd []
      (GET "/api/math/plus?x=1&y=2022"
           {:headers {"accept" "application/json"}
            :handler #(swap (:total %))}))

;; Update to clojure parseInt
(defn int-value [v]
      (-> v .-target .-value int))

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
       [nav-link "#/make" "Make Entries" :make]
       [nav-link "#/about" "About" :about]]]]))
(defn home-page []
    (def str1 "Hello")
    [:div.content.box
        [:p
        [:p str1 + ", I hope everyone is having a great day let's get busy"
        [:p
        [:p "Click the test button to add a year to 2022."]
            [:button.button.is-primary {:on-click #(getAdd)} "2022 + 1"]
            [:p "That answer is: "
               [:span  (:total @app-data)]]
    [:div1.content.box
        [:p "Select an option below to have your math equation either read from your mind
            or make your own entries."
        [:p [:a.button.is-primary
          [nav-link "#/about" "Mind Reader"]]
          [:p ]
        [:p [:a.button.is-primary
          [nav-link "#/make" "Make Entries"]]]]]]]]]])
(defn make-page []
      (let [params (r/atom {})]
   [:section.section>div.container>div.content
       [:p "Enter numbers in the text boxes below for your own equation then click the button for your answer."]
        [:form
         [:div.form-group
          [:label "1st number: "]
          [:input {:type :text :placeholder "First number here" :on-change #(swap! params assoc :x (int-value %))}]]
               [:button.button.is-primary {:on-click #(math params "plus")} "+"]
         [:div.form-group
          [:label "2nd number: "]
          [:input {:type :text :placeholder "Second number here" :on-change #(swap! params assoc :y (int-value %))}]]]
        [:p "Your answer is: "
         [:span  (:total @app-data)]]
       ]))
(defn about-page []
  [:section.section>div.container>div.content
   [:div.content.box
      [:p "Mind reader suggests your equation is 1+ 1, and your answer is 2"
      [:p
      [:img {:src "/img/warning_clojure.png"}]]]]])
(def pages
  {:home #'home-page
   :make #'make-page
   :about #'about-page})

(defn page []
  [(pages (:page @session))])

;; -------------------------
;; Routes

(def router
  (reitit/router
    [["/" :home]
     ["/about" :about]
     ["/make" :make]]))

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
