(ns hungry-moles.helpers)

(defn grid [x-count y-count start-from-x start-from-y spread]
  (for [y (range start-from-y (+ start-from-y (* y-count spread)) spread)]
    (for [x (range start-from-x (+ start-from-x (* x-count spread)) spread)]
      [x y])))
