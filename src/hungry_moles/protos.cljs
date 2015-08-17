(ns hungry-moles.protos
  (:require-macros [hungry-moles.macros :as m]))


;; PROTOS

(defprotocol IManageEntities
  (add-entities [this entities])
  (get-entities [this])
  (update-entities [this entities]))

(defprotocol IVisibleEntity
  (add! [_ game])
  (preload! [_ game]))



;; TYPES

(deftype StaticEntity [entity]
  IVisibleEntity
  (add! [_ game]
    (let [sprite (.sprite (.-add game) (:x entity) (:y entity) (:key entity))]
      (.log js/console sprite)
      sprite))
  (preload! [_ game]
    (.image (.-load game) (:key entity) (:src (:asset entity)))))


(extend-type js/Phaser.World
  IManageEntities
  (add-entities [this entities]
    (let [game (.-game this)
          mapped-entities (map #(assoc % :sprite (add! (StaticEntity. %) game)) entities)]
      (doseq [entity mapped-entities]
        (if (:body entity)
          (case (:body entity)
            :arcade (set! (.-physicsBodyType (:sprite entity)) (.-ARCADE js/Phaser.Physics))
            :else nil))
        (.add this (:sprite entity)))
      (set! (m/call-in* this [-entities] ) mapped-entities)))
  
  (get-entities [this]
    (m/call-in* this [-entities] ))
  
  (update-entities [this entities]
    (let [stored-entities (get-entities this)]
      (.log js/console stored-entities)
      (doseq [ent entities]
        (let [phy-ent (get stored-entities (:key ent))]
          (set! (.-x phy-ent) (:x ent))
          (set! (.-y phy-ent) (:y ent))))
      ))
  )
  

(defn defscreen [& params]
  (let [params (apply hash-map params)
        [x y] (:size params)]
    (when (:states params)
      (let [game (js/Phaser.Game. x y (.-auto js/Phaser)  (:title params))]
        (doseq [[state-name state-obj] (:states params)]
          
          (.add (.-state game) (str (name state-name))
                (fn [game]
                  (reify Object
                    ;; preload
                    (preload [_]
                      (doseq [ent (or (:entities state-obj) {})]
                        
                        (m/call-in* game [-load image] (:key ent) (:src (:asset ent))))
                      
                      (:preload state-obj) game)
                    ;; create
                    (create [_]
                      (let [world (.-world game)]
                        (set! (.-enableBody world) true)
                        
                        (add-entities world (:entities state-obj)))
                      
                      (if-let [sys (:start-system params)]
                        (cond
                          (= :arcade sys) (.startSystem (.-physics game) (.-ARCADE js/Phaser.Physics))
                          :else (throw (js/Error. "System is not supported!"))))
                      ((:create state-obj) game))
                    ;; update
                    (update [_] ((:update state-obj) game))
                    ;; render
                    (render [_] ((:render state-obj) game))))))
        game)))
  )
