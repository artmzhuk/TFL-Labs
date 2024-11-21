package learnerInterface

import lexems.dfaAnd
import lexems.findWordForDFA
import net.automatalib.alphabet.ArrayAlphabet
import net.automatalib.automaton.fsa.CompactDFA
import net.automatalib.automaton.fsa.CompactNFA
import net.automatalib.util.automaton.fsa.DFAs
import net.automatalib.util.automaton.fsa.NFAs
import net.automatalib.visualization.Visualization


fun checkWord(dfa: CompactDFA<String>, word: String): Boolean {
    val castedWord = if (word == "ε") "" else word
    return dfa.accepts(castedWord.map { it.toString() }.toMutableList())
}

fun checkAutomata(
    mainPrefixes: List<String>,
    nonMainPrefixes: List<String>,
    suffixes: List<String>,
    matrix: List<String>,
    initialDFA: CompactDFA<String>
): Pair<String, Boolean> {
    val alphabet = ArrayAlphabet<String>("0", "1", "2", "a", "b", "c")
    val lenOfRow = suffixes.size
    var resNFA = CompactNFA<String>(alphabet)
    val initialState = resNFA.addInitialState()
    var prefixMap = mutableMapOf<List<String>, Int>()

    for (i in mainPrefixes.indices) {
        var currentState = initialState
        var prefixSplitted = mainPrefixes[i].split("")
        if (prefixSplitted.size > 0) {
            prefixSplitted = prefixSplitted.subList(1, prefixSplitted.size - 1)
        }

        if (mainPrefixes[i] == "ε") {
            if (matrix[i * lenOfRow] == "1") {
                resNFA.setAccepting(currentState, true)
            }
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
        var prefixSplitted = nonMainPrefixes[i].split("")
        if (prefixSplitted.size > 0) {
            prefixSplitted = prefixSplitted.subList(1, prefixSplitted.size - 1)
        }
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
    //Visualization.visualize(resDFA)
    //Visualization.visualize(initialDFA)
    //Visualization.visualize(dfaComplement)
    val initialMinusReceived = dfaAnd(initialDFA, dfaComplement)
    //Visualization.visualize(initialMinusReceived)
    if (initialMinusReceived.size() == 1 || initialMinusReceived.size() == 0) {
        val initialComplement = DFAs.complement(initialDFA, alphabet)
        val recievedMinusInitial = dfaAnd(resDFA, initialComplement)
        if (recievedMinusInitial.size() == 0 || recievedMinusInitial.size() == 1) {
            println(resDFA.size())
            return Pair<String, Boolean>("true", false)
        } else {
            val res = findWordForDFA(recievedMinusInitial)
            return  Pair<String, Boolean>(res, false)
        }
    } else {
        //Visualization.visualize(initialDFA)
        //Visualization.visualize(resDFA)
        //Visualization.visualize(initialMinusReceived)
        val res = findWordForDFA(initialMinusReceived)
        return  Pair<String, Boolean>(findWordForDFA(initialMinusReceived), true)
    }
}