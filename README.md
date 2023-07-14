# ExpressionParserClojure

Два парсера выражений на Clojure

Оригинал задания доступен по [ссылке](https://www.kgeorgiy.info/courses/paradigms/homeworks.html#clojure-functional-expressions).

## Functional parser

- Функции constant, variable, add, subtract, multiply, divide, negate, meansq(среднее квадратов), rms(корень из среднего квадратов) служат для представления арифметических выражений.
  - Пример описания выражения ```2x-3```:
    ```
    (def expr
      (subtract
        (multiply
          (constant 2)
          (variable "x"))
        (constant 3)))
    ```
  - Выражение должно быть функцией, возвращающей значение выражения при подстановке переменных, заданных отображением. Например, ```(expr {"x" 2})``` равно ```1```.
```parseFunction``` - разборщик выражений, читающий выражения в стандартной для Clojure форме. Например, ```(parseFunction "(- (* 2 x) 3)")``` эквивалентно expr.
- Все функции, если это имеет смысл, могут принимать любое количество аргументов.

## Object parser
- Конструкторы Constant, Variable, Add, Subtract, Multiply, Divide, Negate, Sumexp(сумма экспонент), LSE(ln от sumexp) служат для представления арифметических выражений.
  - Пример описания выражения ```2x-3```:
    ```
    (def expr
      (Subtract
        (Multiply
          (Constant 2)
          (Variable "x"))
        (Constant 3)))
    ```
  - Функция ```(evaluate expression vars)``` производит вычисление выражения expression для значений переменных, заданных отображением vars. Например, ```(evaluate expr {"x" 2})``` равно ```1```.
  - Функция ```(toString expression)``` выдаёт запись выражения в стандартной для Clojure форме.
  - Функция ```(parseObject "expression")``` разбирает выражения, записанные в стандартной для Clojure форме. Например, ```(parseObject "(- (* 2 x) 3)")``` эквивалентно expr.
  - Функция ```(diff expression "variable")``` возвращает выражение, представляющее производную исходного выражения по заданой перемененной.
