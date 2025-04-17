package ru.nikita.heritage

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HeritageApplication

/**
 * Запускает твоё охуенное приложение
 */
fun main(args: Array<String>) {
	runApplication<HeritageApplication>(*args)
}
