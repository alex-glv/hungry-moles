(ns hungry-moles.protos
  (:require-macros [hungry-moles.macros :as m]))


;; PROTOS

(defprotocol IManageEntities
  (add-entities [this entities])
  (get-entities [this])
  (update-entities [this entities]))

(defprotocol IVisibleEntity
  (pos-update! [this params])
  (add! [this])
  (create-primitive [this])
  (preload [this]))

(defprotocol IBodyEntity
  (enable-body! [this]))

(defrecord SpriteEntity [game x y key]
  IVisibleEntity
  (pos-update! [this params]
    (let [p (:primitive this)]
      (set! (.-x p) (:x params))
      (set! (.-y p) (:y params))))
  
  (create-primitive [this]
    (let [p (js/Phaser.Sprite. game (:x this) (:y this) (:key this))]
      p))
  
  IBodyEntity
  (enable-body! [this]
    (let [p (:primitive this)]
      (set! (.-physicsBodyType p) (.-ARCADE js/Phaser.Physics)))))

;; TYPES

(extend-type js/Phaser.World
  IManageEntities
  (add-entities [this entities]
    (doseq [e entities]
      (.log js/console e)
      (doseq [s (:systems e)]
        (let [p (:primitive s)]
          (m/call-in* this [add] p)))))
  
  (get-entities [this] )
  
  (update-entities [this entities]
    (doseq [e entities]
      (doseq [s (:systems e)]
        (pos-update! s e)))))
  

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
  (let [s (SpriteEntity. game (:x params) (:y params) (:key params)) 
        v (create-primitive s)
        sys-enabled (assoc s :primitive v)]
    (enable-body! sys-enabled)
    sys-enabled))

;; helpers
(defn defentity [game params & systems]
  (assoc params :systems (map #(% game params) systems)))


;; (defprotocol ISystem
;;   (methods [this]))

;; (deftype Physical [x y key]
;;   (methods [_]
;;     [(fn [])
;;      (fn [])]))
