@file:Suppress("ClassName")

package ru.nsu.fit.endpoint.rest

import io.ktor.locations.location

@location("/health_check") class health_check()
@location("/get_role") class get_role()
@location("/customers") class customers()
@location("/create_customer") class create_customer()
@location("/get_customer_id/{customer_login}") class get_customer_id(val customer_login: String)