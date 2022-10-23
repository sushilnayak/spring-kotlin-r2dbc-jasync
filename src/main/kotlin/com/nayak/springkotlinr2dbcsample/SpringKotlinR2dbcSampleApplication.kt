package com.nayak.springkotlinr2dbcsample

import com.nayak.springkotlinr2dbcsample.ConnectionProperty.MYSQL_DRIVER
import io.r2dbc.pool.PoolingConnectionFactoryProvider.*
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions.*
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Duration

@EnableAsync
@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties(R2dbcPoolProperties::class, R2dbcProperties::class)
class SpringKotlinR2dbcSampleApplication

fun main(args: Array<String>) {
    runApplication<SpringKotlinR2dbcSampleApplication>(*args)
}

@Service
class RandomService(val databaseClient: DatabaseClient) {
    @Scheduled(fixedDelay = 1000, initialDelay = 1000)
    @Async
    fun firstScheduledQuery() {

        val mutableMap = databaseClient.sql("select 1").fetch().first().block()

        println("Query result => ${mutableMap?.values}")
    }
}


object ConnectionProperty {
    const val HOST = "host"
    const val USERNAME = "username"
    const val PASSWORD = "password"
    const val PORT = "port"
    const val DB = "db"
    const val MYSQL_DRIVER = "mysql"
}

@ConstructorBinding
@ConfigurationProperties(prefix = "r2dbc.pool")
data class R2dbcPoolProperties(
    val initialSize: Int,
    val maxSize: Int,
    val maxLife: Long,
    val maxCreateConnectionTime: Long,
    val maxIdleTime: Long,
)

@ConstructorBinding
@ConfigurationProperties(prefix = "r2dbc")
data class R2dbcProperties(
    val host: String,
    val port: Int,
    val db: String,
    val username: String,
    val password: String,
)

@Configuration
class R2dbcConfig(
    val r2dbcProperties: R2dbcProperties,
    val r2dbcPoolProperties: R2dbcPoolProperties
) {

    @Bean
    fun databaseClient(connectionFactory: ConnectionFactory): DatabaseClient {
        return DatabaseClient.builder().connectionFactory(connectionFactory).build();
    }

    @Bean
    fun connectionFactory() =
        makeConnectionFactory(
            hashMapOf(
                ConnectionProperty.HOST to r2dbcProperties.host,
                ConnectionProperty.USERNAME to r2dbcProperties.username,
                ConnectionProperty.PASSWORD to r2dbcProperties.password,
                ConnectionProperty.PORT to r2dbcProperties.port.toString(),
                ConnectionProperty.DB to r2dbcProperties.db,
            )
        )

    fun makeConnectionFactory(connectionProperties: Map<String, String>): ConnectionFactory =
        ConnectionFactories.get(
            builder()
                .option(DRIVER, POOLING_DRIVER)
                .option(PROTOCOL, MYSQL_DRIVER)
                .option(HOST, connectionProperties[ConnectionProperty.HOST]!!)
                .option(USER, connectionProperties[ConnectionProperty.USERNAME]!!)
                .option(PORT, connectionProperties[ConnectionProperty.PORT]!!.toInt())
                .option(PASSWORD, connectionProperties[ConnectionProperty.PASSWORD]!!)
                .option(DATABASE, connectionProperties[ConnectionProperty.DB]!!)
                .option(MAX_SIZE, r2dbcPoolProperties.maxSize)
                .option(INITIAL_SIZE, r2dbcPoolProperties.initialSize)
                .option(MAX_IDLE_TIME, Duration.ofSeconds(r2dbcPoolProperties.maxIdleTime))
                .option(MAX_CREATE_CONNECTION_TIME, Duration.ofSeconds(r2dbcPoolProperties.maxCreateConnectionTime))
                .option(MAX_LIFE_TIME, Duration.ofMinutes(r2dbcPoolProperties.maxLife))
                .build()
        )
}