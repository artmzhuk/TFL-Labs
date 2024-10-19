import dk.brics.automaton.Automaton
import dk.brics.automaton.RegExp
import net.automatalib.alphabet.Alphabet
import net.automatalib.alphabet.ArrayAlphabet
import net.automatalib.automaton.fsa.CompactDFA
import net.automatalib.automaton.fsa.CompactNFA
import net.automatalib.util.automaton.fsa.*
import net.automatalib.util.automaton.random.RandomAutomata
import net.automatalib.visualization.Visualization
import java.util.*

fun concatenateAutomata(dfa1: CompactDFA<String>, dfa2: CompactDFA<String>, alphabet: Alphabet<String>): CompactDFA<String> {
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
            val nextState = dfa1.getSuccessor(state, input)
            if (nextState != -1) {
                resultNFA.addTransition(stateMapping1[state]!!, input, stateMapping1[nextState]!!)
            }
        }
    }

    for (state in dfa1.states) {
        if (dfa1.isAccepting(state)) {
            resultNFA.setAccepting(stateMapping1[state]!!, false) // Сделать не финальным, т.к. продолжается в dfa2
            for (input in alphabet) {
                val nextState = dfa2.getSuccessor(dfa2.initialState, input)
                if (nextState != null) {
                    resultNFA.addTransition(stateMapping1[state]!!, input, stateMapping2[nextState]!!)
                }
            }
        }
    }
    for (state in dfa2.states) {
        for (input in alphabet) {
            val nextState = dfa2.getSuccessor(state, input)
            if (nextState != -1) {
                resultNFA.addTransition(stateMapping2[state]!!, input, stateMapping2[nextState]!!)
            }
        }
    }
    val resultDFA = CompactDFA<String>(alphabet)
    NFAs.determinize(resultNFA, alphabet, resultDFA)
    return DFAs.minimize(resultDFA)
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
        println(currentState)
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
    return dfa
}

fun regex(regex:String):CompactDFA<Char>{
    val bricsAutomaton: Automaton = RegExp(regex).toAutomaton()

    val alphabet: Alphabet<Char> = ArrayAlphabet<Char>('a', 'b', 'c', 'd')
    val dfa = CompactDFA(alphabet)

    var map = mutableMapOf<dk.brics.automaton.State, Int>()
    for (state in bricsAutomaton.states) {
        val sourceState = dfa.addState(state.isAccept)
        map[state] = sourceState
    }
    dfa.initialState = map[bricsAutomaton.initialState]
    for (state in bricsAutomaton.states){
        for (trans in state.transitions) {
            dfa.addTransition(map[state], trans.min, map[trans.dest])
        }
    }
    println("DFA created from regex: $regex")
    return dfa
}

fun test(alphabet: Alphabet<String>):CompactDFA<String>{
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

fun main(args: Array<String>) {
    val alphabet = ArrayAlphabet("a", "b", "c", "0", "1", "2")
/*    val test1 = regex("a*b")
    val test2 = regex("c*d")
    Visualization.visualize(test1)
    Visualization.visualize(test2)
    val res = CompactDFA<Char>(ArrayAlphabet('a', 'b', 'c', 'd'))
    net.automatalib.util.automaton.fsa.DFAs.combine( test1, test2, ArrayAlphabet('a', 'b', 'c', 'd'), res)
    Visualization.visualize(res)*/
    val size = 4
    val nesting = 10
    //val dfa = generateFiniteAutomata(size, alphabet)
    val dfa = test(alphabet)
    val dfa2 = dfa
    Visualization.visualize(dfa)
    Visualization.visualize(concatenateAutomata(dfa, dfa2, alphabet))
}
