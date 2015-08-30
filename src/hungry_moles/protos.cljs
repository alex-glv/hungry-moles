(ns hungry-moles.protos
  (:require-macros [hungry-moles.core :as m]))

(declare make-body make-group)
(def top-parent (atom))
;; PROTOS

(defprotocol IManageEntities
  (add-entity [this e])
  (get-entities [this])
  (update-entity [this e]))

(defprotocol IBody
  (enable-body! [this]))

(defprotocol IVisibleBody
  (update! [this entity])
  (add-animations [this a])
  (play-animation [this name]))

(defprotocol IEntityStorage
  (get-body [this e])
  (get-spec [this e])
  (register-entity [this e body primitive parent])
  (-get-primitive [this e])
  (-record-meta [this e]))
                  

(defrecord EntityStorage [st]
  IEntityStorage
  (-record-meta [this e]
    (get @st (:uuid (meta e))))
  (get-body [this e]
    (let [rm (-record-meta this e)]
      (:body rm)))
  (-get-primitive [this e]
    (let [rm (-record-meta this e)]
      (:primitive rm)))
  (get-spec [this e]
    (let [rm (-record-meta this e)]
      (:spec rm)))
  (register-entity [this spec uuid body primitive]
    (swap! st assoc
           uuid
           {:body body :primitive primitive :spec spec}))
  
  cljs.core/ICounted
  (-count [_] (count @st)))

(defprotocol IEntity
  (preload [this game])
  (create [this game]))

(defrecord PhysicalBody [game x y key primitive]
  IVisibleBody
  
  (update! [this entity]
    (let [x (:x entity)
          y (:y entity)
          p primitive]
      (set! (.-x p) x)
      (set! (.-y p) y)))

  (add-animations [this a]
    (doseq [anim-list a]
      (doseq [anim anim-list]
        (let [name (clj->js (first anim))
              d (second anim)]
          (m/call-in* primitive [-animations add] name (clj->js (:frames d)) (:fps d) (:loop d))))))

  (play-animation [this name]
    (m/call-in* primitive [play] name)))

(def storage (EntityStorage. (atom {})))

(defrecord Group [primitive]
  IBody
  (enable-body! [this]
    (set! (.-enableBody primitive) true))
  IManageEntities
  (add-entity [this e]
    (let [p (-get-primitive storage e)]
      (m/call-in* primitive [add] p)))
  
  (update-entity [this e]
    (update! (get-body storage e) e)))

(defn defscreen [& params]
  (let [params (apply hash-map params)
        [x y] (:size params)
        entities (:entities params)]
    (when (:states params)
      (let [game (js/Phaser.Game. x y (.-auto js/Phaser)  (:title params))]
        (doseq [[state-name state-obj] (:states params)]
          (.add
           (.-state game) (str (name state-name))
           (fn [game]
             (reify Object
               ;; preload
               (preload [_]
                 (doseq [e entities]
                   (preload (:entity (meta e)) game))
                 ((:preload state-obj) game (Group. (.-world game))))
               ;; create
               (create [_]
                 (let [top-group (Group. (.-world game))]
                   (enable-body! top-group)
                   (if-let [sys (:start-system params)]
                     (cond
                       (= :arcade sys) (.startSystem (.-physics game) (.-ARCADE js/Phaser.Physics))
                       :else (throw (js/Error. "System is not supported!"))))
                   (doseq [e entities]
                     (create (:entity (meta e)) game)
                     (add-entity top-group e))
                   ((:create state-obj) game top-group)))
               ;; update
               (update [_] ((:update state-obj) game (Group. (.-world game))))
               ;; render
               (render [_] ((:render state-obj) game))))))
        game))))


(defn register-world [world]
  (reset! top-parent (Group. world)))

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

(defn make-group [parent]
  (Group. parent))

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
