(ns cygnus-www.macros)

(defmacro defui [name args state & body]
  `(defn ~name ~args
     (let [~'self (reagent/atom ~state)]
       (fn [] ~@body))))

(defmacro defpage [name state & body]
  `(do
     (defn ~name []
       (let [~'self (reagent/atom ~state)]
         ;; Scope CSS
         (fn [] [:div.page-root {:id '~name} ~@body])))
     (secretary/defroute (str "/" (str '~name)) []
       (reset! state/page ~name))))

(defmacro forall [& args]
  `(doall (for ~@args)))
