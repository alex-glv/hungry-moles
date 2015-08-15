(ns hungry-moles.protos)


;; PROTOS

(defprotocol IManageEntities
  (add-entities [this entities])
  (get-entities [this]))

(defprotocol IVisibleEntity
  (add! [_ game])
  (preload! [_ game]))



;; TYPES

(deftype StaticEntity [entity]
  IVisibleEntity
  (add! [_ game]
    (let [sprite (.sprite (.-add game) (:x entity) (:y entity) (:key entity))]
      sprite)
    )
  (preload! [_ game]
    (.image (.-load game) (:key entity) (:src (:asset entity)))))

(deftype SpriteEntity [entity]
  IVisibleEntity
  (add! [_ game]
    (let [sprite (.tileSprite (.-add game) (:x entity) (:y entity) (:x2 entity) (:y2 entity) (:key entity))]
      sprite))
  (preload! [_ game]
    (.spritesheet (.-load game) (:key entity) (get-in entity :asset :src) (get-in entity :asset :x) (get-in entity :asset :y))))

(extend-type js/Phaser.World
  IManageEntities
  (add-entities [this entities]
    (doseq [entity entities]
      (let [game (.-game this)
            ;; sprite-entity (case (or :image (:type (:asset entity)))
            ;;                 :spritesheet (SpriteEntity. entity)
            ;;                 :image (StaticEntity. entity)
            ;;          :else (throw (js/Error "Unknown entity type passed")))
            ;; sprite (add! sprite-entity game)
            ]
        
        ;; (if (:body entity)
        ;;   (case (:body entity)
        ;;     :arcade (set! (.-physicsBodyType sprite) (.-ARCADE js/Phaser.Physics))
        ;;     :else nil))
        ;; (.add this sprite)
        )))
  (get-entities [this]
    (seq (.-children this))))


;; (extend-type js/Phaser.DisplayObject
;;   IAssociative
;;   (-assoc [])
;;   )

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
                    (preload [_] (
                                  ;; ADD ENTITIES PRELOAD
                                  (:preload state-obj) game))
                    ;; create
                    (create [_]
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
