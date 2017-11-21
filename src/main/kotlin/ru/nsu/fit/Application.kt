package ru.nsu.fit

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.auth.Authentication
import io.ktor.auth.Principal
import io.ktor.auth.authentication
import io.ktor.auth.basicAuthentication
import io.ktor.content.default
import io.ktor.content.files
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.locations.Locations
import io.ktor.locations.*
import io.ktor.pipeline.PipelineContext
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.delete
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.apache.commons.lang.Validate
import ru.nsu.fit.endpoint.rest.*
import ru.nsu.fit.endpoint.service.MainFactory
import java.util.*

const val ADMIN_LOGIN = "admin"
const val ADMIN_PASSWORD = "admin"
const val ADMIN_ROLE = "admin"
const val UNKNOWN_ROLE = "unknown"

class RolePrincipal(
        val role: String
) : Principal

fun main(args: Array<String>) {
    val server = embeddedServer(Netty, 8080) {
        install(DefaultHeaders)
        install(CallLogging)
        install(StatusPages)
        install(Locations)
        install(ContentNegotiation) {
            jackson {
                configure(SerializationFeature.INDENT_OUTPUT, true)
            }
        }
        install(Authentication) {
            basicAuthentication("Basic") { credentials ->
                if(credentials.name == ADMIN_LOGIN && credentials.password == ADMIN_PASSWORD)
                    RolePrincipal(ADMIN_ROLE)
                else
                    RolePrincipal(UNKNOWN_ROLE)
            }
        }
        routing {
            route("endpoint/rest") {
                get<health_check> {
                    call.respondText { "{\"status\": \"OK\"}" }
                }
                get<get_role> {
                    val rolePrincipal = call.authentication.principal as RolePrincipal
                    call.respondText { "{\"role\": \"${rolePrincipal.role}\"}" }
                }
                get<customers> {
                    authenticateAdmin()
                    call.respond(MainFactory.getInstance().customerManager.customers)
                }
                post<create_customer> {
                    authenticateAdmin()
                    call.respond(MainFactory.getInstance().customerManager.createCustomer(call.receive()))
                }
                delete("/remove_customer/{id}") {
                    authenticateAdmin()
                    val uuid = UUID.fromString(call.parameters["id"])
                    MainFactory.getInstance().customerManager.removeCustomer(uuid)
                    call.respond(HttpStatusCode.OK)
                }
                get<get_customer_id> {
                    authenticateAdmin()
                    val customers = MainFactory
                            .getInstance()
                            .customerManager
                            .customers
                            .filter { x -> x.login == it.customer_login }
                    Validate.isTrue(customers.size == 1)
                    val id = customers.first().id
                    if (id == null) {
                        log.info("No customers found by login ${it.customer_login}")
                    }
                    call.respond(id ?: "")
                }

            }
            files("static")
            default("static/index.html")
        }

    }
    server.start(wait = true)
}

private suspend fun PipelineContext<Unit, ApplicationCall>.authenticateAdmin() {
    if ((call.authentication.principal as RolePrincipal).role != ADMIN_ROLE) {
        call.respond(HttpStatusCode.Unauthorized, "Only admin access")
    }
}

