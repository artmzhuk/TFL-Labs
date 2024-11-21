package server

import org.http4k.core.*
import org.http4k.filter.DebuggingFilters
import org.http4k.format.KotlinxSerialization.auto
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Jetty
import org.http4k.server.asServer
import kotlinx.serialization.Serializable
import learnerInterface.checkAutomata
import learnerInterface.checkWord
import lexems.combineLexems
import net.automatalib.automaton.fsa.CompactDFA
import net.automatalib.visualization.Visualization

@Serializable
data class StartRequest(val maxSize: Int, val nesting: Int)

@Serializable
data class SizeResponse(val size: Int)

@Serializable
data class checkTableResp(val response: String, val type:Boolean)

@Serializable
data class StringJson(val word: String)

@Serializable
data class BooleanJson(val response: String)

@Serializable
data class TableRequest(
    val main_prefixes: String,
    val non_main_prefixes: String,
    val suffixes: String,
    val table: String
)


fun runserver() {
    lateinit var currentDFA: CompactDFA<String>

    val startRequestLens = Body.auto<StartRequest>().toLens()
    val sizeResponseLens = Body.auto<SizeResponse>().toLens()
    val stringJsonLens = Body.auto<StringJson>().toLens()
    val checkTableRespJsonLens = Body.auto<checkTableResp>().toLens()
    val booleanJsonLens = Body.auto<BooleanJson>().toLens()
    val TableRequestLens = Body.auto<TableRequest>().toLens()


    val app: HttpHandler = routes(
        "/start" bind Method.POST to { request ->
            val startRequest = startRequestLens(request)

            val lexems = lexems.generateLexems(startRequest.maxSize, startRequest.nesting)
            currentDFA = combineLexems(startRequest.maxSize, startRequest.nesting, lexems)
            //println(currentDFA.size())
            //Visualization.visualize(currentDFA)

            Response(Status.OK)
        },

        "/checkWord" bind Method.POST to { request ->
            val word = stringJsonLens(request)
            val res = checkWord(currentDFA, word.word)
            var strRes = "0"
            if (res) {
                strRes = "1"
            }
            Response(Status.OK).with(booleanJsonLens of BooleanJson(strRes))
        },

        "/checkTable" bind Method.POST to { request ->
            val table = TableRequestLens(request)
            val res = checkAutomata(
                table.main_prefixes.split(" "),
                table.non_main_prefixes.split(" "),
                table.suffixes.split(" "),
                table.table.split(" "),
                currentDFA
            )
            Response(Status.OK).with(checkTableRespJsonLens of checkTableResp(res.first, res.second))
        },

        "/getSize" bind Method.GET to { request ->
            val sizeResponse = SizeResponse(currentDFA.size())

            // Respond with JSON
            Response(Status.OK).with(sizeResponseLens of sizeResponse)
        },


        )

    val server = app
        .asServer(Jetty(8080))
        .start()

    println("Server started on port 8080")
    server.block()
}