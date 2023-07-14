(defn constant [n] (constantly n))
(defn variable [var] (fn [args] (args var)))
(defn op [operation] (fn [& args] (fn [m] (apply operation (mapv #(% m) args)))))
(def add (op +))
(def subtract (op -))
(def multiply (op *))
(defn div
  ([arg] (/ 1 (double arg)))
  ([arg & args] (/ arg (double (apply * args)))))
(def divide (op div))
(defn negate [arg] ((op -) arg))
(defn mean-square [& args] (/ (apply + (mapv #(* % %) args)) (count args)))
(def meansq (op mean-square))
(def rms (op (fn [& args] (Math/sqrt (apply mean-square args)))))
(def functionOperations {
                 "number" constant
                 "symbol" variable
                 '+ add
                 '- subtract
                 '* multiply
                 '/ divide
                 'negate negate
                 'meansq meansq
                 'rms rms
                 })
(defn parse [args operations]
  (cond
    (number? args) ((operations "number") args)
    (symbol? args) ((operations "symbol") (name args))
    :else (apply (operations (peek args)) (mapv #(parse % operations) (rest args)))))
(defn parseFunction [line] (parse (read-string line) functionOperations))

(load-file "proto.clj")
(def evaluate (method :evaluate))
(def toString (method :toString))
(def toStringInfix (method :toStringInfix))
(def diff (method :diff))
(def calculate (method :calculate))
(def value (field :value))
(def varb (field :varb))
(def args (field :args))
(def symbolOperation (field :symbolOperation))
(declare Add)
(declare Subtract)
(declare Multiply)
(declare Divide)
(declare Negate)
(declare Constant)
(declare Variable)
(declare Sumexp)
(declare LSE)
(defn createExpression [ctor proto op sym diff] (constructor ctor (assoc proto
                                                              :calculate (fn [this] op)
                                                              :symbolOperation sym
                                                              :diff diff)))
(def ConstantPrototype
  {:evaluate (fn [this vars] (value this))
   :toString (fn [this]  (str (value this)))
   :toStringInfix (fn [this] (str (value this)))
   :diff (fn [this var] (Constant 0))
   })
(defn _Constant [this value] (assoc this :value value))
(def Constant (constructor _Constant ConstantPrototype))
(def one (Constant 1))
(def zero (Constant 0))
(def VariablePrototype
  {:evaluate (fn [this vars] (vars (varb this)))
   :toString (fn [this] (varb this))
   :toStringInfix (fn [this] (varb this))
   :diff (fn [this var] (if (= var (varb this)) one zero))
   })
(defn _Variable [this variable] (assoc this :varb variable))
(def Variable (constructor _Variable VariablePrototype))
(def ExpressionPrototype
  {:evaluate (fn [this vars] (apply (calculate this) (mapv #(evaluate % vars) (args this))))
   :toString (fn [this] (str "(" (symbolOperation this) " " (clojure.string/join " " (mapv toString (args this))) ")"))
   :toStringInfix (fn [this] (str "(" (toStringInfix (first (args this))) " " (symbolOperation this) " "
                                  (clojure.string/join (str " " (symbolOperation this) " ")
                                                       (mapv toStringInfix (rest (args this)))) ")"))
   })
(defn Expression [this & args] (assoc this :args args))
(defn UnaryExpression [this arg] (Expression this arg))
(defn diffEasy [this var op] (apply op (mapv #(diff % var) (args this))))
(defn diffAdd [this var] (diffEasy this var Add))
(defn diffSub [this var] (diffEasy this var Subtract))
(defn diffMul ([var args] (let [first (first args) other (rest args)]
                            (if (= 1 (count args))
                              (diff first var)
                              (Add (apply Multiply (diff first var) other) (Multiply first (diffMul var other)))))))
(defn diffMultiply [this var] (diffMul var (args this)))
(defn diffDiv [var args]
  (cond
    (= 1 (count args)) (diffDiv var [one (first args)])
    (= 2 (count args)) (let [first (first args) second (last args)]
                         (Divide (Subtract (Multiply (diff first var) second)
                                           (Multiply first (diff second var)))
                                 (Multiply second second)))
    :else (diffDiv var [(first args) (apply Multiply (rest args))])))
(defn diffDivide [this var] (diffDiv var (args this)))
(defn diffNegate [this var] (Negate (diff (first (args this)) var)))
(defn diffSumexp [this var] (apply Add (mapv #(Multiply (diff % var) (Sumexp %)) (args this))))
(defn diffLSE [this var] (Divide (diff (apply Sumexp (args this)) var) (apply Sumexp (args this))))
(defn sumexp [& args] (apply + (mapv #(Math/exp %) args)))
(defn lse [& args] (Math/log (apply sumexp args)))
(defn createOperation [op sym diff] (createExpression Expression ExpressionPrototype op sym diff))
(def Add (createOperation + "+" diffAdd))
(def Subtract (createOperation - "-" diffSub))
(def Multiply (createOperation * "*" diffMultiply))
(def Divide (createOperation div "/" diffDivide))
(def Negate (createExpression UnaryExpression ExpressionPrototype - "negate" diffNegate))
(def Sumexp (createOperation sumexp "sumexp" diffSumexp))
(def LSE (createOperation lse "lse" diffLSE))
(def objectOperations {
                 "number" Constant
                 "symbol" Variable
                 '+ Add
                 '- Subtract
                 '* Multiply
                 '/ Divide
                 'negate Negate
                 'sumexp Sumexp
                 'lse LSE
                 })
(defn parseObject [line] (parse (read-string line) objectOperations))
