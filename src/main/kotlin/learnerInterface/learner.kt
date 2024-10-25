package learnerInterface

import lexems.dfaAnd
import lexems.findWordForDFA
import net.automatalib.alphabet.ArrayAlphabet
import net.automatalib.automaton.fsa.CompactDFA
import net.automatalib.automaton.fsa.CompactNFA
import net.automatalib.util.automaton.fsa.DFAs
import net.automatalib.util.automaton.fsa.NFAs


fun checkWord(dfa: CompactDFA<String>, word: String): Boolean {
    return dfa.accepts(word.map { it.toString() }.toMutableList())
}

fun checkAutomata(
    mainPrefixes: List<String>,
    nonMainPrefixes: List<String>,
    suffixes: List<String>,
    matrix: List<String>,
    initialDFA: CompactDFA<String>
): String {
    val alphabet = ArrayAlphabet<String>("0", "1", "2", "a", "b", "c")
    val lenOfRow = suffixes.size
    var resNFA = CompactNFA<String>(alphabet)
    val initialState = resNFA.addInitialState()
    var prefixMap = mutableMapOf<List<String>, Int>()

    for (i in mainPrefixes.indices) {
        var currentState = initialState
        val prefixSplitted = mainPrefixes[i].split("")
        if (mainPrefixes[i] == "ε") {
            //resNFA.addInitialState()
        } else {
            for (input in prefixSplitted) {
                val nextState = resNFA.getTransitions(currentState, input)
                if (nextState.isNotEmpty()) {
                    currentState = nextState.iterator().next()
                } else {
                    val newState = resNFA.addState()
                    resNFA.addTransition(currentState, input, newState)
                    currentState = newState
                }
            }
        }
        if (matrix[i * lenOfRow] == "1") {
            resNFA.setAccepting(currentState, true)
        }
        prefixMap.put(matrix.subList(i * lenOfRow, (i + 1) * lenOfRow), currentState)
    }
    for (i in nonMainPrefixes.indices) {
        var currentState = initialState
        val prefixSplitted = nonMainPrefixes[i].split("")
        val correspondingMainPrefix =
            prefixMap.get(matrix.subList((i + mainPrefixes.size) * lenOfRow, (i + 1 + mainPrefixes.size) * lenOfRow))
        if (correspondingMainPrefix == null) {
            println("something is wrong")
        } else {
            for (i in prefixSplitted.indices) {
                val nextState = resNFA.getTransitions(currentState, prefixSplitted[i])
                if (i == prefixSplitted.size - 1) {
                    resNFA.addTransition(currentState, prefixSplitted[i], correspondingMainPrefix)
                } else if (nextState.isNotEmpty()) {
                    currentState = nextState.iterator().next()
                } else {
                    val newState = resNFA.addState()
                    resNFA.addTransition(currentState, prefixSplitted[i], newState)
                    currentState = newState
                }
            }
        }
    }
    var resDFA = CompactDFA<String>(alphabet)
    NFAs.determinize(resNFA, alphabet, resDFA, true, true)
    val dfaComplement = DFAs.complement(resDFA, alphabet)
    val initialMinusReceived = dfaAnd(initialDFA, dfaComplement)
    if (initialMinusReceived.size() == 1 || initialMinusReceived.size() == 0){
        val initialComplement = DFAs.complement(initialDFA, alphabet)
        val recievedMinusInitial = dfaAnd(resDFA, initialComplement)
        if (recievedMinusInitial.size() == 0 || recievedMinusInitial.size() == 1){
            return ""
        } else {
            return findWordForDFA(recievedMinusInitial)
        }
    } else {
        return findWordForDFA(initialMinusReceived)
    }
}