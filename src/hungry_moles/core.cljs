(ns ^:figwheel-always hungry-moles.core
    (:require [hungry-moles.protos :as p]
              [hungry-moles.helpers :as h]
              [cljs-uuid-utils.core :as uuid])
    (:require-macros [hungry-moles.core :as m]))

(defonce game (atom))
(defonce entities (atom))
(declare update-entity)

(m/defentity* ship (uuid/make-random-uuid)
  {:key "ship"
   :asset {:src "assets/img/player.png"}
   :x 400
   :y 400
})

(m/defentity* invaders (uuid/make-random-uuid)
  {:key "invader"
   :asset {:src "assets/img/invader32x32x4.png"
           :type :spritesheet
           :x 32 :y 32 :frames nil}
   :animations [{:fly {:frames [0 1 2 3]
                       :loop true
                       :fps 20}}]
   })



(defn get-screen []
  (p/defscreen
    :size [800 600] 
    :title "invaders"
    :start-system :arcade
    :entities [#'ship #'invaders]
    :states {:Play {:preload (fn [game]
                               (p/preload (:entity (meta ship)) game)
                               (p/preload (:entity (meta invaders)) game))
                    
                    :create (fn [game]
                              (let [ w (m/call-in* game [-world])]
                                (p/add-entity w ship)
                                (p/add-entity w invaders)
                                (reset! entities ship)
                                (p/play-animation (p/get-body (:uuid (meta invaders))) "fly")))
                    :update (fn [game]
                              (let [w (m/call-in* game [-world])
                                    new (update-entity @entities)]
                                (reset! entities new)
                                (p/update-entity w new))
                              )
                    :render (fn [game] nil)}}))

(defn update-entity [e]
  (let [x (:x e)
        y (:y e)
        ue (-> e
               (assoc :x (+ 200 (* 100 (+ 1 (.sin js/Math
                                                 (/ (.now js/Date) 1000)))))))]
    
    ue))

(defn start-game []
  (let [new-game (get-screen)]
    (reset! game new-game)
    (.start (.-state new-game) "Play")))

(defn destroy-world []
  (.destroy @game))

(defn restart-game []
  (destroy-world)
  (start-game))


;; (def entities (atom
;;                [;; {:key "bullet"
;;                 ;;  :asset {:src "assets/img/bullet.png"}
;;                 ;;  :body :arcade
;;                 ;;  :group 30}
                
;;                 ;; {:key "enemyBullet"
;;                 ;;  :asset {:src "assets/img/enemy-bullet.png"}
;;                 ;;  :group 30}
;;                 {:key "ship"
;;                  :asset {:src "assets/img/player.png"}
;;                  :x 300 :y 300
;;                  }
;;                 ;; {:key "explode"
;;                 ;;  :asset {:src "assets/img/explode.png"}
;;                 ;;  };
                
;;                 ;; {:key "background"
;;                 ;;  :asset {:src "assets/img/background.png"}
;;                 ;;  :x 200 :y 500}
;;                 ]))


;; (def non-entities
;;   [{:key "starfield"
;;     :asset {:src "assets/img/starfield.png"}
;;     :x 0 :y 0 :x2 800 :y2 600}])

