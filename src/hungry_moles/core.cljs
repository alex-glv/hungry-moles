(ns ^:figwheel-always hungry-moles.core
    (:require [hungry-moles.protos :as p])
    (:require-macros [hungry-moles.macros :as m]))

(def entities (atom
               [;; {:key "bullet"
                ;;  :asset {:src "assets/img/bullet.png"}
                ;;  :body :arcade
                ;;  :group 30}
                ;; {:key "invader"
                ;;  :asset {:src "assets/img/invader32x32x4.png"
                ;;          :type :spritesheet
                ;;          :x 32 :y 32}
                ;;  :body :arcade
                ;;  :group 30
                ;;  }
                ;; {:key "enemyBullet"
                ;;  :asset {:src "assets/img/enemy-bullet.png"}
                ;;  :group 30}
                {:key "ship"
                 :asset {:src "assets/img/player.png"}
                 :x 300 :y 300
                 }
                ;; {:key "explode"
                ;;  :asset {:src "assets/img/explode.png"}
                ;;  };
                
                ;; {:key "background"
                ;;  :asset {:src "assets/img/background.png"}
                ;;  :x 200 :y 500}
                ]))

;; (def non-entities
;;   [{:key "starfield"
;;     :asset {:src "assets/img/starfield.png"}
;;     :x 0 :y 0 :x2 800 :y2 600}])

(defonce phaser-game (atom))

(defn get-screen []
  (p/defscreen
    :size [800 600] 
    :title "invaders"
    :start-system :arcade
    :states {:Play {:entities @entities
                    :preload (fn [game] nil)
                    :create (fn [game]
                              ;; (let [ship (m/call-in* @phaser-game [-world getChildAt] 0)]
                              ;;   ;; (set! (m/call-in* ship [-x]) 300)
                              ;;   ;; (set! (m/call-in* ship [-y]) 300)
                              ;;   )
                              )
                    
                    :update (fn [game]

                              (let [upd-ents (map #(assoc %
                                                          :x (+ 1 (:x %))
                                                          :y (+ 1 (:y %))))]
                                (update-entities (.-world game) ent))

                              )
                    
                    :render (fn [game] nil)}}))




(defn start-game []
  (let [game (get-screen)]
    (reset! phaser-game game)
    (.start (.-state game) "Play")))

(defn destroy-world []
  (.destroy @phaser-game))

(defn restart-game []
  (destroy-world)
  (start-game))
