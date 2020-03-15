/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Toporov Konstantin. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 3 only ("GPL")  (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/copyleft/gpl.html  See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * You should include file containing license in each project.
 * If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by
 * the GPL Version 3, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [GPL Version 3] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under the GPL Version 3 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 3 code and therefore, elected the GPL
 * Version 3 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Toporov Konstantin.
 */
package org.diacalc.android.maths


import java.util.Hashtable

/************************************************************************
 * *Mathematic expression evaluator.* Supports the following functions:
 * +, -, *, /, ^, %, cos, sin, tan, acos, asin, atan, sqrt, sqr, log, min, max, ceil, floor, abs, neg, rndr.<br></br>
 * When the getValue() is called, a Double object is returned. If it returns null, an error occured.
 *
 *
 * <pre>
 * Sample:
 * MathEvaluator m = new MathEvaluator("-5-6/(-2) + sqr(15+x)");
 * m.addVariable("x", 15.1d);
 * System.out.println( m.getValue() );
</pre> *
 * @version 1.1
 * @author    The-Son LAI, [Lts@writeme.com](mailto:Lts@writeme.com)
 * @date     April 2001
 */
open class MathEvaluator {
    private var node: Node? = null
    private var expression: String? = null
    private var variables: Hashtable<String?, Double?> = Hashtable()

    /***
     * creates an empty MathEvaluator. You need to use setExpression(String s) to assign a math expression string to it.
     */
    constructor() {
        init()
    }

    /***
     * creates a MathEvaluator and assign the math expression string.
     */
    constructor(s: String?) {
        init()
        setExpression(s)
    }

    private fun init() {
        if (operators == null) initializeOperators()
    }

    /***
     * adds a variable and its value in the MathEvaluator
     */
    fun addVariable(v: String?, `val`: Double) {
        addVariable(v, `val`)
    }

    /***
     * adds a variable and its value in the MathEvaluator
     */
    fun addVariable(v: String?, value: Double?) {
        variables[v] = value
    }

    /***
     * sets the expression
     */
    fun setExpression(s: String?) {
        expression = s
    }

    /***
     * resets the evaluator
     */
    fun reset() {
        node = null
        expression = null
        variables = Hashtable()
    }

    /***
     * trace the binary tree for debug
     */
    fun trace() {
        try {
            node = Node(expression!!)
            node!!.trace()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /***
     * evaluates and returns the value of the expression
     */
    val value: Double?
        get() = if (expression == null) null else try {
            node = Node(expression!!)
            evaluate(node)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            null
        }

    private fun initializeOperators() {
        operators = arrayOfNulls(25)
        operators!![0] = Operator("+", 2, 0)
        operators!![1] = Operator("-", 2, 0)
        operators!![2] = Operator("*", 2, 10)
        operators!![3] = Operator("/", 2, 10)
        operators!![4] = Operator("^", 2, 10)
        operators!![5] = Operator("%", 2, 10)
        operators!![6] = Operator("&", 2, 0)
        operators!![7] = Operator("|", 2, 0)
        operators!![8] = Operator("cos", 1, 20)
        operators!![9] = Operator("sin", 1, 20)
        operators!![10] = Operator("tan", 1, 20)
        operators!![11] = Operator("acos", 1, 20)
        operators!![12] = Operator("asin", 1, 20)
        operators!![13] = Operator("atan", 1, 20)
        operators!![14] = Operator("sqrt", 1, 20)
        operators!![15] = Operator("sqr", 1, 20)
        operators!![16] = Operator("log", 1, 20)
        operators!![17] = Operator("min", 2, 0)
        operators!![18] = Operator("max", 2, 0)
        operators!![19] = Operator("exp", 1, 20)
        operators!![20] = Operator("floor", 1, 20)
        operators!![21] = Operator("ceil", 1, 20)
        operators!![22] = Operator("abs", 1, 20)
        operators!![23] = Operator("neg", 1, 20)
        operators!![24] = Operator("rnd", 1, 20)
    }

    /***
     * gets the variable's value that was assigned previously
     */
    private fun getVariable(s: String?): Double? {
        return variables[s]
    }

    private fun getDouble(s: String?): Double? {
        if (s == null) return null
        var res: Double? = null
        try {
            res = s.toDouble()
        } catch (e: java.lang.Exception) {
            return getVariable(s)
        }
        return res
    }

    protected var operators: Array<Operator?>? = null

    inner class Operator(var operator: String, val type: Int, val priority: Int)

    protected open inner class Node {
        protected var string: String? = null
        var operator: Operator? = null
        var left: Node? = null
        var right: Node? = null
        var nParent: Node? = null
        private var level = 0
        var value: Double? = null

        constructor(s: String) {
            init(null, s, 0)
        }

        constructor(parent: Node?, s: String, level: Int) {
            init(parent, s, level)
        }

        @Throws(java.lang.Exception::class)
        private fun init(parent: Node?, s: String, level: Int) {
            var s = s
            s = removeIllegalCharacters(s)
            s = removeBrackets(s)
            s = addZero(s)
            if (checkBrackets(s) != 0) throw java.lang.Exception("Wrong number of brackets in [$s]")
            nParent = parent
            string = s
            this.value = getDouble(s)
            this.level = level
            val sLength = s.length
            var inBrackets = 0
            var startOperator = 0
            for (i in 0 until sLength) {
                if (s[i] == '(') inBrackets++ else if (s[i] == ')') inBrackets-- else {
                    // the expression must be at "root" level
                    if (inBrackets == 0) {
                        val o = getOperator(string, i)
                        if (o != null) {
                            // if first operator or lower priority operator
                            if (operator == null || operator!!.priority >= o.priority) {
                                operator = o
                                startOperator = i
                            }
                        }
                    }
                }
            }
            if (operator != null) {
                // one operand, should always be at the beginning
                if (startOperator == 0 && operator!!.type == 1) {
                    // the brackets must be ok
                    if (checkBrackets(s.substring(operator!!.operator.length)) == 0) {
                        left = Node(this, s.substring(operator!!.operator.length), this.level + 1)
                        right = null
                        return
                    } else throw java.lang.Exception("Error during parsing... missing brackets in [$s]")
                } else if (startOperator > 0 && operator!!.type == 2) {
                    //nOperator = nOperator;
                    left = Node(this, s.substring(0, startOperator), this.level + 1)
                    right = Node(this, s.substring(startOperator + operator!!.operator.length), this.level + 1)
                }
            }
        }

        private fun getOperator(s: String?, start: Int): Operator? {
            val operators = operators
            var temp: String = s!!.substring(start)
            temp = getNextWord(temp)
            if (operators != null) {
                for (i in operators.indices) {
                    if (temp.startsWith(operators!![i]!!.operator)) return operators!![i]
                }
            }
            return null
        }

        private fun getNextWord(s: String): String {
            val sLength = s.length
            for (i in 1 until sLength) {
                val c = s[i]
                if ((c > 'z' || c < 'a') && (c > '9' || c < '0')) return s.substring(0, i)
            }
            return s
        }

        /***
         * checks if there is any missing brackets
         * @return true if s is valid
         */
        protected fun checkBrackets(s: String): Int {
            val sLength = s.length
            var inBracket = 0
            for (i in 0 until sLength) {
                if (s[i] == '(' && inBracket >= 0) inBracket++ else if (s[i] == ')') inBracket--
            }
            return inBracket
        }

        /***
         * returns a string that doesnt start with a + or a -
         */
        protected fun addZero(s: String): String {
            if (s.startsWith("+") || s.startsWith("-")) {
                val sLength = s.length
                for (i in 0 until sLength) {
                    if (getOperator(s, i) != null) return "0$s"
                }
            }
            return s
        }

        /***
         * displays the tree of the expression
         */
        fun trace() {
            val op = if (operator == null) " " else operator!!.operator
            _D("$op : $string")
            if (hasChild()) {
                if (hasLeft()) left!!.trace()
                if (hasRight()) right!!.trace()
            }
        }

        fun hasChild(): Boolean {
            return left != null || right != null
        }

        fun hasOperator(): Boolean {
            return operator != null
        }

        protected fun hasLeft(): Boolean {
            return left != null
        }

        protected fun hasRight(): Boolean {
            return right != null
        }

        /***
         * Removes spaces, tabs and brackets at the begining
         */
        fun removeBrackets(s: String): String {
            var res = s
            if (s.length > 2 && res.startsWith("(") && res.endsWith(")") && checkBrackets(s.substring(1, s.length - 1)) == 0) {
                res = res.substring(1, res.length - 1)
            }
            return if (res != s) removeBrackets(res) else res
        }

        /***
         * Removes illegal characters
         */
        fun removeIllegalCharacters(s: String): String {
            val illegalCharacters = charArrayOf(' ')
            var res = s
            for (j in illegalCharacters.indices) {
                var i: Int = res.lastIndexOf(illegalCharacters[j], res.length)
                while (i != -1) {
                    val temp = res
                    res = temp.substring(0, i)
                    res += temp.substring(i + 1)
                    i = res.lastIndexOf(illegalCharacters[j], s.length)
                }
            }
            return res
        }

        private fun _D(s: String?) {
            var nbSpaces = ""
            for (i in 0 until level) nbSpaces += "  "
        }
    }

    companion object {
        protected var operators: Array<Operator?>? = null
        private fun evaluate(n: Node?): Double? {
            if (n!!.hasOperator() && n.hasChild()) {
                if (n.operator!!.type == 1) n.value = evaluateExpression(n.operator, evaluate(n.left), null) else if (n.operator!!.type == 2) n.value = evaluateExpression(n.operator, evaluate(n.left), evaluate(n.right))
            }
            return n.value
        }

        private fun evaluateExpression(o: Operator?, f1: Double?, f2: Double?): Double? {
            val op = o!!.operator
            var res: Double? = null
            if ("+" == op) res = f1!!.toDouble() + f2!!.toDouble() else if ("-" == op) res = f1!!.toDouble() - f2!!.toDouble() else if ("*" == op) res = f1!!.toDouble() * f2!!.toDouble() else if ("/" == op) res = f1!!.toDouble() / f2!!.toDouble() else if ("%" == op) res = f1!!.toDouble() % f2!!.toDouble() else if ("&" == op) res = f1!!.toDouble() + f2!!.toDouble() // todo
            else if ("|" == op) res = f1!!.toDouble() + f2!!.toDouble() // todo
            else if ("cos" == op) res = java.lang.Math.cos(f1!!.toDouble()) else if ("sin" == op) res = java.lang.Math.sin(f1!!.toDouble()) else if ("tan" == op) res = java.lang.Math.tan(f1!!.toDouble()) else if ("sqr" == op) res = f1!!.toDouble() * f1.toDouble() else if ("sqrt" == op) res = java.lang.Math.sqrt(f1!!.toDouble()) else if ("min" == op) res = java.lang.Math.min(f1!!.toDouble(), f2!!.toDouble()) else if ("max" == op) res = java.lang.Math.max(f1!!.toDouble(), f2!!.toDouble()) else if ("floor" == op) res = java.lang.Math.floor(f1!!.toDouble()) else if ("ceil" == op) res = java.lang.Math.ceil(f1!!.toDouble()) else if ("abs" == op) res = java.lang.Math.abs(f1!!.toDouble()) else if ("neg" == op) res = -f1!!.toDouble()
            //else if  ( "rnd".equals(op) ) 	res = new Double( Math.random() * f1.doubleValue() );
            return res
        }

        protected fun _D(s: String?) {
            java.lang.System.err.println(s)
        }
    }
}