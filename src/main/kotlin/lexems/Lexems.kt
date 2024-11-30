package lexems

import net.automatalib.alphabet.ArrayAlphabet
import net.automatalib.automaton.fsa.CompactDFA
import net.automatalib.visualization.Visualization

class LexemBundle(
    val constDFA: CompactDFA<String>,
    val varDFA: CompactDFA<String>,
    val eolDFA: CompactDFA<String>,
    val blankDFA: CompactDFA<String>,
    val equalDFA: CompactDFA<String>,
    val sepDFA: CompactDFA<String>,
    val lbr1DFA: CompactDFA<String>,
    val lbr2DFA: CompactDFA<String>,
    val lbr3DFA: CompactDFA<String>,
    val rbr1DFA: CompactDFA<String>,
    val rbr2DFA: CompactDFA<String>,
    val rbr3DFA: CompactDFA<String>
)


fun generateLexems(size: Int, nesting: Int): LexemBundle {
    val initialSymbols = mutableSetOf("a", "b", "c", "0", "1", "2").shuffled()

    val eolBlankList = initialSymbols.subList(0, 2)
    val equalSep = initialSymbols.subList(2, 3)
    val constVarBracketsSet = initialSymbols.subList(3, 6)

    val constDFA = generateConst(size, ArrayAlphabet<String>(constVarBracketsSet.toTypedArray()[0]))
    val varDFA = generateVar(size, ArrayAlphabet<String>(constVarBracketsSet.toTypedArray()[1]), constDFA)
    val eolDFA = generateBlankAndEOL(size, ArrayAlphabet<String>(eolBlankList[0]))
    val blankDFA = generateBlankAndEOL(size, ArrayAlphabet<String>(eolBlankList[1]))
    val equalDFA = generateEqualAndSep(size, equalSep.toTypedArray())
    val sepDFA = generateEqualAndSep(size, equalSep.toTypedArray())
    val lbr1DFA = generateLbr(size, ArrayAlphabet<String>(*constVarBracketsSet.toTypedArray()), varDFA, constDFA)
    var lbr2DFA = generateLbr(size, ArrayAlphabet<String>(*constVarBracketsSet.toTypedArray()), varDFA, constDFA)
    var lbr3DFA = generateLbr(size, ArrayAlphabet<String>(*constVarBracketsSet.toTypedArray()), varDFA, constDFA)
    var rbr1DFA = generateRbr(size, ArrayAlphabet<String>(*constVarBracketsSet.toTypedArray()), varDFA, constDFA, lbr1DFA)
    var rbr2DFA = generateRbr(size, ArrayAlphabet<String>(*constVarBracketsSet.toTypedArray()), varDFA, constDFA, lbr2DFA)
    var rbr3DFA = generateRbr(size, ArrayAlphabet<String>(*constVarBracketsSet.toTypedArray()), varDFA, constDFA, lbr3DFA)



    val lexemBundle = LexemBundle(
        constDFA, varDFA, eolDFA, blankDFA, equalDFA, sepDFA,
        lbr1DFA, lbr2DFA, lbr3DFA, rbr1DFA, rbr2DFA, rbr3DFA
    )

    return lexemBundle
}

fun generateRbr(
    size: Int,
    alphabet: ArrayAlphabet<String>,
    varDFA: CompactDFA<String>,
    constDFA: CompactDFA<String>,
    lbrDFA: CompactDFA<String>
): CompactDFA<String> {
    while (true) {
        val rbr1DFA = generateFiniteAutomata(size, alphabet)
        val andConst = dfaAnd(rbr1DFA, constDFA)
        val andVar = dfaAnd(rbr1DFA, varDFA)
        val andLbr = dfaAnd(rbr1DFA, lbrDFA)
        if (isEmpty(andConst) && isEmpty(andVar)
            && isEmpty(andLbr)
        ) {
            val concat = concatenateAutomata(lbrDFA, rbr1DFA)
            val concatAndLbr = dfaAnd(lbrDFA, concat)
            val concatAndRbr = dfaAnd(rbr1DFA, concat)
            if ((concatAndRbr.size() == 0 || concatAndRbr.size() == 1) && (concatAndLbr.size() == 0 || concatAndLbr.size() == 1)) {
                return rbr1DFA
            }
        }
    }
}

fun generateLbr(
    size: Int,
    alphabet: ArrayAlphabet<String>,
    varDFA: CompactDFA<String>,
    constDFA: CompactDFA<String>
): CompactDFA<String> {
    while (true) {
        val lbr1DFA = generateFiniteAutomata(size, alphabet)
        val andConst = dfaAnd(lbr1DFA, constDFA)
        val andVar =  dfaAnd(lbr1DFA, varDFA)

        if (isEmpty(andVar) && isEmpty(andConst)) {//TODO
            return lbr1DFA
        }
    }
}

fun generateEqualAndSep(size: Int, alphabet: Array<String>): CompactDFA<String> {
    if (alphabet.size > 1) {
        val firstLast = alphabet[0]
        val alphabetNew = alphabet.copyOfRange(1, alphabet.size)
        println(firstLast)
        var dfa = generateFiniteAutomata(size, ArrayAlphabet<String>(*alphabetNew))
        val dfa1 = CompactDFA<String>(ArrayAlphabet(firstLast))
        dfa1.addState()
        dfa1.addState()
        dfa1.initialState = 0
        dfa1.addTransition(0, firstLast, 1)
        dfa1.setAccepting(1, true)
        //Visualization.visualize(dfa)
        val concat1 = concatenateAutomata(dfa1, dfa)
        //Visualization.visualize(concat1)
        val concat2 = concatenateAutomata(concat1, dfa1)
        //Visualization.visualize(concat2)
        return concat2
    } else {
        while (true) {
            var dfa = generateFiniteAutomata(size, ArrayAlphabet<String>(*alphabet))
            if (dfa.size() > 2) {
                return dfa
            }
        }
    }
}

fun generateBlankAndEOL(size: Int, alphabet: ArrayAlphabet<String>): CompactDFA<String> {
    return generateFiniteAutomata(size, alphabet)
}

fun generateConst(size: Int, alphabet: ArrayAlphabet<String>): CompactDFA<String> {
    val constAutomata = generateInfiniteAutomata(size, alphabet)
    return constAutomata
}

fun generateVar(size: Int, alphabet: ArrayAlphabet<String>, constDFA: CompactDFA<String>): CompactDFA<String> {
    while (true) {
        val varAutomata = generateInfiniteAutomata(size, alphabet)
        if (isEmpty(dfaAnd(varAutomata, constDFA))) {
            return varAutomata
        }
    }
}

fun isEmpty(a: CompactDFA<String>): Boolean{
    return if (a.size() == 0){
        true
    } else if (a.size() == 1){
        val numOfTransitions = a.getTransitions(a.initialState).size
        numOfTransitions == 0
    } else {
        false
    }
}