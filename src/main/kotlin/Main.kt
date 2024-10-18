import net.automatalib.alphabet.Alphabet
import net.automatalib.alphabet.ArrayAlphabet
import net.automatalib.automaton.fsa.CompactDFA
import net.automatalib.util.automaton.random.RandomAutomata
import net.automatalib.visualization.Visualization
import java.util.*

fun <I> dfsDFA(dfa: CompactDFA<I>, startState: Int, visited: MutableSet<Int>, alphabet: Alphabet<I>): CompactDFA<I> {
    val stack = Stack<Int>()
    val color = mutableMapOf<Int, String>()
    val predecessors = mutableMapOf<Pair<Int, I>, Int>()

    stack.push(startState)
    color[startState] = "grey"

    while (stack.isNotEmpty()) {
        val currentState = stack.pop()
        println("Visiting: $currentState")

        for (input in alphabet) {
            val nextState = dfa.getSuccessor(currentState, input)
            val trans = dfa.getTransition(currentState, input)

            if (nextState != null) {
                if (color[nextState] == "grey") {
                    println(" $currentState to $nextState on  $input")
                    dfa.removeTransition(currentState, input, trans)
                } else if (color[nextState].isNullOrEmpty()) {
                    visited.add(nextState)
                    stack.push(nextState)
                    color[nextState] = "grey"
                    predecessors[nextState to input] = currentState
                }
            }
        }

        color[currentState] = "black"
    }

    println("completed")
    return dfa
}

fun <I> dfs2(dfa: CompactDFA<I>, currentState: Int, color: Array<String>) {
    color[currentState] = "grey"
    var numOfTransitions = 0
    for (input in dfa.inputAlphabet) {
        val nextState = dfa.getSuccessor(currentState, input)
        val trans = dfa.getTransition(currentState, input)
        if (nextState != null) {
            numOfTransitions++
            if (color[nextState] == "white") {
                dfs2(dfa, nextState, color)
            }
            if (color[nextState] == "grey") {
                dfa.removeTransition(currentState, input, trans)
                numOfTransitions--
            }
        }
    }

    if (numOfTransitions == 0) {
        println(currentState)
        Visualization.visualize(dfa)
        dfa.setAccepting(currentState, true)
    }
    color[currentState] = "black"
}


fun main(args: Array<String>) {
    val alphabet = ArrayAlphabet("a", "b", "c", "0", "1", "2")
    val size = 4
    val random = Random()
    val dfa: CompactDFA<String> = RandomAutomata.randomICDFA(random, size, alphabet, true)

    val startState = dfa.initialState

    if (startState != null) {
        //dfsDFA(dfa, startState, visited, alphabet)
        //Visualization.visualize(dfa)
        var colors = Array<String>(size) { "white" }
        dfs2(dfa, startState, colors)
        Visualization.visualize(dfa)
    } else {
        println("Автомат не имеет начального состояния")
    }

}
