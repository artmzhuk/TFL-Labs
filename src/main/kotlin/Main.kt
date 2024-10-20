import dk.brics.automaton.Automaton
import dk.brics.automaton.RegExp
import lexems.concatenateAutomata
import lexems.convertDfaToNfa
import lexems.generateAlphabets
import lexems.kleeneStar
import net.automatalib.alphabet.Alphabet
import net.automatalib.alphabet.ArrayAlphabet
import net.automatalib.automaton.fsa.CompactDFA
import net.automatalib.util.automaton.fsa.DFAs
import net.automatalib.visualization.Visualization

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

fun test1():CompactDFA<String>{
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

fun main(args: Array<String>) {
    val alphabet = ArrayAlphabet("a", "b", "c", "0", "1", "2")
/*    val test1 = regex("a*b")
    val test2 = regex("c*d")
    Visualization.visualize(test1)
    Visualization.visualize(test2)
    val res = CompactDFA<Char>(ArrayAlphabet('a', 'b', 'c', 'd'))
    net.automatalib.util.automaton.fsa.DFAs.combine( test1, test2, ArrayAlphabet('a', 'b', 'c', 'd'), res)
    Visualization.visualize(res)*/
    //test1()
    val size = 30
    val nesting = 10
/*
    for(i in 0..100){
        lexems.generateLexems(size, nesting)
        println(i)
    }
*/
    lexems.generateLexems(size, nesting)

    //val dfa = lexems.generateFiniteAutomata(size, alphabet)
    //generateAlphabets()

    //val dfa = test(alphabet)
    //Visualization.visualize( kleeneStar(dfa))
    //Visualization.visualize(convertDfaToNfa(dfa))

/*    val dfa2 = dfa
    Visualization.visualize(dfa)
    val concat = concatenateAutomata(dfa, dfa2, alphabet)
    Visualization.visualize(concat)
    Visualization.visualize(DFAs.and(concat, dfa, alphabet))*/
}
