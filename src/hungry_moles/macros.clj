(ns hungry-moles.macros
  (:require [clojure.string :as str]))

;; NOPE:
;; (defmacro create [of-type params & body]
;;   (let [tmn (fn [x] (let [s-str (clojure.string/split (name x) #"-" )
;;                          coll ["." (first s-str)]]
;;                      (str/join (into coll (map str/capitalize (rest s-str) )))))
;;         ]
;;     `(let [obj# (~of-type ~@params)]
;;        ~@(for [call body]
;;            `(~(symbol (tmn (first call))) obj# ~@(rest call)))
;;        obj#)
;;     ))

;; (defmacro defscreen* [& params]
;;   (let [params (apply hash-map params)
;;         [x y] (:size params)]
;;     (when (:states params)
;;       (let [ob# 'Object up# 'update cr# 'create
;;             rn# 'render pr# 'preload un# '_ gm# (gensym "game")]
;;         `(let [~gm# (js/Phaser.Game. ~x ~y (.-auto js/Phaser)  ~(:title params))]
;;            ~@(for [[state-name state-obj] (:states params)]
;;                `(.add (.-state ~gm#) ~(str (name state-name))
;;                       (deftype ~(gensym (name state-name)) [game#] ~ob#
;;                                ;; preload
;;                                (~pr# [~un#] (
;;                                              ;; ADD ENTITIES PRELOAD
;;                                              ~(:preload state-obj) game#))
;;                                ;; create
;;                                (~cr# [~un#]
;;                                  (if-let [sys# ~(:start-system params)]
;;                                    (cond
;;                                      (= :arcade sys#) (.startSystem (.-physics game#) (.-ARCADE js/Phaser.Physics))
;;                                      :else (throw (js/Error. "System is not supported!"))))
;;                                  (~(:create state-obj) game#))
;;                                ;; update
;;                                (~up# [~un#] (~(:update state-obj) game#))
;;                                ;; render
;;                                (~rn# [~un#] (~(:render state-obj) game#)))))

           
;;            ~gm#)))))

(defmacro call-in* [js-object in & params]
  "Calls nested function on js-object
   (call-in* obj [prop method] (body))
   results in (. (. obj prop) method (body))"
  (into
   params
   (reverse
    (reduce (fn [c x] `(. ~c ~x)) js-object in))))
