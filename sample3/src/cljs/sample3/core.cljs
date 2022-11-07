(ns sample3.core
  (:require
    [day8.re-frame.http-fx]
    [reagent.dom :as rdom]
    [reagent.core :as r]
    [re-frame.core :as rf]
    [goog.events :as events]
    [goog.history.EventType :as HistoryEventType]
    [markdown.core :refer [md->html]]
    [sample3.ajax :as ajax]
    [sample3.events]
    [reitit.core :as reitit]
    [reitit.frontend.easy :as rfe]
    [clojure.string :as string])
  (:import goog.History))
(defn nav-link [uri title page]
      [:a.navbar-item
       {:href   uri
        :class (when (= page @(rf/subscribe [:common/page-id])) :is-active)}
       title])

;######Changes by David
;;Adding button function and placement
;; Initial of app-data for 0 as place holder
(def app-data (r/atom {:x 0 :y 0 :total 0}))

;; Updates the value of total in app-data
(defn swap [val]
      (swap! app-data assoc
             :total val))


;; Calls the math API for a specific operation and x and y values
(defn math [operation]
      (POST (str "/api/math/" operation)
            {:headers {"accept" "application/transit-json"}
             :params  @app-data
             :handler #(swap (:total %))}))

;; Function for hard coded math API for Year + 1
(defn getAdd []
      (GET "/api/math/plus?x=1&y=2022"
           {:headers {"accept" "application/json"}
            :handler #(swap (:total %))}))

;; User entries
(defn int-value [v]
      (-> v .-target .-value int))

;###########
(defn change-color []
      (cond
        (<= 0 (:total @app-data) 19) {:style {:color "lightgreen" :font-weight :bold}}
        (<= 20 (:total @app-data) 49) {:style {:color "lightblue" :font-weight :bold}}
        :default {:style {:color "lightsalmon" :font-weight :bold}}))
;###########

(defn navbar [] 
  (r/with-let [expanded? (r/atom false)]
              [:nav.navbar.is-info>div.container
               [:div.navbar-brand
                [:a.navbar-item {:href "/" :style {:font-weight :bold}} "sample3"]
                [:span.navbar-burger.burger
                 {:data-target :nav-menu
                  :on-click #(swap! expanded? not)
                  :class (when @expanded? :is-active)}
                 [:span][:span][:span]]]
               [:div#nav-menu.navbar-menu
                {:class (when @expanded? :is-active)}
                [:div.navbar-start
                 [nav-link "#/" "Home" :home]
                 [nav-link "#/about" "About" :about]]]]))

(defn about-page []
  [:section.section>div.container>div.content
   [:img {:src "/img/warning_clojure.png"}]])

(defn home-page []
      (def str1 "Hello")
      (let [params (r/atom {})]
      [:div.content.box
        [:p str1 + ", I hope everyone is having a great day!"]
          [:p "Click the test button to add a year to 2022."]
          [:button.button.is-primary {:on-click #(getAdd)} "2022 + 1"]
          [:p "That answer is: "
           [:span  (:total @app-data)]]
                   [:section.section>div.container>div.content
                    [:p "Enter numbers in the text boxes below for your own equation then click an operation for your answer."]
                      [:form
                        [:div.form-group
                          [:input {:type :text :placeholder "First number here" :on-change #(swap! app-data assoc :x (int-value %))}]]
                    [:p]
                     [:div.form-group
                      [:input {:type :text :placeholder "Second number here" :on-change #(swap! app-data assoc :y (int-value %))}]]]
                        [:p]
                          [:button.button.is-primary {:on-click #(math "plus")} "+"]
                          [:button.button.is-black {:on-click #(math "minus")} "-"]
                          [:button.button.is-primary {:on-click #(math "multiply")} "x"]
                          [:button.button.is-black {:on-click #(math "divide")} "/"]
                        [:p]
                          [:div.form-group
                           [:label "Your answer is: " + [:span (change-color) (:total @app-data)]]]]]))
(defn page []
  (if-let [page @(rf/subscribe [:common/page])]
    [:div
     [navbar]
     [page]]))

(defn navigate! [match _]
  (rf/dispatch [:common/navigate match]))

(def router
  (reitit/router
    [["/" {:name        :home
           :view        #'home-page
           :controllers [{:start (fn [_] (rf/dispatch [:page/init-home]))}]}]
     ["/about" {:name :about
                :view #'about-page}]]))

(defn start-router! []
  (rfe/start!
    router
    navigate!
    {}))

;; -------------------------
;; Initialize app
(defn ^:dev/after-load mount-components []
  (rf/clear-subscription-cache!)
  (rdom/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (start-router!)
  (ajax/load-interceptors!)
  (mount-components))
