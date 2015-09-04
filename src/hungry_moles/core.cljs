(ns ^:figwheel-always hungry-moles.core
    (:use
     [cljs-uuid-utils.core :only (make-random-uuid)])
    (:require-macros [hungry-moles.core :refer (defentity* call-in*)]))


(def *uuid-fn* make-random-uuid)
(declare make-body make-group)
(def world (atom))

;; PROTOS

(defprotocol IManageEntities
  (add-entity [this uuid])
  (get-entities [this])
  (update-entity [this uuid])
  )

(defprotocol IBody
  (enable-body! [this]))

(defprotocol IVisibleBody
  (update! [this entity])
  (set-alive! [this entity])
  (set-visible! [this entity])
  (add-animations [this a])
  (play-animation [this name]))

(defprotocol IEntityStorage
  (get-body [this e])
  (get-spec [this e])
  (register-entity [this e ^:mutable body ^:mutable primitive parent])
  (-get-primitive [this e])
  (-record-meta [this e]))
                  
(defprotocol IEntity
  (preload [this game])
  (create [this game parent]))

(defrecord EntityStorage [ ^:mutable st]
  IEntityStorage
  (-record-meta [this uuid]
    (get @st uuid))
  (get-body [this uuid]
    (let [rm (-record-meta this uuid)]
      (:body rm)))
  (-get-primitive [this uuid]
    (let [rm (-record-meta this uuid)]
      (:primitive rm)))
  (get-spec [this uuid]
    (let [rm (-record-meta this uuid)]
      (:spec rm)))
  (register-entity [this spec uuid body primitive]
    (swap! st assoc
           uuid
           {:body body :primitive primitive :spec spec}))
  
  cljs.core/ICounted
  (-count [_] (count @st))
  cljs.core/IFn
  (-invoke [this e k]
    (get (-record-meta this e) k)))

(defn defanimations [a p]
  (doseq [anim-list a]
    (doseq [[an as] anim-list]
      (let [name (clj->js an)]
        (call-in* p [-animations add] name (clj->js (:frames as)) (:fps as) (:loop as))))))

(defrecord PhysicalBody [game x y key ^:mutable primitive]
  IVisibleBody
  
  (update! [this entity]
    (let [x (:x entity)
          y (:y entity)
          ;; v (:visible entity)
          p primitive]
      (set! (.-x p) x)
      (set! (.-y p) y)))

  (set-visible! [this entity]
    (set! (.-visible primitive) (:visible entity)))
  
  (set-alive! [this entity]
    (set! (.-visible primitive) (:alive entity)))
  ;; (set-exists! [this entity]
  ;;   (set! (.-exists primitive) (:exists entity)))
  
  (add-animations [this a]
    (defanimations a primitive))

  (play-animation [this name]
    (call-in* primitive [play] name)))

(def storage (EntityStorage. (atom {})))

(defrecord Group [primitive key]
  IBody
  (enable-body! [this]
    (set! (.-enableBody primitive) true))
  IManageEntities
  (add-entity [this uuid]
    (let [p (-get-primitive storage uuid)]
      (call-in* primitive [add] p)))
  
  (update-entity [this e]
    (update! (get-body storage (:uuid (meta e))) e))

  IVisibleBody
  (add-animations [this a]
    (doseq [p (.-children primitive)]
      (defanimations a p)))
  
  (play-animation [this name]
    (doseq [p (.-children primitive)]
      (call-in* p [play] name))))

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
                 ((:preload state-obj) game (make-group (.-world game))))
               ;; create
               (create [_]
                 (let [top-group (make-group (.-world game))]
                   (enable-body! top-group)
                   (if-let [sys (:start-system params)]
                     (cond
                       (= :arcade sys) (.startSystem (.-physics game) (.-ARCADE js/Phaser.Physics))
                       :else (throw (js/Error. "System is not supported!"))))
                   (doseq [e entities]
                     (.log js/console (:primitive top-group))
                     (create (:entity (meta e)) game top-group))
                   ((:create state-obj) game top-group)))
               ;; update
               (update [_] ((:update state-obj) game (make-group (.-world game))))
               ;; render
               (render [_] ((:render state-obj) game))))))
        game)))
  )


(defn register-world [world]
  (reset! world (make-group world)))

(defn make-body [game params]
  "Creates physical system object, attaches it top-group the entity and
  creates primitive (drawing)"
  (letfn [(cp [game record]
            (let [p (js/Phaser.Sprite. game (:x record) (:y record) (:key record))]
              (set! (.-visible p) (:visible record))
              (set! (.-exists p) (:visible record))
              (set! (.-alive p) (:visible record))
              (set! (.-physicsBodyType p) (.-ARCADE js/Phaser.Physics))
              p))]
          (cp game params)))

;; helpers


(defn make-group
  ([parent]
   (make-group parent nil))
  ([parent key]
   (Group. parent key)))


(defmulti load-resource (fn [game entity]
                          (:type (:asset entity))))

(defmethod load-resource
  :spritesheet
  [game entity]
  (let [asset (:asset entity)]
    (call-in* game [-load spritesheet] (:key entity) (:src asset) (:x asset) (:y asset))))

(defmethod load-resource
  :default
  [game entity]
  (call-in* game [-load image] (:key entity) (:src (:asset entity))))


(defn create-entity [game uuid params]
  (let [p (make-body game params)
        b (PhysicalBody. game (:x params) (:y params) (:key params) p)]
    (add-animations b (:animations params))
    ;; (set-visible! b (:visible params))
    ;; (set-alive! b (:visible params))
    (register-entity storage params uuid b p)))


;;;
;; (with-group invaders 
;;   (play-animation "fly"))
