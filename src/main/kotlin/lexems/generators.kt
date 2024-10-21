package lexems

import net.automatalib.alphabet.Alphabet
import net.automatalib.alphabet.ArrayAlphabet
import net.automatalib.automaton.fsa.CompactDFA
import net.automatalib.automaton.fsa.CompactNFA
import net.automatalib.util.automaton.fsa.DFAs
import net.automatalib.util.automaton.fsa.NFAs
import net.automatalib.util.automaton.random.RandomAutomata
import net.automatalib.visualization.Visualization
import java.util.*

fun dfaAnd(dfa1: CompactDFA<String>, dfa2: CompactDFA<String>): CompactDFA<String> {
    var alphabetSet = dfa1.inputAlphabet.toTypedArray().toSet()
    alphabetSet = alphabetSet.intersect(dfa2.inputAlphabet.toTypedArray().toSet())
    val alphabet = ArrayAlphabet<String>(*alphabetSet.toTypedArray())
    val dfa = DFAs.and(dfa1, dfa2, alphabet)
    var hasAccepting = false
    for (state in dfa.states) {
        if (dfa.isAccepting(state)) {
            hasAccepting = true
            break
        }
    }
    if (hasAccepting) {
        return dfa
    }
    val res = CompactDFA<String>(ArrayAlphabet<String>(*alphabetSet.toTypedArray()))
    res.addState()
    res.initialState = 0
    return res
}

fun dfaUnion(dfa1: CompactDFA<String>, dfa2: CompactDFA<String>): CompactDFA<String> {
    var alphabetSet = dfa1.inputAlphabet.toTypedArray().toSet()
    alphabetSet = alphabetSet.plus(dfa2.inputAlphabet.toTypedArray().toSet())
    val alphabet = ArrayAlphabet<String>(*alphabetSet.toTypedArray())
    val newNFA = CompactNFA<String>(alphabet)
    val stateMapping1 = mutableMapOf<Int, Int>()
    val stateMapping2 = mutableMapOf<Int, Int>()
    for (state in dfa1.states) {
        val newState = newNFA.addState(dfa1.isAccepting(state))
        stateMapping1[state] = newState
    }
    for (state in dfa1.states) {
        for (input in dfa1.inputAlphabet) {
            val nextState = dfa1.getSuccessor(state, input)
            if (nextState != -1) {
                newNFA.addTransition(stateMapping1[state]!!, input, stateMapping1[nextState]!!)
            }
        }
    }
    for (state in dfa2.states) {
        val newState = newNFA.addState(dfa2.isAccepting(state))
        stateMapping2[state] = newState
    }
    for (state in dfa2.states) {
        for (input in dfa2.inputAlphabet) {
            val nextState = dfa2.getSuccessor(state, input)
            if (nextState != -1) {
                newNFA.addTransition(stateMapping2[state]!!, input, stateMapping2[nextState]!!)
            }
        }
    }
    val newInitial = newNFA.addInitialState()
    addEpsilonTransition(newNFA, newInitial, stateMapping1[dfa1.initialState]!!)
    addEpsilonTransition(newNFA, newInitial, stateMapping2[dfa2.initialState]!!)
    val resultDFA = CompactDFA<String>(alphabet)
    NFAs.determinize(newNFA, alphabet, resultDFA, true, true)
    //Visualization.visualize(dfa1)
    //Visualization.visualize(dfa2)
    //Visualization.visualize(resultDFA)
    //dk.brics.automaton.BasicOperations.union()
    return resultDFA
}

fun copyWithNewAlphabet(dfa: CompactDFA<String>, newAlphabet: Alphabet<String>): CompactDFA<String> {
    val newDFA = CompactDFA<String>(newAlphabet)
    val stateMapping1 = mutableMapOf<Int, Int>()
    for (state in dfa.states) {
        val newState = newDFA.addState(dfa.isAccepting(state))
        stateMapping1[state] = newState
    }
    for (state in dfa.states) {
        for (input in dfa.inputAlphabet) {
            val nextState = dfa.getSuccessor(state, input)
            if (nextState != -1) {
                newDFA.addTransition(stateMapping1[state]!!, input, stateMapping1[nextState]!!)
            }
        }
    }
    return newDFA
}

fun concatenateAutomataWithoutDeterminisation(dfa1: CompactDFA<String>, dfa2: CompactDFA<String>): CompactNFA<String> {
    var alphabetSet = dfa1.inputAlphabet.toTypedArray().toSet()
    alphabetSet = alphabetSet.plus(dfa2.inputAlphabet.toTypedArray())
    val alphabet = ArrayAlphabet<String>(*alphabetSet.toTypedArray())
    val resultNFA = CompactNFA(alphabet)

    val stateMapping1 = mutableMapOf<Int, Int>()
    val stateMapping2 = mutableMapOf<Int, Int>()

    for (state in dfa1.states) {
        val newState = resultNFA.addState(dfa1.isAccepting(state))
        stateMapping1[state] = newState
    }

    for (state in dfa2.states) {
        val newState = resultNFA.addState(dfa2.isAccepting(state))
        stateMapping2[state] = newState
    }

    resultNFA.setInitial(stateMapping1[dfa1.initialState], true)
    //resultDFA.initialState = stateMapping1[dfa1.initialState]

    for (state in dfa1.states) {
        for (input in alphabet) {
            if (dfa1.inputAlphabet.containsSymbol(input)) {
                val nextState = dfa1.getSuccessor(state, input)
                if (nextState != -1) {
                    resultNFA.addTransition(stateMapping1[state]!!, input, stateMapping1[nextState]!!)
                }
            }
        }
    }

    for (state in dfa1.states) {
        if (dfa1.isAccepting(state)) {
            resultNFA.setAccepting(stateMapping1[state]!!, false) // Сделать не финальным, т.к. продолжается в dfa2
            for (input in alphabet) {
                if (dfa2.inputAlphabet.containsSymbol(input)) {
                    val nextState = dfa2.getSuccessor(dfa2.initialState, input)
                    if (nextState != null) {
                        resultNFA.addTransition(stateMapping1[state]!!, input, stateMapping2[nextState]!!)
                    }
                }
            }
        }
    }
    for (state in dfa2.states) {
        for (input in alphabet) {
            if (dfa2.inputAlphabet.containsSymbol(input)) {
                val nextState = dfa2.getSuccessor(state, input)
                if (nextState != -1) {
                    resultNFA.addTransition(stateMapping2[state]!!, input, stateMapping2[nextState]!!)
                }
            }
        }
    }
    return resultNFA

}

fun concatenateAutomata(dfa1: CompactDFA<String>, dfa2: CompactDFA<String>): CompactDFA<String> {
    var alphabetSet = dfa1.inputAlphabet.toTypedArray().toSet()
    alphabetSet = alphabetSet.plus(dfa2.inputAlphabet.toTypedArray())
    val alphabet = ArrayAlphabet<String>(*alphabetSet.toTypedArray())
    val resultNFA = concatenateAutomataWithoutDeterminisation(dfa1, dfa2)
    val resultDFA = CompactDFA<String>(alphabet)
    NFAs.determinize(resultNFA, alphabet, resultDFA, true, true)
    return resultDFA
}


    fun <I> removeCycles(dfa: CompactDFA<I>, currentState: Int, color: Array<String>) {
    color[currentState] = "grey"
    var numOfTransitions = 0
    for (input in dfa.inputAlphabet) {
        val nextState = dfa.getSuccessor(currentState, input)
        val trans = dfa.getTransition(currentState, input)
        if (nextState != -1) {
            numOfTransitions++
            if (color[nextState] == "white") {
                removeCycles(dfa, nextState, color)
            }
            if (color[nextState] == "grey") {
                dfa.removeTransition(currentState, input, trans)
                numOfTransitions--
            }
        }
    }
    if (numOfTransitions == 0) {
        //println(currentState)
        //Visualization.visualize(dfa)
        dfa.setAccepting(currentState, true)
    }
    color[currentState] = "black"
}

fun generateFiniteAutomata(size: Int, alphabet: Alphabet<String>): CompactDFA<String> {
    //TODO rename
    var dfa: CompactDFA<String>
    while (true) {
        dfa = RandomAutomata.randomICDFA(Random(), size, alphabet, true)
        if (dfa.size() > 1 && dfa.initialState != null) {
            break
        }
    }
    var colors = Array<String>(size) { "white" }
    removeCycles(dfa, dfa.initialState!!, colors)
    dfa.setAccepting(dfa.initialState, false)
    return dfa
}

fun generateInfiniteAutomata(size: Int, alphabet: Alphabet<String>): CompactDFA<String> {
    //TODO rename
    var dfa: CompactDFA<String>
    while (true) {
        dfa = RandomAutomata.randomICDFA(Random(), size, alphabet, true)
        if (dfa.size() > 1 && dfa.initialState != null) {
            break
        }
    }
    for (state in dfa.states) {
        if (state != dfa.initialState && dfa.isAccepting(state)) {
            dfa.setAccepting(dfa.initialState, false)
            break
        }
    }
    return dfa
}

fun dfsForEqual(dfa: CompactDFA<String>, currentState: Int, visited: Array<Boolean>, toChange: String): Boolean {
    visited[currentState] = true
    var res = true
    for (input in dfa.inputAlphabet) {
        val nextState = dfa.getSuccessor(currentState, input)
        val trans = dfa.getTransition(currentState, input)
        if (nextState != -1) {
            res = false
            if (true) {
                val needToChange = dfsForEqual(dfa, nextState, visited, toChange)
                if (needToChange) {
                    dfa.removeTransition(currentState, input, nextState)
                    if (dfa.getTransition(currentState, toChange) == null) {
                        dfa.addTransition(currentState, toChange, nextState)
                    }
                }
            }
        }
    }
    return res
}

fun <I> convertDfaToNfa(dfa: CompactDFA<I>): CompactNFA<I> {
    val alphabet: Alphabet<I> = dfa.inputAlphabet

    val nfa = CompactNFA<I>(alphabet)

    val stateMapping = mutableMapOf<Int, Int>()
    for (state in dfa.states) {
        val newState = nfa.addState(dfa.isAccepting(state))  // принимающее состояние
        stateMapping[state] = newState
    }

    for (state in dfa.states) {
        for (input in alphabet) {
            val successor = dfa.getSuccessor(state, input)
            if (successor != -1) {
                nfa.addTransition(stateMapping[state]!!, input, stateMapping[successor]!!)
            }
        }
    }
    nfa.setInitial(dfa.initialState, true)
    return nfa
}

fun addEpsilonTransition(automata: CompactNFA<String>, from: Int, to: Int) {
    if (automata.isAccepting(to)) {
        automata.setAccepting(from, true)
    }
    for (input in automata.inputAlphabet) {
        val nextStates = automata.getTransitions(to, input)
        if (nextStates != null) {
            for (next in nextStates) {
                automata.addTransition(from, input, next)
            }
        }
    }
}

fun kleeneStar(dfa: CompactDFA<String>): CompactDFA<String> {
    var nfa = convertDfaToNfa(dfa)
    val oldInitial = nfa.initialStates.iterator().next()!!
    val newInitial = nfa.addState()
    nfa.setInitial(newInitial, true)
    nfa.setInitial(oldInitial, false)
    nfa.setAccepting(newInitial, true)
    addEpsilonTransition(nfa, newInitial, oldInitial)
    //Visualization.visualize(nfa)
    for (state in nfa.states) {
        if (state != newInitial && nfa.isAccepting(state)) {
            addEpsilonTransition(nfa, state, newInitial)
        }
    }


    //Visualization.visualize(nfa)
    val resultDFA = CompactDFA<String>(nfa.inputAlphabet)
    NFAs.determinize(nfa, nfa.inputAlphabet, resultDFA)
    return DFAs.minimize(resultDFA)
}

fun kleeneOneOrMore(dfa: CompactDFA<String>): CompactDFA<String> {
    val kleene = kleeneStar(dfa)
    val res = concatenateAutomata(dfa, kleene)
    return res
}