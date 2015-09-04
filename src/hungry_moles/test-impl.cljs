(ns hungry-moles.test-impl
  (:require-macros [hungry-moles.core :refer (defentity* defgroup* )])
  (:require [hungry-moles.core])
  (:use [hungry-moles.core :only (defscreen PhysicalBody get-body storage play-animation update! update-entity *uuid-fn* IEntity preload create load-resource create-entity)]
        [hungry-moles.helpers :only (grid)]))

(defonce game (atom))
(defonce ship-mutable (atom))
(declare update-screen)

(defn create-entities []
  (defentity* ship
    {:key "ship"
     :asset {:src "assets/img/player.png"}
     :x 400
     :y 400
     :visible true
     })

  (defgroup* invaders 40
    {:key "invaders"
     :asset {:src "assets/img/invader32x32x4.png"
             :type :spritesheet
             :x 32 :y 32 :frames nil}
     :animations [{:fly {:frames [0 1 2 3]
                         :loop true
                         :fps 20}}]
     :visible true
     }))

;; (defgroup* bullets)


(defn get-screen []  
  (defscreen
    :size [800 600] 
    :title "invaders"
    :start-system :arcade
    :entities [ship invaders]
    :states {:Play {:preload (fn [game] (.log js/console "Preload. Entities in registry: " (count storage) ))
                    :create (fn [game parent]
                              ;; (play-animation (get-body storage invaders) "fly")
                              (reset! ship-mutable ship)
                              (dorun
                               (map (fn [e c]
                                      (let [[x y] c]
                                        (update! (get-body storage e) (assoc invaders :x x :y y))
                                        (play-animation (get-body storage e) "fly")))
                                    (:children invaders) (grid 20 2 40 40 40))))
                    :update (fn [game parent]
                              (let [new (update-screen @ship-mutable)]
                                (swap! ship-mutable assoc :x (:x new) :y (:y new))
                                (update-entity parent new)))
                    :render (fn [game parent] nil)}}))

(defn update-screen [e]
  (let [x (:x e)
        y (:y e)
        ue (-> e
               (assoc :x (+ 200 (* 100 (+ 1 (.sin js/Math
                                                 (/ (.now js/Date) 1000)))))))]
    
    ue))

(defn start-game []
  (create-entities)
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


