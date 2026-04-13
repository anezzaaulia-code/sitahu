package anezza.aulia.sitahu.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import anezza.aulia.sitahu.FormatHelper
import anezza.aulia.sitahu.MutasiStok
import anezza.aulia.sitahu.Pengeluaran
import anezza.aulia.sitahu.Produk
import anezza.aulia.sitahu.model.RiwayatUmumItem
import java.text.NumberFormat
import java.util.Locale

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        createTables(db)
        ensureSchema(db)
        normalizeLegacyDateColumns(db)
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        ensureSchema(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        ensureSchema(db)
        normalizeLegacyDateColumns(db)
    }

    private fun createTables(db: SQLiteDatabase) {
        listOf(
            CREATE_TABLE_PRODUK,
            CREATE_TABLE_PARAMETER_PRODUKSI,
            CREATE_TABLE_PRODUKSI,
            CREATE_TABLE_PENJUALAN,
            CREATE_TABLE_ITEM_PENJUALAN,
            CREATE_TABLE_MUTASI_STOK,
            CREATE_TABLE_PENGELUARAN
        ).forEach { safeExec(db, it) }
    }

    private fun ensureSchema(db: SQLiteDatabase) {
        createTables(db)

        ensureColumns(
            db,
            "produk",
            linkedMapOf(
                "nama" to "TEXT NOT NULL DEFAULT ''",
                "satuan" to "TEXT NOT NULL DEFAULT ''",
                "stok_saat_ini" to "INTEGER NOT NULL DEFAULT 0",
                "stok_minimum" to "INTEGER NOT NULL DEFAULT 0",
                "harga_jual" to "INTEGER NOT NULL DEFAULT 0",
                "dibuat_pada" to "TEXT",
                "diubah_pada" to "TEXT",
                "aktif" to "INTEGER NOT NULL DEFAULT 1",
                "is_deleted" to "INTEGER NOT NULL DEFAULT 0"
            )
        )
        ensureColumns(
            db,
            "parameter_produksi",
            linkedMapOf(
                "produk_id" to "INTEGER NOT NULL DEFAULT 0",
                "hasil_per_masak" to "INTEGER NOT NULL DEFAULT 1",
                "aktif" to "INTEGER NOT NULL DEFAULT 1"
            )
        )
        ensureColumns(
            db,
            "produksi",
            linkedMapOf(
                "produk_id" to "INTEGER NOT NULL DEFAULT 0",
                "tanggal_produksi" to "TEXT NOT NULL DEFAULT ''",
                "jumlah_masak" to "INTEGER NOT NULL DEFAULT 0",
                "jumlah_hasil" to "INTEGER NOT NULL DEFAULT 0",
                "catatan" to "TEXT"
            )
        )
        ensureColumns(
            db,
            "penjualan",
            linkedMapOf(
                "tanggal_penjualan" to "TEXT NOT NULL DEFAULT ''",
                "total_penjualan" to "INTEGER NOT NULL DEFAULT 0",
                "catatan" to "TEXT"
            )
        )
        ensureColumns(
            db,
            "item_penjualan",
            linkedMapOf(
                "penjualan_id" to "INTEGER NOT NULL DEFAULT 0",
                "produk_id" to "INTEGER NOT NULL DEFAULT 0",
                "nama_produk_snapshot" to "TEXT NOT NULL DEFAULT ''",
                "satuan_snapshot" to "TEXT NOT NULL DEFAULT ''",
                "harga_snapshot" to "INTEGER NOT NULL DEFAULT 0",
                "jumlah" to "INTEGER NOT NULL DEFAULT 0",
                "subtotal" to "INTEGER NOT NULL DEFAULT 0"
            )
        )
        ensureColumns(
            db,
            "mutasi_stok",
            linkedMapOf(
                "produk_id" to "INTEGER NOT NULL DEFAULT 0",
                "jenis_referensi" to "TEXT NOT NULL DEFAULT ''",
                "referensi_id" to "INTEGER NOT NULL DEFAULT 0",
                "tanggal_mutasi" to "TEXT NOT NULL DEFAULT ''",
                "arah" to "TEXT NOT NULL DEFAULT ''",
                "jumlah" to "INTEGER NOT NULL DEFAULT 0",
                "catatan" to "TEXT"
            )
        )
        ensureColumns(
            db,
            "pengeluaran",
            linkedMapOf(
                "tanggal_pengeluaran" to "TEXT NOT NULL DEFAULT ''",
                "nama_pengeluaran" to "TEXT NOT NULL DEFAULT ''",
                "nominal" to "INTEGER NOT NULL DEFAULT 0",
                "catatan" to "TEXT",
                "is_deleted" to "INTEGER NOT NULL DEFAULT 0"
            )
        )

        migrateLegacyData(db)
        backfillDefaultValues(db)
    }

    private fun migrateLegacyData(db: SQLiteDatabase) {
        copyColumnValueIfAvailable(db, "pengeluaran", "kategori", "nama_pengeluaran")
        copyColumnValueIfAvailable(db, "pengeluaran", "keterangan", "catatan")
    }

    private fun backfillDefaultValues(db: SQLiteDatabase) {
        safeExec(db, "UPDATE produk SET stok_saat_ini = 0 WHERE stok_saat_ini IS NULL")
        safeExec(db, "UPDATE produk SET stok_minimum = 0 WHERE stok_minimum IS NULL")
        safeExec(db, "UPDATE produk SET harga_jual = 0 WHERE harga_jual IS NULL")
        safeExec(db, "UPDATE produk SET aktif = 1 WHERE aktif IS NULL")
        safeExec(db, "UPDATE produk SET is_deleted = 0 WHERE is_deleted IS NULL")
        safeExec(db, "UPDATE parameter_produksi SET hasil_per_masak = 1 WHERE hasil_per_masak IS NULL OR hasil_per_masak <= 0")
        safeExec(db, "UPDATE parameter_produksi SET aktif = 1 WHERE aktif IS NULL")
        safeExec(db, "UPDATE produksi SET jumlah_masak = 0 WHERE jumlah_masak IS NULL")
        safeExec(db, "UPDATE produksi SET jumlah_hasil = 0 WHERE jumlah_hasil IS NULL")
        safeExec(db, "UPDATE penjualan SET total_penjualan = 0 WHERE total_penjualan IS NULL")
        safeExec(db, "UPDATE item_penjualan SET harga_snapshot = 0 WHERE harga_snapshot IS NULL")
        safeExec(db, "UPDATE item_penjualan SET jumlah = 0 WHERE jumlah IS NULL")
        safeExec(db, "UPDATE item_penjualan SET subtotal = 0 WHERE subtotal IS NULL")
        safeExec(db, "UPDATE mutasi_stok SET referensi_id = 0 WHERE referensi_id IS NULL")
        safeExec(db, "UPDATE mutasi_stok SET jumlah = 0 WHERE jumlah IS NULL")
        safeExec(db, "UPDATE pengeluaran SET nominal = 0 WHERE nominal IS NULL")
        safeExec(db, "UPDATE pengeluaran SET is_deleted = 0 WHERE is_deleted IS NULL")
        safeExec(db, "UPDATE pengeluaran SET nama_pengeluaran = '' WHERE nama_pengeluaran IS NULL")
        safeExec(db, "UPDATE pengeluaran SET catatan = '' WHERE catatan IS NULL")
    }

    fun getTotalProduk(): Int = countQuery("SELECT COUNT(*) FROM produk WHERE is_deleted=0")

    fun getJumlahProduk(): Int = getTotalProduk()

    fun getTotalStok(): Int = countQuery("SELECT COALESCE(SUM(stok_saat_ini), 0) FROM produk WHERE is_deleted=0")

    fun getTotalPengeluaran(): Int = countQuery("SELECT COALESCE(SUM(nominal), 0) FROM pengeluaran WHERE is_deleted=0")

    fun getTotalPengeluaranNominal(): Long = getTotalPengeluaran().toLong()

    private fun countQuery(sql: String): Int {
        return runCatching {
            readableDatabase.rawQuery(sql, null).use { cursor ->
                if (cursor.moveToFirst()) cursor.getInt(0) else 0
            }
        }.getOrDefault(0)
    }

    private fun ensureColumns(
        db: SQLiteDatabase,
        tableName: String,
        columns: Map<String, String>
    ) {
        columns.forEach { (columnName, definition) ->
            ensureColumnExists(db, tableName, columnName, definition)
        }
    }

    private fun ensureColumnExists(
        db: SQLiteDatabase,
        tableName: String,
        columnName: String,
        columnDefinition: String
    ) {
        if (!tableExists(db, tableName)) return
        val columns = getTableColumns(db, tableName)
        if (columns.contains(columnName)) return
        safeExec(db, "ALTER TABLE $tableName ADD COLUMN $columnName $columnDefinition")
    }

    private fun normalizeLegacyDateColumns(db: SQLiteDatabase) {
        normalizeColumnValues(db, "produk", "id", listOf("dibuat_pada", "diubah_pada"))
        normalizeColumnValues(db, "produksi", "id", listOf("tanggal_produksi"))
        normalizeColumnValues(db, "penjualan", "id", listOf("tanggal_penjualan"))
        normalizeColumnValues(db, "pengeluaran", "id", listOf("tanggal_pengeluaran"))
        normalizeColumnValues(db, "mutasi_stok", "id", listOf("tanggal_mutasi"))
    }

    private fun normalizeColumnValues(
        db: SQLiteDatabase,
        tableName: String,
        idColumn: String,
        columns: List<String>
    ) {
        if (!tableExists(db, tableName) || !hasColumn(db, tableName, idColumn)) return
        columns.forEach { targetColumn ->
            if (!hasColumn(db, tableName, targetColumn)) return@forEach
            val updates = mutableListOf<Pair<Int, String>>()
            val cursor = db.rawQuery(
                "SELECT $idColumn, $targetColumn FROM $tableName WHERE $targetColumn IS NOT NULL AND TRIM($targetColumn) != ''",
                null
            )
            cursor.use {
                while (it.moveToNext()) {
                    val id = it.getInt(0)
                    val rawValue = it.getString(1) ?: continue
                    val normalized = runCatching { FormatHelper.normalizeLegacyTimestamp(rawValue) }.getOrNull() ?: continue
                    if (normalized != rawValue) {
                        updates.add(id to normalized)
                    }
                }
            }
            updates.forEach { (id, normalized) ->
                runCatching {
                    db.update(
                        tableName,
                        ContentValues().apply { put(targetColumn, normalized) },
                        "$idColumn=?",
                        arrayOf(id.toString())
                    )
                }
            }
        }
    }

    private fun copyColumnValueIfAvailable(
        db: SQLiteDatabase,
        tableName: String,
        fromColumn: String,
        toColumn: String
    ) {
        if (!hasColumn(db, tableName, fromColumn) || !hasColumn(db, tableName, toColumn)) return
        safeExec(
            db,
            "UPDATE $tableName SET $toColumn = $fromColumn WHERE ($toColumn IS NULL OR TRIM($toColumn) = '') AND $fromColumn IS NOT NULL AND TRIM($fromColumn) != ''"
        )
    }

    private fun tableExists(db: SQLiteDatabase, tableName: String): Boolean {
        return db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
            arrayOf(tableName)
        ).use { it.moveToFirst() }
    }

    private fun hasColumn(db: SQLiteDatabase, tableName: String, columnName: String): Boolean {
        return getTableColumns(db, tableName).contains(columnName)
    }

    private fun getTableColumns(db: SQLiteDatabase, tableName: String): Set<String> {
        if (!tableExists(db, tableName)) return emptySet()
        val columns = mutableSetOf<String>()
        db.rawQuery("PRAGMA table_info($tableName)", null).use { cursor ->
            val nameIndex = cursor.getColumnIndex("name")
            while (cursor.moveToNext()) {
                if (nameIndex >= 0) {
                    columns.add(cursor.getString(nameIndex))
                }
            }
        }
        return columns
    }

    private fun safeExec(db: SQLiteDatabase, sql: String) {
        runCatching { db.execSQL(sql) }
    }

    companion object {
        private const val DATABASE_NAME = "sitahu.db"
        private const val DATABASE_VERSION = 10

        private val CREATE_TABLE_PRODUK =
            """
            CREATE TABLE IF NOT EXISTS produk (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nama TEXT NOT NULL,
                satuan TEXT NOT NULL,
                stok_saat_ini INTEGER NOT NULL DEFAULT 0,
                stok_minimum INTEGER NOT NULL DEFAULT 0,
                harga_jual INTEGER NOT NULL DEFAULT 0,
                dibuat_pada TEXT,
                diubah_pada TEXT,
                aktif INTEGER NOT NULL DEFAULT 1,
                is_deleted INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()

        private val CREATE_TABLE_PARAMETER_PRODUKSI =
            """
            CREATE TABLE IF NOT EXISTS parameter_produksi (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                produk_id INTEGER NOT NULL,
                hasil_per_masak INTEGER NOT NULL DEFAULT 1,
                aktif INTEGER NOT NULL DEFAULT 1
            )
            """.trimIndent()

        private val CREATE_TABLE_PRODUKSI =
            """
            CREATE TABLE IF NOT EXISTS produksi (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                produk_id INTEGER NOT NULL,
                tanggal_produksi TEXT NOT NULL,
                jumlah_masak INTEGER NOT NULL,
                jumlah_hasil INTEGER NOT NULL,
                catatan TEXT
            )
            """.trimIndent()

        private val CREATE_TABLE_PENJUALAN =
            """
            CREATE TABLE IF NOT EXISTS penjualan (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                tanggal_penjualan TEXT NOT NULL,
                total_penjualan INTEGER NOT NULL,
                catatan TEXT
            )
            """.trimIndent()

        private val CREATE_TABLE_ITEM_PENJUALAN =
            """
            CREATE TABLE IF NOT EXISTS item_penjualan (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                penjualan_id INTEGER NOT NULL,
                produk_id INTEGER NOT NULL,
                nama_produk_snapshot TEXT NOT NULL,
                satuan_snapshot TEXT NOT NULL,
                harga_snapshot INTEGER NOT NULL,
                jumlah INTEGER NOT NULL,
                subtotal INTEGER NOT NULL
            )
            """.trimIndent()

        private val CREATE_TABLE_MUTASI_STOK =
            """
            CREATE TABLE IF NOT EXISTS mutasi_stok (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                produk_id INTEGER NOT NULL,
                jenis_referensi TEXT NOT NULL,
                referensi_id INTEGER NOT NULL,
                tanggal_mutasi TEXT NOT NULL,
                arah TEXT NOT NULL,
                jumlah INTEGER NOT NULL,
                catatan TEXT
            )
            """.trimIndent()

        private val CREATE_TABLE_PENGELUARAN =
            """
            CREATE TABLE IF NOT EXISTS pengeluaran (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                tanggal_pengeluaran TEXT NOT NULL,
                nama_pengeluaran TEXT NOT NULL,
                nominal INTEGER NOT NULL,
                catatan TEXT,
                is_deleted INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
    }

    fun insertProduk(produk: Produk, hasilPerMasak: Int): Long {
        val db = writableDatabase
        val now = FormatHelper.nowStorage()
        return try {
            db.beginTransaction()
            val produkId = db.insert(
                "produk",
                null,
                ContentValues().apply {
                    put("nama", produk.nama)
                    put("satuan", produk.satuan)
                    put("stok_saat_ini", produk.stokSaatIni)
                    put("stok_minimum", produk.stokMinimum)
                    put("harga_jual", produk.hargaJual)
                    put("dibuat_pada", now)
                    put("diubah_pada", now)
                    put("aktif", if (produk.aktif) 1 else 0)
                    put("is_deleted", 0)
                }
            )

            if (produkId == -1L) {
                -1L
            } else {
                upsertParameter(db, produkId.toInt(), hasilPerMasak)
                db.setTransactionSuccessful()
                produkId
            }
        } catch (_: Exception) {
            -1L
        } finally {
            db.endTransaction()
        }
    }

    fun updateProduk(produk: Produk, hasilPerMasak: Int): Boolean {
        val db = writableDatabase
        return try {
            db.beginTransaction()
            val rows = db.update(
                "produk",
                ContentValues().apply {
                    put("nama", produk.nama)
                    put("satuan", produk.satuan)
                    put("stok_saat_ini", produk.stokSaatIni)
                    put("stok_minimum", produk.stokMinimum)
                    put("harga_jual", produk.hargaJual)
                    put("aktif", if (produk.aktif) 1 else 0)
                    put("diubah_pada", FormatHelper.nowStorage())
                },
                "id=?",
                arrayOf(produk.id.toString())
            )
            upsertParameter(db, produk.id, hasilPerMasak)
            if (rows > 0) db.setTransactionSuccessful()
            rows > 0
        } catch (_: Exception) {
            false
        } finally {
            db.endTransaction()
        }
    }

    fun deleteProduk(id: Int): Boolean {
        return runCatching {
            writableDatabase.update(
                "produk",
                ContentValues().apply {
                    put("is_deleted", 1)
                    put("diubah_pada", FormatHelper.nowStorage())
                },
                "id=? AND is_deleted=0",
                arrayOf(id.toString())
            ) > 0
        }.getOrDefault(false)
    }

    fun getAllProduk(includeInactive: Boolean = true): List<Produk> {
        return runCatching {
            val list = mutableListOf<Produk>()
            val selection = if (includeInactive) {
                "SELECT * FROM produk WHERE is_deleted=0 ORDER BY nama ASC"
            } else {
                "SELECT * FROM produk WHERE is_deleted=0 AND aktif=1 ORDER BY nama ASC"
            }
            readableDatabase.rawQuery(selection, null).use { cursor ->
                while (cursor.moveToNext()) {
                    list.add(
                        Produk(
                            id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            nama = cursor.getString(cursor.getColumnIndexOrThrow("nama")),
                            satuan = cursor.getString(cursor.getColumnIndexOrThrow("satuan")),
                            stokSaatIni = cursor.getInt(cursor.getColumnIndexOrThrow("stok_saat_ini")),
                            stokMinimum = cursor.getInt(cursor.getColumnIndexOrThrow("stok_minimum")),
                            hargaJual = cursor.getInt(cursor.getColumnIndexOrThrow("harga_jual")),
                            dibuatPada = cursor.getString(cursor.getColumnIndexOrThrow("dibuat_pada")) ?: "",
                            diubahPada = cursor.getString(cursor.getColumnIndexOrThrow("diubah_pada")) ?: "",
                            aktif = cursor.getInt(cursor.getColumnIndexOrThrow("aktif")) == 1
                        )
                    )
                }
            }
            list
        }.getOrDefault(emptyList())
    }

    fun getActiveProduk(): List<Produk> = getAllProduk(includeInactive = false)

    fun getProdukById(id: Int): Produk? = getProdukById(readableDatabase, id)

    private fun getProdukById(db: SQLiteDatabase, id: Int): Produk? {
        return runCatching {
            db.rawQuery("SELECT * FROM produk WHERE id=?", arrayOf(id.toString())).use { cursor ->
                if (cursor.moveToFirst()) {
                    Produk(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        nama = cursor.getString(cursor.getColumnIndexOrThrow("nama")),
                        satuan = cursor.getString(cursor.getColumnIndexOrThrow("satuan")),
                        stokSaatIni = cursor.getInt(cursor.getColumnIndexOrThrow("stok_saat_ini")),
                        stokMinimum = cursor.getInt(cursor.getColumnIndexOrThrow("stok_minimum")),
                        hargaJual = cursor.getInt(cursor.getColumnIndexOrThrow("harga_jual")),
                        dibuatPada = cursor.getString(cursor.getColumnIndexOrThrow("dibuat_pada")) ?: "",
                        diubahPada = cursor.getString(cursor.getColumnIndexOrThrow("diubah_pada")) ?: "",
                        aktif = cursor.getInt(cursor.getColumnIndexOrThrow("aktif")) == 1
                    )
                } else {
                    null
                }
            }
        }.getOrNull()
    }

    fun insertPengeluaran(pengeluaran: Pengeluaran): Long {
        return runCatching {
            writableDatabase.insert(
                "pengeluaran",
                null,
                ContentValues().apply {
                    put("tanggal_pengeluaran", FormatHelper.normalizeDateTime(pengeluaran.tanggalPengeluaran))
                    put("nama_pengeluaran", pengeluaran.namaPengeluaran)
                    put("nominal", pengeluaran.nominal)
                    put("catatan", pengeluaran.catatan)
                    put("is_deleted", 0)
                }
            )
        }.getOrDefault(-1L)
    }

    fun updatePengeluaran(pengeluaran: Pengeluaran): Boolean {
        return runCatching {
            writableDatabase.update(
                "pengeluaran",
                ContentValues().apply {
                    put("tanggal_pengeluaran", FormatHelper.normalizeDateTime(pengeluaran.tanggalPengeluaran))
                    put("nama_pengeluaran", pengeluaran.namaPengeluaran)
                    put("nominal", pengeluaran.nominal)
                    put("catatan", pengeluaran.catatan)
                },
                "id=?",
                arrayOf(pengeluaran.id.toString())
            ) > 0
        }.getOrDefault(false)
    }

    fun deletePengeluaran(id: Int): Boolean {
        return runCatching {
            writableDatabase.update(
                "pengeluaran",
                ContentValues().apply {
                    put("is_deleted", 1)
                },
                "id=? AND is_deleted=0",
                arrayOf(id.toString())
            ) > 0
        }.getOrDefault(false)
    }

    fun getAllPengeluaran(): List<Pengeluaran> {
        return runCatching {
            val list = mutableListOf<Pengeluaran>()
            readableDatabase.rawQuery(
                "SELECT id, tanggal_pengeluaran, nama_pengeluaran, nominal, IFNULL(catatan, '') AS catatan FROM pengeluaran WHERE is_deleted=0 ORDER BY tanggal_pengeluaran DESC, id DESC",
                null
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    list.add(
                        Pengeluaran(
                            id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            tanggalPengeluaran = cursor.getString(cursor.getColumnIndexOrThrow("tanggal_pengeluaran")),
                            namaPengeluaran = cursor.getString(cursor.getColumnIndexOrThrow("nama_pengeluaran")),
                            nominal = cursor.getInt(cursor.getColumnIndexOrThrow("nominal")),
                            catatan = cursor.getString(cursor.getColumnIndexOrThrow("catatan")) ?: ""
                        )
                    )
                }
            }
            list
        }.getOrDefault(emptyList())
    }

    fun getPengeluaranById(id: Int): Pengeluaran? {
        return runCatching {
            readableDatabase.rawQuery(
                "SELECT id, tanggal_pengeluaran, nama_pengeluaran, nominal, IFNULL(catatan, '') AS catatan FROM pengeluaran WHERE id=?",
                arrayOf(id.toString())
            ).use { cursor ->
                if (cursor.moveToFirst()) {
                    Pengeluaran(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        tanggalPengeluaran = cursor.getString(cursor.getColumnIndexOrThrow("tanggal_pengeluaran")),
                        namaPengeluaran = cursor.getString(cursor.getColumnIndexOrThrow("nama_pengeluaran")),
                        nominal = cursor.getInt(cursor.getColumnIndexOrThrow("nominal")),
                        catatan = cursor.getString(cursor.getColumnIndexOrThrow("catatan")) ?: ""
                    )
                } else {
                    null
                }
            }
        }.getOrNull()
    }

    fun getParameterAktif(produkId: Int): Int {
        return runCatching {
            readableDatabase.rawQuery(
                "SELECT hasil_per_masak FROM parameter_produksi WHERE produk_id=? AND aktif=1 LIMIT 1",
                arrayOf(produkId.toString())
            ).use { cursor ->
                if (cursor.moveToFirst()) cursor.getInt(0) else 1
            }
        }.getOrDefault(1)
    }

    private fun upsertParameter(db: SQLiteDatabase, produkId: Int, hasilPerMasak: Int) {
        val cleanValue = hasilPerMasak.coerceAtLeast(1)
        val existing = db.rawQuery(
            "SELECT id FROM parameter_produksi WHERE produk_id=? LIMIT 1",
            arrayOf(produkId.toString())
        )
        val exists = existing.moveToFirst()
        existing.close()

        if (exists) {
            db.update(
                "parameter_produksi",
                ContentValues().apply {
                    put("hasil_per_masak", cleanValue)
                    put("aktif", 1)
                },
                "produk_id=?",
                arrayOf(produkId.toString())
            )
        } else {
            db.insert(
                "parameter_produksi",
                null,
                ContentValues().apply {
                    put("produk_id", produkId)
                    put("hasil_per_masak", cleanValue)
                    put("aktif", 1)
                }
            )
        }
    }

    fun insertProduksi(
        produkId: Int,
        jumlahMasak: Int,
        tanggal: String,
        catatan: String
    ): Boolean {
        val db = writableDatabase
        val hasilPerMasak = getParameterAktif(produkId)
        val jumlahHasil = jumlahMasak * hasilPerMasak
        val tanggalNormalized = FormatHelper.normalizeDateTime(tanggal)
        if (jumlahMasak <= 0 || jumlahHasil <= 0) return false

        return try {
            db.beginTransaction()
            val produk = getProdukById(db, produkId) ?: return false
            val produksiId = db.insert(
                "produksi",
                null,
                ContentValues().apply {
                    put("produk_id", produkId)
                    put("tanggal_produksi", tanggalNormalized)
                    put("jumlah_masak", jumlahMasak)
                    put("jumlah_hasil", jumlahHasil)
                    put("catatan", catatan)
                }
            )
            if (produksiId == -1L) return false

            updateStokInternal(db, produkId, produk.stokSaatIni + jumlahHasil)
            db.insert(
                "mutasi_stok",
                null,
                ContentValues().apply {
                    put("produk_id", produkId)
                    put("jenis_referensi", "PRODUKSI")
                    put("referensi_id", produksiId)
                    put("tanggal_mutasi", tanggalNormalized)
                    put("arah", "MASUK")
                    put("jumlah", jumlahHasil)
                    put("catatan", if (catatan.isBlank()) "Produksi" else catatan)
                }
            )
            db.setTransactionSuccessful()
            true
        } catch (_: Exception) {
            false
        } finally {
            db.endTransaction()
        }
    }

    fun insertPenjualan(
        items: List<Triple<Int, Int, Int>>,
        total: Int,
        tanggal: String,
        catatan: String
    ): Boolean {
        if (items.isEmpty()) return false
        val db = writableDatabase
        val tanggalNormalized = FormatHelper.normalizeDateTime(tanggal)

        return try {
            db.beginTransaction()
            val stokMap = mutableMapOf<Int, Int>()
            items.groupBy { it.first }.forEach { (produkId, groupedItems) ->
                val produk = getProdukById(db, produkId) ?: return false
                val jumlah = groupedItems.sumOf { it.second }
                if (jumlah > produk.stokSaatIni) return false
                stokMap[produkId] = produk.stokSaatIni
            }

            val penjualanId = db.insert(
                "penjualan",
                null,
                ContentValues().apply {
                    put("tanggal_penjualan", tanggalNormalized)
                    put("total_penjualan", total)
                    put("catatan", catatan)
                }
            )
            if (penjualanId == -1L) return false

            items.forEach { item ->
                val produk = getProdukById(db, item.first) ?: return false
                val itemResult = db.insert(
                    "item_penjualan",
                    null,
                    ContentValues().apply {
                        put("penjualan_id", penjualanId)
                        put("produk_id", item.first)
                        put("nama_produk_snapshot", produk.nama)
                        put("satuan_snapshot", produk.satuan)
                        put("harga_snapshot", produk.hargaJual)
                        put("jumlah", item.second)
                        put("subtotal", item.third)
                    }
                )
                if (itemResult == -1L) return false

                val stokBaru = (stokMap[item.first] ?: produk.stokSaatIni) - item.second
                if (stokBaru < 0) return false
                stokMap[item.first] = stokBaru
                updateStokInternal(db, item.first, stokBaru)

                val mutasiResult = db.insert(
                    "mutasi_stok",
                    null,
                    ContentValues().apply {
                        put("produk_id", item.first)
                        put("jenis_referensi", "PENJUALAN")
                        put("referensi_id", penjualanId)
                        put("tanggal_mutasi", tanggalNormalized)
                        put("arah", "KELUAR")
                        put("jumlah", item.second)
                        put("catatan", if (catatan.isBlank()) "Penjualan" else catatan)
                    }
                )
                if (mutasiResult == -1L) return false
            }

            db.setTransactionSuccessful()
            true
        } catch (_: Exception) {
            false
        } finally {
            db.endTransaction()
        }
    }

    private fun updateStokInternal(db: SQLiteDatabase, produkId: Int, stokBaru: Int) {
        db.update(
            "produk",
            ContentValues().apply {
                put("stok_saat_ini", stokBaru)
                put("diubah_pada", FormatHelper.nowStorage())
            },
            "id=?",
            arrayOf(produkId.toString())
        )
    }

    fun getMutasiByProduk(produkId: Int): List<MutasiStok> {
        return runCatching {
            val list = mutableListOf<MutasiStok>()
            readableDatabase.rawQuery(
                "SELECT * FROM mutasi_stok WHERE produk_id=? ORDER BY tanggal_mutasi DESC, id DESC",
                arrayOf(produkId.toString())
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    list.add(
                        MutasiStok(
                            id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            produkId = cursor.getInt(cursor.getColumnIndexOrThrow("produk_id")),
                            jenisReferensi = cursor.getString(cursor.getColumnIndexOrThrow("jenis_referensi")),
                            referensiId = cursor.getInt(cursor.getColumnIndexOrThrow("referensi_id")),
                            tanggal = cursor.getString(cursor.getColumnIndexOrThrow("tanggal_mutasi")),
                            arah = cursor.getString(cursor.getColumnIndexOrThrow("arah")),
                            jumlah = cursor.getInt(cursor.getColumnIndexOrThrow("jumlah")),
                            catatan = cursor.getString(cursor.getColumnIndexOrThrow("catatan")) ?: ""
                        )
                    )
                }
            }
            list
        }.getOrDefault(emptyList())
    }

    fun getRiwayatUmum(): List<RiwayatUmumItem> {
        val list = mutableListOf<RiwayatUmumItem>()
        val db = readableDatabase
        val rupiah = NumberFormat.getNumberInstance(Locale("id", "ID"))

        try {
            val c = db.rawQuery(
                """
                SELECT pr.id, pr.tanggal_produksi, p.nama, pr.jumlah_hasil, IFNULL(pr.catatan, '')
                FROM produksi pr
                LEFT JOIN produk p ON p.id = pr.produk_id
                ORDER BY pr.tanggal_produksi DESC, pr.id DESC
                """.trimIndent(),
                null
            )

            while (c.moveToNext()) {
                val id = c.getString(0) ?: ""
                val tanggal = c.getString(1) ?: ""
                val namaProduk = c.getString(2) ?: "Produk"
                val jumlahHasil = c.getInt(3)
                val catatan = c.getString(4) ?: ""

                list.add(
                    RiwayatUmumItem(
                        id = "PROD-${id.padStart(4, '0')}",
                        tanggal = tanggal,
                        tipe = "PRODUKSI",
                        judul = "Produksi $namaProduk",
                        subtitle = if (catatan.isNotEmpty()) catatan else "$namaProduk diproduksi",
                        nilai = "$jumlahHasil pcs"
                    )
                )
            }
            c.close()
        } catch (_: Exception) {
        }

        try {
            val c = db.rawQuery(
                """
                SELECT id, tanggal_penjualan, total_penjualan
                FROM penjualan
                ORDER BY tanggal_penjualan DESC, id DESC
                """.trimIndent(),
                null
            )

            while (c.moveToNext()) {
                val id = c.getString(0) ?: ""
                val tanggal = c.getString(1) ?: ""
                val total = c.getLong(2)

                list.add(
                    RiwayatUmumItem(
                        id = "TRX-${id.padStart(4, '0')}",
                        tanggal = tanggal,
                        tipe = "PENJUALAN",
                        judul = "Penjualan",
                        subtitle = "Transaksi penjualan tersimpan",
                        nilai = "Rp${rupiah.format(total)}"
                    )
                )
            }
            c.close()
        } catch (_: Exception) {
        }

        try {
            val c = db.rawQuery(
                """
                SELECT id, tanggal_pengeluaran, nama_pengeluaran, nominal, IFNULL(catatan, '')
                FROM pengeluaran
                WHERE is_deleted=0
                ORDER BY tanggal_pengeluaran DESC, id DESC
                """.trimIndent(),
                null
            )

            while (c.moveToNext()) {
                val id = c.getString(0) ?: ""
                val tanggal = c.getString(1) ?: ""
                val namaPengeluaran = c.getString(2) ?: "Pengeluaran"
                val nominal = c.getLong(3)
                val catatan = c.getString(4) ?: ""

                list.add(
                    RiwayatUmumItem(
                        id = "OUT-${id.padStart(4, '0')}",
                        tanggal = tanggal,
                        tipe = "PENGELUARAN",
                        judul = namaPengeluaran,
                        subtitle = if (catatan.isNotEmpty()) catatan else "Data pengeluaran",
                        nilai = "Rp${rupiah.format(nominal)}"
                    )
                )
            }
            c.close()
        } catch (_: Exception) {
        }

        return list.sortedByDescending { it.tanggal }
    }
}
