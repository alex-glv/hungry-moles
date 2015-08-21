(ns ^:figwheel-always hungry-moles.core
    (:require [hungry-moles.protos :as p])
    (:require-macros [hungry-moles.macros :as m]))

(defonce game (atom))
(defonce entities (atom))
(def entities-map [ {:key "ship"
                     :asset {:src "assets/img/player.png"}
                     :x 200
                     :y 200} ])

(declare update-entity)

(defn get-screen []
  (p/defscreen
    :size [800 600] 
    :title "invaders"
    :start-system :arcade
    :states {:Play {:preload (fn [game]
                               (doseq [e entities-map]
                                 (m/call-in* game [-load image] (:key e) (:src (:asset e)))))
                    
                    :create (fn [game]
                              (let [e (map #(p/defentity game % #'p/physical) entities-map)
                                    w (m/call-in* game [-world])]
                                (reset! entities e)
                                (p/add-entities w e)))
                    :update (fn [game]
                              (let [w (m/call-in* game [-world])
                                    new (map update-entity @entities)]
                                (reset! entities new)
                                (p/update-entities w new)))
                    :render (fn [game] nil)}}))

(defn update-entity [e]
  (let [x (:x e)
        y (:y e)
        ue (-> e
               (assoc :x (rand-int 600))
               (assoc :y (rand-int 800)))]
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
;;                 ;; {:key "invader"
;;                 ;;  :asset {:src "assets/img/invader32x32x4.png"
;;                 ;;          :type :spritesheet
;;                 ;;          :x 32 :y 32}
;;                 ;;  :body :arcade
;;                 ;;  :group 30
;;                 ;;  }
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

