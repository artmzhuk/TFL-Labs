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
    val initialSymbols = mutableSetOf("a", "b", "c", "0", "1", "2")

    val eolBlankSet = initialSymbols.shuffled().take(2).toSet()
    val equalSep = initialSymbols.shuffled().take(2).toSet()
    val constVarSet = initialSymbols.shuffled().take(2).toSet()
    val firstArray = eolBlankSet.toTypedArray()

    val constDFA = generateConst(size, ArrayAlphabet<String>(*constVarSet.toTypedArray()))
    val varDFA = generateVar(size, ArrayAlphabet<String>(*constVarSet.toTypedArray()), constDFA)
    val eolDFA = generateBlankAndEOL(size, ArrayAlphabet<String>(firstArray[0]))
    val blankDFA = generateBlankAndEOL(size, ArrayAlphabet<String>(firstArray[1]))
    val equalDFA = generateEqual(size, equalSep.toTypedArray())


    //Visualization.visualize(constDFA)
    //Visualization.visualize(varDFA)
    //Visualization.visualize(DFAs.and(constDFA, varDFA,  ArrayAlphabet()))
    //Visualization.visualize(eolDFA)
    //Visualization.visualize(blankDFA)
}

fun generateEqual(size: Int, alphabet:Array<String>):CompactDFA<String>{
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
        Visualization.visualize(dfa)
        val concat1 = concatenateAutomata(dfa1, dfa)
        Visualization.visualize(concat1)
        return dfa
    } else {
        var dfa = generateFiniteAutomata(size, ArrayAlphabet<String>(*alphabet))
        return dfa
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
        if(DFAs.and(varAutomata, constDFA, ArrayAlphabet()).size() == 1){
            return varAutomata
        }
    }
}
