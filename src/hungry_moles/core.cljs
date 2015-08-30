(ns ^:figwheel-always hungry-moles.core
    (:require [hungry-moles.protos :as p]
              [hungry-moles.helpers :as h]
              [cljs-uuid-utils.core :as uuid])
    (:use (cljs-uuid-utils.core :only (make-random-uuid)))
    (:require-macros [hungry-moles.core :as m]))

(defonce game (atom))
(defonce ship-mutable (atom))
(declare update-entity)
(def *uuid-fn* make-random-uuid)
(m/defentity* ship
  {:key "ship"
   :asset {:src "assets/img/player.png"}
   :x 400
   :y 400
})

(m/defentity* invaders
  {:key "invader"
   :asset {:src "assets/img/invader32x32x4.png"
           :type :spritesheet
           :x 32 :y 32 :frames nil}
   :x 100 :y 100
   :animations [{:fly {:frames [0 1 2 3]
                       :loop true
                       :fps 20}}]
})



(defn get-screen []
  (p/defscreen
    :size [800 600] 
    :title "invaders"
    :start-system :arcade
    :entities [ship invaders]
    :states {:Play {:preload (fn [game] (.log js/console "Preload. Entities in registry: " (count p/storage) ))
                    :create (fn [game parent]
                              (p/play-animation (p/get-body p/storage invaders) "fly")
                              (reset! ship-mutable ship))
                    :update (fn [game parent]
                              (let [new (update-entity @ship-mutable)]
                                (swap! ship-mutable assoc :x (:x new) :y (:y new))
                                (p/update-entity parent @ship-mutable))
                              )
                    :render (fn [game parent] nil)}}))

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

