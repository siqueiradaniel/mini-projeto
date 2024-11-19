

import java.io.File
import java.text.Normalizer

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

fun removeAccents(input: String): String {
    return Normalizer.normalize(input, Normalizer.Form.NFD)
        .replace("[^\\p{ASCII}]".toRegex(), "") // Remove caracteres não ASCII (acentos)
}


fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Por favor, forneça o caminho do arquivo CSV como argumento.")
        return
    }

    val inPath = args[0]
    val outPath = args[1]

    val csvBuyPath = inPath + "compras.csv"
    val csvBuyFile = File(csvBuyPath)

    val csvSalePath = inPath + "vendas.csv"
    val csvSaleFile = File(csvSalePath)

    val csvSearchPath = inPath + "busca.csv"
    val csvSearchFile = File(csvSearchPath)

    if (!csvBuyFile.exists()) {
        println("O arquivo especificado não existe: $csvBuyPath")
        return
    }
    if (!csvSaleFile.exists()) {
        println("O arquivo especificado não existe: $csvSalePath")
        return
    }

    val csvResultSearchFile = File(outPath + "resultado_busca.csv")
    val csvGeneralStockFile = File(outPath + "estoque_geral.csv")
    val csvCategorStockFile = File(outPath + "estoque_categorias.csv")
    val csvBalanceteFile = File(outPath + "balancete.csv")

    val mpProducts = mutableMapOf<String, Product>()
    val mpProductsStock = mutableMapOf<String, Int>()
    val mpCategoryStock = mutableMapOf<String, Int>()
    var totalCompra: Double = 0.0
    var totalVenda: Double = 0.0

    csvBuyFile.readLines().drop(1).forEach { line ->
        val it = line.split(",")

        val code = removeAccents(it[0]).uppercase()
        val quantity = it[1].toInt()
        val name = removeAccents(it[2]).uppercase()
        val costPrice = it[3].toDouble()
        val salePrice = it[4].toDouble()
        val category = removeAccents(it[5]).uppercase()
        val type = removeAccents(it[6]).uppercase()
        val size = it[7] as String?
        val primaryColor = it[8]?.let { removeAccents(it).uppercase() }
        val secundaryColor = it[9]?.let { removeAccents(it).uppercase() }
        val version = it[10]?.let { removeAccents(it).uppercase() }
        val fabricationYear = it[11]?.let { removeAccents(it).uppercase() }
        val fabricationMaterial = it[12]?.let { removeAccents(it).uppercase() }
        val relevance = it[13]?.let { removeAccents(it).uppercase() }

        val product = when (category) {
            "ROUPA" -> {
                Clothing(code,name,costPrice,salePrice,category,ClothingType.valueOf(type),Size.valueOf(size!!),primaryColor!!, secundaryColor)
            }
            "ELETRONICO" -> {
                Electronic(code,name,costPrice,salePrice,category,ElectronicType.valueOf(type.replace("-", "")), version!!, (fabricationYear!!))
            }
            "COLECIONAVEL" -> {
                Collectible(code,name,costPrice,salePrice,category,CollectibleType.valueOf(type),size,FabricationMaterial.valueOf(fabricationMaterial!!),Relevance.valueOf(relevance!!))
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

    csvSaleFile.readLines().drop(1).forEach { line ->
        val it = line.split(",")

        val code = removeAccents(it[0]).uppercase()
        val quantity = it[1].toInt()
        val product = mpProducts[code]

        if (product != null) {
            mpCategoryStock[product.category] = (mpCategoryStock[product.category] ?: 0) - quantity
            mpProductsStock[code] = (mpProductsStock[code] ?: 0) - quantity
            totalVenda += product.salePrice * quantity
        }
    }


    if (csvSearchFile.exists()) {
        csvResultSearchFile.printWriter().use { writer ->
            writer.println("BUSCAS, QUANTIDADE")

            var cont = 1
            csvSearchFile.readLines().drop(1).forEach { line ->
                val ent = line.split(",")

                // Initialize the product list
                var productList: List<Product> = mpProducts.values.toList()

                // Apply filters based on the entries
                if (ent[0] != "-") {
                    productList = productList.filter { removeAccents(it.category).uppercase() == removeAccents(ent[0]).uppercase() }
                }
                if (ent[1] != "-") {
                    val entrie = removeAccents(ent[1]).uppercase().replace("-", "")
                    if (entrie in ClothingType.values().map { it.name }) {
                        productList = productList.filter { it is Clothing && it.type == ClothingType.valueOf(entrie) }
                    }
                    if (entrie in ElectronicType.values().map { it.name }) {
                        productList =
                            productList.filter { it is Electronic && it.type == ElectronicType.valueOf(entrie) }
                    }
                    if (entrie in CollectibleType.values().map { it.name }) {
                        productList =
                            productList.filter { it is Collectible && it.type == CollectibleType.valueOf(entrie) }
                    }
                }
                if (ent[2] != "-") {
                    productList = productList.filter { it is Collectible && it.size == ent[2] }
                }
                if (ent[3] != "-") {
                    productList = productList.filter { it is Clothing && it.primaryColor == ent[3] }
                }
                if (ent[4] != "-") {
                    productList = productList.filter { it is Clothing && it.secundaryColor == ent[4] }
                }
                if (ent[5] != "-") {
                    productList = productList.filter { it is Electronic && it.version == ent[5] }
                }
                if (ent[6] != "-") {
                    productList = productList.filter { it is Electronic && it.fabricationYear == ent[6] }
                }
                if (ent[7] != "-") {
                    productList =
                        productList.filter { it is Collectible && it.fabricationMaterial.name == ent[7].uppercase() }
                }
                if (ent[8] != "-") {
                    productList = productList.filter { it is Collectible && it.relevance.name == ent[8].uppercase() }
                }

                // Calculate the total quantity for the filtered products
                val totalQuantity = productList.sumOf { mpProductsStock[it.code]!! }

                writer.println("$cont, $totalQuantity")
                cont += 1
            }
        }
    }

    csvGeneralStockFile.printWriter().use { writer ->
        writer.println("CODIGO, NOME, QUANTIDADE")

        for (product in mpProducts.values) {
            writer.println("${product.code}, ${product.name}, ${mpProductsStock[product.code]}")
        }
    }

    csvCategorStockFile.printWriter().use { writer ->
        writer.println("CATEGORIA, QUANTIDADE")

        for (category in Category.values()) {
            writer.println("${category}: ${mpCategoryStock[category.name]}")
        }
    }

    csvBalanceteFile.printWriter().use { writer ->
        writer.println("COMPRAS, $totalCompra")
        writer.println("VENDAS, $totalVenda")
        writer.println("BALANCETE, ${totalVenda - totalCompra}")
    }
}


