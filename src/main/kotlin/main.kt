import java.security.MessageDigest

private const val DIFFICULTY = 5

fun main() {

    val blockchain = mutableListOf(
        Block(
            index = 0,
            timestamp = 1759452093897,
            data = "Genesis Block",
            previousHash = "0".repeat(64),
            difficulty = 1,
            nonce = 2,
        ).apply {
            println("block: $index")
            println("timestamp: $timestamp")
            println("hash: $hash")
            println("nonce: $nonce")
        }
    )

    while (true) {
        var nonce = 0
        var block: Block

        do {
            block = Block(
                index = blockchain.size,
                timestamp = System.currentTimeMillis(),
                data = "Block ${blockchain.size}",
                previousHash = blockchain.last().hash,
                difficulty = DIFFICULTY,
                nonce = nonce++,
            )
        } while (!block.valid)

        blockchain.add(block)

        println("-".repeat(70))
        println("block: ${block.index}")
        println("timestamp: ${block.timestamp}")
        println("prev: ${block.previousHash}")
        println("hash: ${block.hash}")
        println("nonce: ${block.nonce}")
    }
}

data class Block(
    val index: Int,
    val timestamp: Long,
    val data: String,
    val previousHash: String,
    val difficulty: Int = 0,
    val nonce: Int = 0
) {
    val hash = toString().sha256()
    val valid = hash.startsWith("0".repeat(difficulty))
}

fun String.sha256(): String {
    val bytes = toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    return digest.joinToString("") { "%02x".format(it) }
}