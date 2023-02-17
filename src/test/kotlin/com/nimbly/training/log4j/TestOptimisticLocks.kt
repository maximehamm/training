package com.nimbly.training.log4j

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.sql.DriverManager

@Suppress("SqlNoDataSourceInspection", "SqlResolve")
class TestOptimisticLocks {

    /**
     * Single use : no problem !
     */
    @Test
    fun insert1Alone() {

        assertInvoiceAmount("F001", 1000.0)

        val con = startConnection();
        con.createStatement().executeUpdate("""
                UPDATE Invoice
                SET amount = 2000.0
                WHERE id = 'F001'"""
        )
        con.commit()

        assertInvoiceAmount("F001", 2000.0)
    }

    /**
     * Two user together : oups !
     */
    @Test
    fun insert2NoConcurrencyMecanism() {

        suspend fun increaseInvoice(who: String, invoiceId: String, addAmount: Double, delay1: Long, delay2: Long) {

            delay(delay1)
            val con = startConnection(autoCommit = false)
            val amount = getInvoiceAmout(invoiceId, con)
            println("# $who Starts connection : amount = $amount")
            delay(delay2)

            val newAmount = amount + addAmount
            println("# $who Sets amount to $newAmount")
            con.createStatement().executeUpdate("""
                    UPDATE Invoice
                    SET amount = $newAmount
                    WHERE id = '$invoiceId'""")
            con.commit()

            println("# $who Commit OK")
        }

        assertInvoiceAmount("F001", 1000.0)

        runBlocking {

            launch {
                increaseInvoice(
                    who = "JOE",
                    invoiceId = "F001",
                    addAmount = 6666.0,
                    delay1 = 0, delay2 = 1000
                )
            }

            launch {
                increaseInvoice(
                    who = "WILLY",
                    invoiceId = "F001",
                    addAmount = 9999.0,
                    delay1 = 500, delay2 = 0
                )
            }
        }

        val amount = getInvoiceAmout("F001")
        println("# Finally : amount = $amount !!!")

        //# JOE Starts connection : amount = 1000.0
        //# WILLY Starts connection : amount = 1000.0
        //# WILLY Sets amount to 9999.0
        //# WILLY Commit OK
        //# JOE Sets amount to 6666.0
        //# JOE Commit OK
        //# Finally : amount = 7666.0 !!!

        assertInvoiceAmount("F001", 7666.0)
    }

    @Test
    fun insert3OptimisticsLock() {

        class OptimistLockException : Exception()

        suspend fun increaseInvoice(who: String, invoiceId: String, addAmount: Double, delay1: Long, delay2: Long) {

            var con: Connection? = null
            try {

                delay(delay1)
                val amount = getInvoiceAmout(invoiceId)
                val version = getInvoiceVersion(invoiceId)
                println("# $who Starts connection : amount = $amount, version = $version")
                delay(delay2)

                val newAmount = amount + addAmount
                println("# $who Sets amount to $newAmount")
                con = startConnection(autoCommit = false)
                val rows = con.createStatement().executeUpdate("""
                        UPDATE Invoice
                        SET amount = $newAmount, version = version + 1
                        WHERE id = '$invoiceId'
                        AND version = $version
                        """)

                if (rows == 0)
                    throw OptimistLockException()

                con.commit()
                println("# $who Commit OK")
            }
            catch (e: OptimistLockException) {
                con?.rollback()
                println("# $who Rollback done")
                println("# $who LOSTS !!!")
            }
        }

        assertInvoiceAmount("F001", 1000.0)
        assertInvoiceVersion("F001", 1)

        runBlocking {

            launch {
                increaseInvoice(
                    who = "JOE",
                    invoiceId = "F001",
                    addAmount = 6666.0,
                    delay1 = 0, delay2 = 1000
                )
            }

            launch {
                increaseInvoice(
                    who = "WILLY",
                    invoiceId = "F001",
                    addAmount = 9999.0,
                    delay1 = 500, delay2 = 0
                )
            }
        }

        val amount = getInvoiceAmout("F001")
        println("# Finally amount is $amount ! JOE SHOULD RETRY")
        println("# JOE SHOULD RETRY")

        //# JOE Starts connection : amount = 1000.0, version = 1
        //# WILLY Starts connection : amount = 1000.0, version = 1
        //# WILLY Sets amount to 10999.0
        //# WILLY Commit OK
        //# JOE Sets amount to 7666.0
        //# JOE LOSTS !!!
        //# JOE Rollback OK
        //# Finally amount is 10999.0 ! JOE SHOULD RETRY
        //# JOE SHOULD RETRY

        assertInvoiceAmount("F001", 10999.0)
        assertInvoiceVersion("F001", 2)

    }

    @BeforeEach
    fun initDB() {

        val con = startConnection(autoCommit = false);
        try {
            con.createStatement().executeUpdate(
                "DROP TABLE Invoice"
            )
        } catch (ignored: Exception) {
        }

        con.createStatement().executeUpdate("""
               CREATE TABLE Invoice (
                   id VARCHAR(32),
                   amount DECIMAL NOT NULL,
                   version INTEGER,
                   PRIMARY KEY (id))"""
        )
        con.createStatement().executeUpdate("""
            INSERT INTO Invoice (id, amount, version)
            VALUES ('F001', 1000.0, 1);"""
        )
        con.commit()
        assertInvoicesCount(1)
    }




    private fun startConnection(autoCommit : Boolean = true) : Connection {
        val con = DriverManager.getConnection("jdbc:hsqldb:file:target/tests/testdb", "SA", "");
        con.autoCommit = autoCommit;
        return con
    }

    private fun getInvoiceAmout(invoiceId: String, con: Connection = startConnection()): Double {
        val stmt = con.createStatement()
        val r = stmt.executeQuery("SELECT amount FROM Invoice WHERE id = '$invoiceId'")
        r.next()
        return r.getBigDecimal("amount").toDouble()
    }

    private fun getInvoiceVersion(invoiceId: String, con: Connection = startConnection()): Int {
        val stmt = con.createStatement()
        val r = stmt.executeQuery("SELECT version FROM Invoice WHERE id = '$invoiceId'")
        r.next()
        return r.getInt("version")
    }

    private fun assertInvoiceAmount(invoiceId: String, amount: Double, con: Connection = startConnection(),) {
        assertEquals(amount, getInvoiceAmout(invoiceId, con))
    }

    private fun assertInvoiceVersion(invoiceId: String, verson: Int, con: Connection = startConnection(), ) {
        assertEquals(verson, getInvoiceVersion(invoiceId, con))
    }

    private fun assertInvoicesCount(count: Int, con: Connection = startConnection()) {
        val stmt = con.createStatement()
        val r = stmt.executeQuery("SELECT count(*) as count from Invoice;")
        r.next()
        assertEquals(count, r.getInt("count"))
    }
}