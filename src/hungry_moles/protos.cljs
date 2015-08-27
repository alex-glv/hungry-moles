(ns hungry-moles.protos
  (:require-macros [hungry-moles.macros :as m]))


;; PROTOS

(defprotocol IManageEntities
  (add-entities [this entities])
  (get-entities [this])
  (update-entities [this entities]))

(defprotocol IVisibleEntity
  (pos-update! [this x y])
  (bring-up [this parent])
  (create-primitive [this])
  (preload [this]))

(defprotocol IBodyEntity
  (enable-body! [this]))

(defrecord PhysicalBody [game x y key]
  IVisibleEntity
  (pos-update! [this x y]
    (let [p (:primitive this)]
      (set! (.-x p) x)
      (set! (.-y p) y)))

  (bring-up [this parent]
    (m/call-in* parent [add] (:primitive this)))
  
  (create-primitive [this]
    (let [p (js/Phaser.Sprite. game (:x this) (:y this) (:key this))]
      p))
  
  IBodyEntity
  (enable-body! [this]
    (let [p (:primitive this)]
      (set! (.-physicsBodyType p) (.-ARCADE js/Phaser.Physics)))))


(defrecord PhysicalGroup [game parent key group]
  IVisibleEntity
  (pos-update! [this x y])
  
  (create-primitive [this]
    (let [p (js/Phaser.Group. game parent (:key this))]
      p))

  (bring-up [this parent]
    (doseq [row (:coords  group)]
      (doseq [[x y] row]
        (m/call-in* (:primitive this) [create] x y (:key this)))))
  
  IBodyEntity
  (enable-body! [this]
    (let [p (:primitive this)]
      (set! (.-physicsBodyType p) (.-ARCADE js/Phaser.Physics)))))

;; TYPES>>

(extend-type js/Phaser.World
  IManageEntities
  (add-entities [this entities]
    (doseq [e entities]
      (doseq [s (:systems e)]
        (bring-up s this))))
  
  (get-entities [this] )
  
  (update-entities [this entities]
    (doseq [e entities]
      (doseq [s (:systems e)]
        (pos-update! s (:x e) (:y e))))))
  

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
                      ((:preload state-obj) game))
                    ;; create
                    (create [_]
                      (let [world (.-world game)]
                        (set! (.-enableBody world) true))
                      (if-let [sys (:start-system params)]
                        (cond
                          (= :arcade sys) (.startSystem (.-physics game) (.-ARCADE js/Phaser.Physics))
                          :else (throw (js/Error. "System is not supported!"))))
                      ((:create state-obj) game))
                    ;; update
                    (update [_] ((:update state-obj) game))
                    ;; render
                    (render [_] ((:render state-obj) game))))))
        game))))




;; systems
(defn physical [game params]
  "Creates physical system object, attaches it to the entity and creates primitive (drawing)"
  (let [s (if (:group params)
            (PhysicalGroup. game (m/call-in* game [-world]) (:key params) (:group params))
            (PhysicalBody. game (:x params) (:y params) (:key params))) 
        v (create-primitive s)
        sys-enabled (assoc s :primitive v)]
    (enable-body! sys-enabled)
    sys-enabled))

;; helpers
(defn defentity [game params & systems]
  "Attaches systems to the entity map"
  (assoc params :systems (map #(% game params) systems)))

(defmulti load-resource (fn [game entity]
                          (:type (:asset entity))))

(defmethod load-resource
  :spritesheet
  [game entity]
  (let [asset (:asset entity)]
    (m/call-in* game [-load spritesheet] (:key entity) (:src asset) (:x asset) (:y asset) (:frames asset))))

(defmethod load-resource
  :default
  [game entity]
  (m/call-in* game [-load image] (:key entity) (:src (:asset entity))))
