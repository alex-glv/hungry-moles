(ns hungry-moles.protos
  (:require-macros [hungry-moles.core :as m]))

(def entities (atom))
(declare make-body)

;; PROTOS

(defprotocol IManageEntities
  (add-entity [this e])
  (get-entities [this])
  (update-entity [this e]))

(defprotocol IVisibleBody
  (attach [this parent])
  (update! [this entity])
  (add-animations [this])
  (play-animation [this name]))

(defprotocol IEntityStorage
  (get-body [this uuid])
  (get-primitive [this uuid])
  (get-spec [this uuid])
  (register-entity [this e]))

(defprotocol IEntity
  (preload [this game])
  (create [this game primitive]))

(defrecord PhysicalBody [game x y key animations primitive]
  IVisibleBody
  (attach [this parent]
    (add-animations this)
    (m/call-in* parent [add] primitive))
  
  (update! [this entity]
    (let [x (:x entity)
          y (:y entity)
          p primitive]
      (set! (.-x p) x)
      (set! (.-y p) y)))

  (add-animations [this]
    (doseq [anim-list animations]
      (doseq [anim anim-list]
        (let [name (clj->js (first anim))
              d (second anim)]
          (m/call-in* primitive [-animations add] name (clj->js (:frames d)) (:fps d) (:loop d))))))

  (play-animation [this name]
    (m/call-in* primitive [play] name)))


(extend-type js/Phaser.World
  IManageEntities
  (add-entity [this e]
    (let [uuid (:uuid (meta e))
          primitive (make-body (.-game this) e)
          c (create (:entity (meta e)) (.-game this) primitive)]
      (swap! entities assoc uuid c)
      (attach c this)))
  
  (update-entity [this e]
    (let [uuid (:uuid (meta e))]
      (update! (get @entities uuid) e))))

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




(defn make-body [game params]
  "Creates physical system object, attaches it to the entity and creates primitive (drawing)"
  (let [cp
        (fn [game record]
          (let [p (js/Phaser.Sprite. game (:x record) (:y record) (:key record))]
            (set! (.-physicsBodyType p) (.-ARCADE js/Phaser.Physics))
            p))
        s (cp game params)]
    s))

;; helpers

(defn defentity [game params]
  "Attaches systems to the entity map"
  (assoc params :body (make-body game params)))


(defmulti load-resource (fn [game entity]
                          (:type (:asset entity))))

(defmethod load-resource
  :spritesheet
  [game entity]
  (let [asset (:asset entity)]
    (m/call-in* game [-load spritesheet] (:key entity) (:src asset) (:x asset) (:y asset))))

(defmethod load-resource
  :default
  [game entity]
  (m/call-in* game [-load image] (:key entity) (:src (:asset entity))))

(defn get-body [uuid]
  (get @entities uuid))
