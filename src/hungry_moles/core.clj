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
       (let [uuid# (*uuid-fn*)]
         {:uuid uuid#
          :entity
          (reify hungry-moles.protos/IEntity
            (~'preload [this# game#]
              (hungry-moles.protos/load-resource game# ~params))
            (~'create [this# game#]
              (let [p# (hungry-moles.protos/make-body game# ~params)
                    b# (hungry-moles.protos/PhysicalBody. game# ~(:x params) ~(:y params) ~(:key params) p#)]
                (hungry-moles.protos/add-animations b# ~(:animations params))
                (hungry-moles.protos/register-entity
                 hungry-moles.protos/storage ~params uuid# b# p#))))}))))
