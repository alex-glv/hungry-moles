(ns hungry-moles.protos)

(defprotocol IManageEntities
  (add-entities [this entities])
  (get-entities [this]))

(extend-type js/Phaser.World
  IManageEntities
  (add-entities [this entities]
    (doseq [entity entities]
      (let [game (.-game this)
            sprite (.sprite (.-add game) (:x entity) (:y entity) (:key entity))]
        (.image (.-load game) (:key entity) (:asset entity))
        (if (:body entity)
          (case (:body entity)
            :arcade (set! (.-physicsBodyType sprite) (.-ARCADE js/Phaser.Physics))
            :else nil))
        (.add this sprite))))
  (get-entities [this]
    (seq (.-children this))))
