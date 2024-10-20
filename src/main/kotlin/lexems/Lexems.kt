package lexems

import net.automatalib.alphabet.ArrayAlphabet
import net.automatalib.automaton.fsa.CompactDFA
import net.automatalib.util.automaton.fsa.DFAs
import net.automatalib.visualization.Visualization

fun generateAlphabets() {
    val initialSymbols = mutableSetOf("a", "b", "c", "0", "1", "2")

    val firstSet = initialSymbols.shuffled().take(2).toSet()
    val secondSet = initialSymbols.shuffled().take(1).toSet()
    val thirdSet = initialSymbols.shuffled().take(3).toSet()

    println("First Set: $firstSet")
    println("Second Set: $secondSet")
    println("Third Set: $thirdSet")
}

fun generateLexems(size: Int, nesting: Int) {
    val initialSymbols = mutableSetOf("a", "b", "c", "0", "1", "2").shuffled()

    val eolBlankList = initialSymbols.subList(0, 2)
    val equalSep = initialSymbols.subList(2, 3)
    val constVarSet = initialSymbols.subList(3, 5)
    val bracketsSet = initialSymbols.subList(5, 6)

    val constDFA = generateConst(size, ArrayAlphabet<String>(constVarSet.toTypedArray()[0]))
    val varDFA = generateVar(size, ArrayAlphabet<String>(constVarSet.toTypedArray()[1]), constDFA)
    val eolDFA = generateBlankAndEOL(size, ArrayAlphabet<String>(eolBlankList[0]))
    val blankDFA = generateBlankAndEOL(size, ArrayAlphabet<String>(eolBlankList[1]))
    val equalDFA = generateEqualAndSep(size, equalSep.toTypedArray())
    val sepDFA = generateEqualAndSep(size, equalSep.toTypedArray())
    val lbr1DFA = generateLbr(size, ArrayAlphabet<String>(*bracketsSet.toTypedArray()), varDFA, constDFA)
    var lbr2DFA = generateLbr(size, ArrayAlphabet<String>(*bracketsSet.toTypedArray()), varDFA, constDFA)
    var lbr3DFA = generateLbr(size, ArrayAlphabet<String>(*bracketsSet.toTypedArray()), varDFA, constDFA)
    var rbr1DFA = generateRbr(size, ArrayAlphabet<String>(*bracketsSet.toTypedArray()), varDFA, constDFA, lbr1DFA)
    var rbr2DFA = generateRbr(size, ArrayAlphabet<String>(*bracketsSet.toTypedArray()), varDFA, constDFA, lbr2DFA)
    var rbr3DFA = generateRbr(size, ArrayAlphabet<String>(*bracketsSet.toTypedArray()), varDFA, constDFA, lbr3DFA)


/*    Visualization.visualize(constDFA)
    Visualization.visualize(varDFA)*/
    //Visualization.visualize(DFAs.and(constDFA, varDFA,  ArrayAlphabet()))
/*    Visualization.visualize(eolDFA)
    Visualization.visualize(blankDFA)
    Visualization.visualize(equalDFA)
    Visualization.visualize(sepDFA)

    Visualization.visualize(lbr1DFA)
    Visualization.visualize(lbr2DFA)
    Visualization.visualize(lbr3DFA)

    Visualization.visualize(rbr1DFA)
    Visualization.visualize(rbr2DFA)
    Visualization.visualize(rbr3DFA)*/

}

fun generateRbr(size:Int, alphabet: ArrayAlphabet<String>, varDFA: CompactDFA<String>, constDFA: CompactDFA<String>, lbrDFA: CompactDFA<String>):CompactDFA<String>{
    while (true){
        val rbr1DFA = generateFiniteAutomata(size, alphabet)
        val andConstSize = dfaAnd(rbr1DFA, constDFA).size()
        val andVarSize = dfaAnd(rbr1DFA, varDFA).size()
        val andLbrSize = dfaAnd(rbr1DFA, lbrDFA).size()
        if ((andConstSize == 1 || andConstSize == 0) && (andVarSize == 1 || andVarSize == 0)
            && (andLbrSize == 1 || andLbrSize == 0)){
            val concat = concatenateAutomata(lbrDFA, rbr1DFA)
            val concatAndLbr =  dfaAnd(lbrDFA, concat)
            val concatAndRbr = dfaAnd(rbr1DFA, concat)
            if ((concatAndRbr.size() == 0 || concatAndRbr.size() == 1) && (concatAndLbr.size() == 0 || concatAndLbr.size() == 1)){
                return rbr1DFA
            }
        }
    }
}

fun generateLbr(size:Int, alphabet: ArrayAlphabet<String>, varDFA: CompactDFA<String>, constDFA: CompactDFA<String>):CompactDFA<String>{
    while (true){
        val lbr1DFA = if (size > 2){
            generateFiniteAutomata(2, alphabet)
        } else {
            generateFiniteAutomata(size, alphabet)
        }
        //Visualization.visualize(lbr1DFA)
        //Visualization.visualize(constDFA)
        val andConstSize = dfaAnd(lbr1DFA, constDFA).size()
        //Visualization.visualize(DFAs.and(lbr1DFA, constDFA, alphabet))

        val andVarSize = dfaAnd(lbr1DFA, varDFA).size()
        if ((andConstSize == 1 || andConstSize == 0) && (andVarSize == 1 || andVarSize == 0)){
            return lbr1DFA
        }
    }
}

fun generateEqualAndSep(size: Int, alphabet:Array<String>):CompactDFA<String>{
    if (alphabet.size > 1){
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
        while (true){
            var dfa = generateFiniteAutomata(size, ArrayAlphabet<String>(*alphabet))
            if (dfa.size() > 2){
                return dfa
            }
        }
    }
}

fun generateBlankAndEOL(size:Int, alphabet: ArrayAlphabet<String>):CompactDFA<String>{
    return generateFiniteAutomata(size, alphabet)
}
fun generateConst(size: Int, alphabet: ArrayAlphabet<String>):CompactDFA<String> {
    val constAutomata = generateInfiniteAutomata(size, alphabet)
    return constAutomata
}

fun generateVar(size:Int, alphabet: ArrayAlphabet<String>, constDFA:CompactDFA<String>):CompactDFA<String>{
    while (true){
        val varAutomata = generateInfiniteAutomata(size, alphabet)
        if(dfaAnd(varAutomata, constDFA).size() == 1){
            return varAutomata
        }
    }
}
