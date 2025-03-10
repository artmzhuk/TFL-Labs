import lexems.combineLexems
import lexems.findWordForDFA
import net.automatalib.alphabet.Alphabet
import net.automatalib.alphabet.ArrayAlphabet
import net.automatalib.automaton.fsa.CompactDFA
import net.automatalib.util.automaton.fsa.DFAs
import net.automatalib.visualization.Visualization

fun test(alphabet: Alphabet<String>): CompactDFA<String> {
    val dfa = CompactDFA<String>(alphabet)
    dfa.addState()
    dfa.addState()
    dfa.addState()
    dfa.addState()
    dfa.initialState = 0
    dfa.addTransition(0, "b", 1)
    dfa.addTransition(0, "c", 1)
    dfa.addTransition(0, "1", 1)
    dfa.addTransition(0, "2", 1)
    dfa.addTransition(0, "0", 2)
    dfa.addTransition(1, "0", 2)
    dfa.addTransition(1, "a", 3)
    dfa.addTransition(1, "1", 3)
    dfa.addTransition(2, "b", 3)
    dfa.setAccepting(1, true)
    dfa.setAccepting(3, true)
    return dfa
}

fun test1(): CompactDFA<String> {
    val dfa = CompactDFA<String>(ArrayAlphabet("a", "b"))
    dfa.addState()
    dfa.addState()
    dfa.initialState = 0
    dfa.addTransition(0, "a", 0)
    dfa.addTransition(0, "b", 1)
    dfa.addTransition(1, "b", 1)
    dfa.setAccepting(1, true)

    val dfa1 = CompactDFA<String>(ArrayAlphabet("c", "d"))
    dfa1.addState()
    dfa1.addState()
    dfa1.initialState = 0
    dfa1.addTransition(0, "c", 0)
    dfa1.addTransition(0, "d", 1)
    dfa1.addTransition(1, "d", 1)
    dfa1.setAccepting(0, true)
    dfa1.setAccepting(1, true)

    Visualization.visualize(dfa)
    Visualization.visualize(dfa1)
    Visualization.visualize(DFAs.and(dfa, dfa, ArrayAlphabet()))
    println(DFAs.and(dfa, dfa1, ArrayAlphabet()).size())
    return dfa
}


fun test3() {
    val size = 3
    val nesting = 0
    val lexems = lexems.generateLexems(size, nesting)

    val eolWord = findWordForDFA(lexems.eolDFA)
    val constWord = findWordForDFA(lexems.constDFA)
    val equalWord = findWordForDFA(lexems.equalDFA)
    val lbr1Word = findWordForDFA(lexems.lbr1DFA)
    val rbr1Word = findWordForDFA(lexems.rbr1DFA)
    val varWord = findWordForDFA(lexems.varDFA)
    val sepWord = findWordForDFA(lexems.sepDFA)

    val res =
        eolWord + eolWord + constWord + lbr1Word + varWord + equalWord + constWord + sepWord + rbr1Word + eolWord + eolWord
    println("res word: $res")

    val resDFA = combineLexems(size, nesting, lexems)
    println(resDFA.accepts(res.map { it.toString() }.toMutableList()))

    Visualization.visualize(resDFA)

}

fun test4() {
    val size = 5
    val nesting = 1
    val lexems = lexems.generateLexems(size, nesting)

    val eolWord = findWordForDFA(lexems.eolDFA)
    val constWord = findWordForDFA(lexems.constDFA)
    val equalWord = findWordForDFA(lexems.equalDFA)
    val lbr1Word = findWordForDFA(lexems.lbr1DFA)
    val rbr1Word = findWordForDFA(lexems.rbr1DFA)
    val lbr2Word = findWordForDFA(lexems.lbr2DFA)
    val rbr2Word = findWordForDFA(lexems.rbr2DFA)
    val lbr3Word = findWordForDFA(lexems.lbr3DFA)
    val rbr3Word = findWordForDFA(lexems.rbr3DFA)
    val varWord = findWordForDFA(lexems.varDFA)
    val sepWord = findWordForDFA(lexems.sepDFA)
    val blankWord = findWordForDFA(lexems.blankDFA)

    val res = eolWord + eolWord +
            constWord + lbr1Word + eolWord +
            lbr3Word + varWord + rbr3Word +
            equalWord + lbr3Word + lbr2Word + constWord + blankWord + constWord + rbr2Word + rbr3Word +
            sepWord + rbr1Word + eolWord + eolWord
    println("res word: $res")

    val resDFA = combineLexems(size, nesting, lexems)
    println(resDFA.accepts(res.map { it.toString() }.toMutableList()))
}