(ns ^:figwheel-always hungry-moles.core
    (:require [hungry-moles.protos :as p])
    (:require-macros [hungry-moles.macros :as m]))

(def entities
  [{:key "ship"
    :asset "assets/ship.png"
    :body :arcade
    :x 200
    :y 200
    }
   {:key "ship"
    :asset "assets/ship.png"
    :body :arcade
    :x 400
    :y 400
    }
   {:key "ship"
    :asset "assets/ship.png"
    :body :arcade
    :x 600
    :y 620
    }])

(defonce phaser-game (atom))

(defn get-screen []
  (m/defscreen*
    :size [800 600] 
    :title "my-demo"
    :start-system :arcade
    :states {:Play {:preload (fn [game]
                               (doseq [ent entities]
                                 (.image (.-load game) (:key ent) (:asset ent))))
                    
                    :create (fn [game]
                              (let [world (.-world game)]
                                (set! (.-enableBody world) true)
                                (p/add-entities world entities)))
                    
                    :update (fn [game]
                              (doseq [ch (p/get-entities (.-world game))]
                                (.moveToPointer (.-arcade (.-physics game)) ch)))
                    
                    :render (fn [game] nil)}
             
             }))

(defn start-game []
  (let [game (get-screen)]
    (reset! phaser-game game)
    (.start (.-state game) "Play")))

(defn destroy-world []
  (.destroy @phaser-game))

(defn restart-game []
  (destroy-world)
  (start-game))
