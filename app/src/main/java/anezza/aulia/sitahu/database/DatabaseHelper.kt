package anezza.aulia.sitahu.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import anezza.aulia.sitahu.Produk
import anezza.aulia.sitahu.MutasiStok

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "sitahu.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {

        db.execSQL("""
            CREATE TABLE produk (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nama TEXT,
                satuan TEXT,
                stok_saat_ini INTEGER,
                stok_minimum INTEGER,
                harga_jual INTEGER,
                dibuat_pada TEXT,
                diubah_pada TEXT
            )
        """)

        db.execSQL("""
            CREATE TABLE parameter_produksi (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                produk_id INTEGER,
                hasil_per_masak INTEGER,
                aktif INTEGER
            )
        """)

        db.execSQL("""
            CREATE TABLE produksi (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                produk_id INTEGER,
                tanggal_produksi TEXT,
                jumlah_masak INTEGER,
                jumlah_hasil INTEGER,
                catatan TEXT
            )
        """)

        db.execSQL("""
            CREATE TABLE mutasi_stok (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                produk_id INTEGER,
                jenis_referensi TEXT,
                referensi_id INTEGER,
                tanggal_mutasi TEXT,
                arah TEXT,
                jumlah INTEGER,
                catatan TEXT
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    // =========================
    // PRODUK
    // =========================

    fun getAllProduk(): List<Produk> {
        val list = mutableListOf<Produk>()
        val db = readableDatabase

        val cursor = db.rawQuery("SELECT * FROM produk", null)

        while (cursor.moveToNext()) {
            list.add(
                Produk(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    nama = cursor.getString(cursor.getColumnIndexOrThrow("nama")),
                    satuan = cursor.getString(cursor.getColumnIndexOrThrow("satuan")),
                    stokSaatIni = cursor.getInt(cursor.getColumnIndexOrThrow("stok_saat_ini")),
                    stokMinimum = cursor.getInt(cursor.getColumnIndexOrThrow("stok_minimum")),
                    hargaJual = cursor.getInt(cursor.getColumnIndexOrThrow("harga_jual")),
                    dibuatPada = cursor.getString(cursor.getColumnIndexOrThrow("dibuat_pada")),
                    diubahPada = cursor.getString(cursor.getColumnIndexOrThrow("diubah_pada"))
                )
            )
        }

        cursor.close()
        return list
    }

    fun getProdukNamaList(): ArrayList<String> {
        val list = ArrayList<String>()
        val db = readableDatabase

        val cursor = db.rawQuery("SELECT nama FROM produk", null)

        while (cursor.moveToNext()) {
            list.add(cursor.getString(0))
        }

        cursor.close()
        return list
    }

    fun getProdukIdByNama(nama: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id FROM produk WHERE nama=?",
            arrayOf(nama)
        )

        return if (cursor.moveToFirst()) {
            cursor.getInt(0)
        } else 0
    }

    fun getProdukById(id: Int): Produk? {
        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM produk WHERE id=?",
            arrayOf(id.toString())
        )

        var produk: Produk? = null

        if (cursor.moveToFirst()) {
            produk = Produk(
                id = cursor.getInt(0),
                nama = cursor.getString(1),
                satuan = cursor.getString(2),
                stokSaatIni = cursor.getInt(3),
                stokMinimum = cursor.getInt(4),
                hargaJual = cursor.getInt(5),
                dibuatPada = cursor.getString(6),
                diubahPada = cursor.getString(7)
            )
        }

        cursor.close()
        return produk
    }

    fun updateStok(produkId: Int, stokBaru: Int) {
        val db = writableDatabase

        val values = ContentValues().apply {
            put("stok_saat_ini", stokBaru)
            put("diubah_pada", System.currentTimeMillis().toString())
        }

        db.update("produk", values, "id=?", arrayOf(produkId.toString()))
    }

    // =========================
    // PARAMETER
    // =========================

    fun getParameterAktif(produkId: Int): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT hasil_per_masak FROM parameter_produksi WHERE produk_id=? AND aktif=1 LIMIT 1",
            arrayOf(produkId.toString())
        )

        return if (cursor.moveToFirst()) cursor.getInt(0) else 0
    }

    // =========================
    // PRODUKSI + STOK + MUTASI
    // =========================

    fun insertProduksi(
        produkId: Int,
        jumlahMasak: Int,
        catatan: String
    ): Boolean {

        val db = writableDatabase

        val hasilPerMasak = getParameterAktif(produkId)
        val jumlahHasil = jumlahMasak * hasilPerMasak
        val now = System.currentTimeMillis().toString()

        val produksiValues = ContentValues().apply {
            put("produk_id", produkId)
            put("tanggal_produksi", now)
            put("jumlah_masak", jumlahMasak)
            put("jumlah_hasil", jumlahHasil)
            put("catatan", catatan)
        }

        val produksiId = db.insert("produksi", null, produksiValues)

        if (produksiId == -1L) return false

        val produk = getProdukById(produkId)
        val stokBaru = (produk?.stokSaatIni ?: 0) + jumlahHasil

        updateStok(produkId, stokBaru)

        val mutasiValues = ContentValues().apply {
            put("produk_id", produkId)
            put("jenis_referensi", "PRODUKSI")
            put("referensi_id", produksiId)
            put("tanggal_mutasi", now)
            put("arah", "MASUK")
            put("jumlah", jumlahHasil)
            put("catatan", "Produksi")
        }

        db.insert("mutasi_stok", null, mutasiValues)

        return true
    }

    fun insertPenjualan(
        items: List<Triple<Int, Int, Int>>,
        total: Int
    ): Boolean {

        val db = writableDatabase
        val now = System.currentTimeMillis().toString()

        val penjualanValues = ContentValues().apply {
            put("tanggal_penjualan", now)
            put("total_penjualan", total)
            put("catatan", "Rekap pasar")
        }

        val penjualanId = db.insert("penjualan", null, penjualanValues)

        if (penjualanId == -1L) return false

        for (item in items) {

            val produkId = item.first
            val jumlah = item.second
            val subtotal = item.third

            val produk = getProdukById(produkId) ?: continue

            // insert item
            val itemValues = ContentValues().apply {
                put("penjualan_id", penjualanId)
                put("produk_id", produkId)
                put("nama_produk_snapshot", produk.nama)
                put("satuan_snapshot", produk.satuan)
                put("harga_snapshot", produk.hargaJual)
                put("jumlah", jumlah)
                put("subtotal", subtotal)
            }

            db.insert("item_penjualan", null, itemValues)

            // update stok (KURANG)
            val stokBaru = produk.stokSaatIni - jumlah
            updateStok(produkId, stokBaru)

            // mutasi stok
            val mutasi = ContentValues().apply {
                put("produk_id", produkId)
                put("jenis_referensi", "PENJUALAN")
                put("referensi_id", penjualanId)
                put("tanggal_mutasi", now)
                put("arah", "KELUAR")
                put("jumlah", jumlah)
                put("catatan", "Penjualan")
            }

            db.insert("mutasi_stok", null, mutasi)
        }

        return true
    }

    fun getMutasiByProduk(produkId: Int): List<MutasiStok> {
        val list = mutableListOf<MutasiStok>()
        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM mutasi_stok WHERE produk_id=? ORDER BY id DESC",
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
                    catatan = cursor.getString(cursor.getColumnIndexOrThrow("catatan"))
                )
            )
        }

        cursor.close()
        return list
    }

}