(ns hungry-moles.core
  (:require [clojure.string :as str]))

(defmacro call-in* [js-object in & params]
  "Calls nested function on js-object
   (call-in* obj [prop method] (body))
   results in (. (. obj prop) method (body))"
  (into
   params
   (reverse
    (reduce (fn [c x] `(. ~c ~x)) js-object in))))


(defmacro defentity* [name params]
  `(def ~name
     (with-meta ~params
       (let [uuid# (hungry-moles.core/*uuid-fn*)]
         {:uuid uuid#
          :entity
          (reify IEntity
            (~'preload [this# game#]
              (load-resource game# ~params))
            (~'create [this# game#]
              (hungry-moles.core/create-entity game# uuid# ~params)))}))))

(defmacro defgroup* [name total params]
  (let [gen-names (take total (repeatedly #(gensym name)))]
    `(do
       ~@(map (fn [uuid] `(defentity* ~uuid ~params)) gen-names)
       (def ~name (vector ~@(map (fn [uuid] `@(var ~uuid)) gen-names))))))

;; (defgroup* invaders 20
;;     {:key "invaders"
;;      :asset {:src "assets/img/invader32x32x4.png"
;;              :type :spritesheet
;;              :x 32 :y 32 :frames nil}
;;      :animations [{:fly {:frames [0 1 2 3]
;;                          :loop true
;;                          :fps 20}}]
     
;;      :visible false
;;      })
