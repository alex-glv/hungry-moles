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

(defmacro defentity* [name uuid params]
  `(def ~name
     (with-meta ~params
       {:uuid ~uuid
        :entity
        (reify hungry-moles.protos/IEntity
          (~'preload [this# game#]
            (hungry-moles.protos/load-resource game# ~params)
            )
          (~'create [this# game# primitive#]
            (hungry-moles.protos/PhysicalBody.
             game# ~(:x params) ~(:y params) ~(:key params)
             ~(:animations params) primitive#)
            ))})))
