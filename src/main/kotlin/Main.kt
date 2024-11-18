

import com.sun.jdi.IntegerType

enum class Category { ROUPA, ELETRONICO, COLECIONAVEL }

enum class ClothingType { CAMISA, MOLETON, ACESSORIO }

enum class Size { PP, P, M, G, GG, XG, XXG }

enum class ElectronicType { VIDEOGAME, JOGO, PORTATIL, OUTROS }

enum class CollectibleType { LIVRO, BONECO, OUTROS }

enum class FabricationMaterial { PAPEL, PLASTICO, ACO, MISTURADO, OUTROS }

enum class Relevance { COMUM, MEDIO, RARO, RARISSIMO }

open class Product (
    var code: String,
    val name: String,
    val costPrice: Double,
    val salePrice: Double,
    val category: String
)

class Clothing (
    code: String,
    name: String,
    costPrice: Double,
    salePrice: Double,
    category: String,
    val type: ClothingType,
    val size: Size,
    val primaryColor: String,
    val secundaryColor: String?
) : Product(code, name, costPrice, salePrice, category) {
    init {
        this.code = "R-" + this.code
    }
}

class Electronic (
    code: String,
    name: String,
    costPrice: Double,
    salePrice: Double,
    category: String,
    val type: ElectronicType,
    val version: String,
    val fabricationYear: String
) : Product(code, name, costPrice, salePrice, category) {
    init {
        this.code = "E-" + this.code
    }
}

class Collectible (
    code: String,
    name: String,
    costPrice: Double,
    salePrice: Double,
    category: String,
    val type: CollectibleType,
    val size: String?,
    val fabricationMaterial: FabricationMaterial,
    val relevance: Relevance
) : Product(code, name, costPrice, salePrice, category) {
    init {
        this.code = "C-" + this.code
    }
}

fun main() {
    val compras = listOf(
        listOf("CAMYO", 13, "Camisa mestre yoda", 22.35, 41.50, "roupa", "camisa", "GG", "preto", "vermelho", null, null, null, null),
        listOf("CANMAR", 20, "Caneca do Mario Bros", 12.57, 25.00, "colecionavel", "outros", null, null, null, null, null, "outros", "comum"),
        listOf("PLAY5", 2, "Playstation 5", 4000.0, 4500.0, "eletronico", "video-game", null, null, null, "1", "2022", null, null),
        listOf("GOKU1", 5, "Action figure Goku", 35.33, 70.0, "colecionavel", "boneco", "15", null, null, null, null, "plastico", "medio"),
        listOf("MARKAR", 5, "Jogo mario kart", 189.90, 250.00, "eletronico", "jogo", null, null, null, "2", "2021", null, null),
        listOf("SANE1", 15, "O senhor dos aneis 1", 35.00, 50.00, "colecionavel", "livro", null, null, null, null, null, "papel", "comum")
    )

    val vendas = listOf(
        listOf("C-CANMAR", 5),
        listOf("E-PLAY5", 1),
        listOf("C-GOKU1", 1)
    )

    // Categoria	Tipo	Tamanho	Cor primaria	Cor secundário	Versão	Ano de fabricação	Material de fabricação	Relevância
    // roupa	-	-	vermelho	-	-	-	-	-
    // -	boneco	-	-	-	-	-	-	-
    // -	video-game	-	-	-	-	2022	-	-


    val buscas: List<List<String>> = listOf(
        listOf("roupa","-","-","vermelho","-","-","-","-","-"),
        listOf("-", "boneco","-","-","-","-","-","-","-"),
        listOf("-", "videogame","-","-","-","-","2022","-","-")
    )

    val mpProducts = mutableMapOf<String, Product>()
    val mpProductsStock = mutableMapOf<String, Int>()
    val mpCategoryStock = mutableMapOf<String, Int>()
    var totalCompra: Double = 0.0
    var totalVenda: Double = 0.0

    compras.forEach {
        val code = it[0] as String
        val quantity = it[1] as Int
        val name = it[2] as String
        val costPrice = it[3] as Double
        val salePrice = it[4] as Double
        val category = it[5] as String
        val type = it[6] as String
        val size = it[7] as String?
        val primaryColor = it[8] as String?
        val secundaryColor = it[9] as String?
        val version = it[10] as String?
        val fabricationYear = it[11] as String?
        val fabricationMaterial = it[12] as String?
        val relevance = it[13] as String?

        val product = when (category.uppercase()) {
            "ROUPA" -> {
                Clothing(code,name,costPrice,salePrice,category.uppercase(),ClothingType.valueOf(type.uppercase()),Size.valueOf(size!!.uppercase()),primaryColor!!, secundaryColor)
            }
            "ELETRONICO" -> {
                Electronic(code,name,costPrice,salePrice,category.uppercase(),ElectronicType.valueOf(type.uppercase().replace("-", "")), version!!, (fabricationYear!!))
            }
            "COLECIONAVEL" -> {
                Collectible(code,name,costPrice,salePrice,category.uppercase(),CollectibleType.valueOf(type.uppercase()),size,FabricationMaterial.valueOf(fabricationMaterial!!.uppercase()),Relevance.valueOf(relevance!!.uppercase()))
            }
            else -> null
        }

        if (product != null) {
            mpProducts[product.code] = product
            mpProductsStock[product.code] = (mpProductsStock[product.code] ?: 0) + quantity
            mpCategoryStock[product.category] = (mpCategoryStock[product.category] ?: 0) + quantity
            totalCompra += product.costPrice * quantity
        }
    }

    vendas.forEach {
        val code = it[0] as String
        val quantity = it[1] as Int
        val product = mpProducts[code]

        if (product != null) {
            mpCategoryStock[product.category] = (mpCategoryStock[product.category] ?: 0) - quantity
            mpProductsStock[code] = (mpProductsStock[code] ?: 0) - quantity
            totalVenda += product.salePrice * quantity
        }
    }

    var cont = 1
    buscas.forEach { ent ->
        //Categoria	Tipo	Tamanho	Cor primaria	Cor secundário	Versão	Ano de fabricação	Material de fabricação	Relevância


        var productList: List<Product> = mpProducts.values.toList()
        if (ent[0] != "-") {
            productList = productList.filter { it.category == ent[0].uppercase()}
        }
        if (ent[1] != "-") {
            val entrie = ent[1].uppercase().replace("-", "")

            if (entrie in ClothingType.values().map { it.name }) {
                productList = productList.filter { it is Clothing && it.type == ClothingType.valueOf(entrie) }
            }
            if (entrie in ElectronicType.values().map {it.name }) {
                productList = productList.filter { it is Electronic && it.type == ElectronicType.valueOf(entrie) }
            }
            if (entrie in CollectibleType.values().map { it.name }) {
                productList = productList.filter { it is Collectible && it.type == CollectibleType.valueOf(entrie) }
            }
        }
        if (ent[2] != "-") {
            productList = productList.filter { it is Collectible && it.size == ent[2]}
        }
        if (ent[3] != "-") {
            productList = productList.filter { it is Clothing && it.primaryColor == ent[3]}
        }
        if (ent[4] != "-") {
            productList = productList.filter { it is Clothing && it.secundaryColor == ent[4]}
        }
        if (ent[5] != "-") {
            productList = productList.filter { it is Electronic && it.version == ent[5]}
        }
        if (ent[6] != "-") {
            productList = productList.filter { it is Electronic && it.fabricationYear == ent[6] }
        }
        if (ent[7] != "-") {
            productList = productList.filter { it is Collectible && it.fabricationMaterial.name == ent[7].uppercase()}
        }
        if (ent[8] != "-") {
            productList = productList.filter { it is Collectible && it.relevance.name == ent[8].uppercase()}
        }

        val totalQuantity = productList.sumOf { mpProductsStock[it.code]!! }
        println("$cont: ${totalQuantity}")
        cont += 1
    }

    for(product in mpProducts.values) {
        println("${product.code}, ${product.name}, ${mpProductsStock[product.code]}")
    }
    for(category in Category.values()) {
        println("${category}: ${mpCategoryStock[category.name]}")
    }
    println("Compra: $totalCompra")
    println("Venda: $totalVenda")
    println("Balancete: ${totalVenda - totalCompra}")
}


