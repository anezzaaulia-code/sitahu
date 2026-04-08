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
    SQLiteOpenHelper(context, "sitahu.db", null, 7) {

    override fun onCreate(db: SQLiteDatabase) {
        createTables(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 5) {
            ensureColumnExists(db, "produk", "is_deleted", "INTEGER NOT NULL DEFAULT 0")
            ensureColumnExists(db, "pengeluaran", "is_deleted", "INTEGER NOT NULL DEFAULT 0")
        }
        if (oldVersion < 6) {
            normalizeLegacyDateColumns(db)
        }
        if (oldVersion < 7) {
            ensureColumnExists(db, "pengeluaran", "nama_pengeluaran", "TEXT")
            db.execSQL(
                "UPDATE pengeluaran SET nama_pengeluaran = kategori WHERE (nama_pengeluaran IS NULL OR TRIM(nama_pengeluaran) = '') AND kategori IS NOT NULL"
            )
        }
    }

    private fun createTables(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE produk (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nama TEXT NOT NULL,
                satuan TEXT NOT NULL,
                stok_saat_ini INTEGER NOT NULL DEFAULT 0,
                stok_minimum INTEGER NOT NULL DEFAULT 0,
                harga_jual INTEGER NOT NULL DEFAULT 0,
                dibuat_pada TEXT,
                diubah_pada TEXT,
                is_deleted INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE parameter_produksi (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                produk_id INTEGER NOT NULL,
                hasil_per_masak INTEGER NOT NULL DEFAULT 1,
                aktif INTEGER NOT NULL DEFAULT 1
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE produksi (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                produk_id INTEGER NOT NULL,
                tanggal_produksi TEXT NOT NULL,
                jumlah_masak INTEGER NOT NULL,
                jumlah_hasil INTEGER NOT NULL,
                catatan TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE penjualan (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                tanggal_penjualan TEXT NOT NULL,
                total_penjualan INTEGER NOT NULL,
                catatan TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE item_penjualan (
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
        )

        db.execSQL(
            """
            CREATE TABLE mutasi_stok (
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
        )

        db.execSQL(
            """
            CREATE TABLE pengeluaran (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                tanggal_pengeluaran TEXT NOT NULL,
                nama_pengeluaran TEXT NOT NULL,
                nominal INTEGER NOT NULL,
                catatan TEXT,
                is_deleted INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
    }

    fun getTotalProduk(): Int = countQuery("SELECT COUNT(*) FROM produk WHERE is_deleted=0")

    fun getJumlahProduk(): Int = getTotalProduk()

    fun getTotalStok(): Int = countQuery("SELECT COALESCE(SUM(stok_saat_ini), 0) FROM produk WHERE is_deleted=0")

    fun getTotalPengeluaran(): Int = countQuery("SELECT COALESCE(SUM(nominal), 0) FROM pengeluaran WHERE is_deleted=0")

    fun getTotalPengeluaranNominal(): Long = getTotalPengeluaran().toLong()

    private fun countQuery(sql: String): Int {
        val cursor = readableDatabase.rawQuery(sql, null)
        val result = if (cursor.moveToFirst()) cursor.getInt(0) else 0
        cursor.close()
        return result
    }

    private fun ensureColumnExists(
        db: SQLiteDatabase,
        tableName: String,
        columnName: String,
        columnDefinition: String
    ) {
        val cursor = db.rawQuery("PRAGMA table_info($tableName)", null)
        val exists = cursor.use {
            var found = false
            val nameIndex = it.getColumnIndex("name")
            while (it.moveToNext()) {
                if (nameIndex >= 0 && it.getString(nameIndex) == columnName) {
                    found = true
                    break
                }
            }
            found
        }
        if (!exists) {
            db.execSQL("ALTER TABLE $tableName ADD COLUMN $columnName $columnDefinition")
        }
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
        columns.forEach { targetColumn ->
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
                db.update(
                    tableName,
                    ContentValues().apply { put(targetColumn, normalized) },
                    "$idColumn=?",
                    arrayOf(id.toString())
                )
            }
        }
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
        return writableDatabase.update(
            "produk",
            ContentValues().apply {
                put("is_deleted", 1)
                put("diubah_pada", FormatHelper.nowStorage())
            },
            "id=? AND is_deleted=0",
            arrayOf(id.toString())
        ) > 0
    }

    fun getAllProduk(): List<Produk> {
        val list = mutableListOf<Produk>()
        val cursor = readableDatabase.rawQuery("SELECT * FROM produk WHERE is_deleted=0 ORDER BY nama ASC", null)
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
                    diubahPada = cursor.getString(cursor.getColumnIndexOrThrow("diubah_pada")) ?: ""
                )
            )
        }
        cursor.close()
        return list
    }

    fun getProdukById(id: Int): Produk? = getProdukById(readableDatabase, id)

    private fun getProdukById(db: SQLiteDatabase, id: Int): Produk? {
        val cursor = db.rawQuery("SELECT * FROM produk WHERE id=?", arrayOf(id.toString()))
        val produk = if (cursor.moveToFirst()) {
            Produk(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                nama = cursor.getString(cursor.getColumnIndexOrThrow("nama")),
                satuan = cursor.getString(cursor.getColumnIndexOrThrow("satuan")),
                stokSaatIni = cursor.getInt(cursor.getColumnIndexOrThrow("stok_saat_ini")),
                stokMinimum = cursor.getInt(cursor.getColumnIndexOrThrow("stok_minimum")),
                hargaJual = cursor.getInt(cursor.getColumnIndexOrThrow("harga_jual")),
                dibuatPada = cursor.getString(cursor.getColumnIndexOrThrow("dibuat_pada")) ?: "",
                diubahPada = cursor.getString(cursor.getColumnIndexOrThrow("diubah_pada")) ?: ""
            )
        } else {
            null
        }
        cursor.close()
        return produk
    }

    fun insertPengeluaran(pengeluaran: Pengeluaran): Long {
        return writableDatabase.insert(
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
    }

    fun updatePengeluaran(pengeluaran: Pengeluaran): Boolean {
        return writableDatabase.update(
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
    }

    fun deletePengeluaran(id: Int): Boolean {
        return writableDatabase.update(
            "pengeluaran",
            ContentValues().apply {
                put("is_deleted", 1)
            },
            "id=? AND is_deleted=0",
            arrayOf(id.toString())
        ) > 0
    }

    fun getAllPengeluaran(): List<Pengeluaran> {
        val list = mutableListOf<Pengeluaran>()
        val cursor = readableDatabase.rawQuery(
            "SELECT id, tanggal_pengeluaran, nama_pengeluaran, nominal, IFNULL(catatan, '') FROM pengeluaran WHERE is_deleted=0 ORDER BY tanggal_pengeluaran DESC, id DESC",
            null
        )
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
        cursor.close()
        return list
    }

    fun getPengeluaranById(id: Int): Pengeluaran? {
        val cursor = readableDatabase.rawQuery(
            "SELECT id, tanggal_pengeluaran, nama_pengeluaran, nominal, IFNULL(catatan, '') FROM pengeluaran WHERE id=?",
            arrayOf(id.toString())
        )
        val item = if (cursor.moveToFirst()) {
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
        cursor.close()
        return item
    }

    fun getParameterAktif(produkId: Int): Int {
        val cursor = readableDatabase.rawQuery(
            "SELECT hasil_per_masak FROM parameter_produksi WHERE produk_id=? AND aktif=1 LIMIT 1",
            arrayOf(produkId.toString())
        )
        val result = if (cursor.moveToFirst()) cursor.getInt(0) else 1
        cursor.close()
        return result
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
        val list = mutableListOf<MutasiStok>()
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM mutasi_stok WHERE produk_id=? ORDER BY tanggal_mutasi DESC, id DESC",
            arrayOf(produkId.toString())
        )
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
        cursor.close()
        return list
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
