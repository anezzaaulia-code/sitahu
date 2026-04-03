package anezza.aulia.sitahu

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "sitahu.db", null, 1) {

    companion object {
        const val TABLE_PRODUK = "produk"
        const val ID = "id"
        const val NAMA = "nama"
        const val KATEGORI = "kategori"
        const val STOK = "stok"
        const val MIN_STOK = "min_stok"
        const val TABLE_LOG = "stok_log"
        const val LOG_ID = "id"
        const val LOG_PRODUK_ID = "produk_id"
        const val LOG_TIPE = "tipe"
        const val LOG_JUMLAH = "jumlah"
        const val LOG_TANGGAL = "tanggal"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_PRODUK (
            $ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $NAMA TEXT,
            $KATEGORI TEXT,
            $STOK INTEGER,
            $MIN_STOK INTEGER
        )
    """.trimIndent()

        db.execSQL(createTable)

        val createLogTable = """
            CREATE TABLE $TABLE_LOG (
                $LOG_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $LOG_PRODUK_ID INTEGER,
                $LOG_TIPE TEXT,
                $LOG_JUMLAH INTEGER,
                $LOG_TANGGAL TEXT
            )
        """.trimIndent()

        db.execSQL(createLogTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    fun insertProduk(produk: Produk): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(NAMA, produk.nama)
            put(KATEGORI, produk.kategori)
            put(STOK, produk.stok)
            put(MIN_STOK, produk.minStok)
        }
        return db.insert(TABLE_PRODUK, null, values)
    }

    fun getAllProduk(): List<Produk> {
        val list = mutableListOf<Produk>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_PRODUK", null)

        while (cursor.moveToNext()) {
            list.add(
                Produk(
                    id = cursor.getInt(0),
                    nama = cursor.getString(1),
                    kategori = cursor.getString(2),
                    stok = cursor.getInt(3),
                    minStok = cursor.getInt(4)
                )
            )
        }

        cursor.close()
        return list
    }

    fun deleteProduk(id: Int) {
        writableDatabase.delete(TABLE_PRODUK, "$ID=?", arrayOf(id.toString()))
    }

    fun getProdukById(id: Int): Produk? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_PRODUK WHERE $ID=?",
            arrayOf(id.toString())
        )

        var produk: Produk? = null

        if (cursor.moveToFirst()) {
            produk = Produk(
                id = cursor.getInt(0),
                nama = cursor.getString(1),
                kategori = cursor.getString(2),
                stok = cursor.getInt(3),
                minStok = cursor.getInt(4)
            )
        }

        cursor.close()
        return produk
    }

    fun updateStok(id: Int, stokBaru: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(STOK, stokBaru)
        }

        db.update(TABLE_PRODUK, values, "$ID=?", arrayOf(id.toString()))
    }

    fun insertLog(produkId: Int, tipe: String, jumlah: Int, tanggal: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(LOG_PRODUK_ID, produkId)
            put(LOG_TIPE, tipe)
            put(LOG_JUMLAH, jumlah)
            put(LOG_TANGGAL, tanggal)
        }
        db.insert(TABLE_LOG, null, values)
    }

    fun getLogByProdukId(produkId: Int): List<Triple<String, Int, String>> {
        val list = mutableListOf<Triple<String, Int, String>>()
        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT tipe, jumlah, tanggal FROM $TABLE_LOG WHERE produk_id=? ORDER BY id DESC LIMIT 4",
            arrayOf(produkId.toString())
        )

        while (cursor.moveToNext()) {
            val tipe = cursor.getString(0)
            val jumlah = cursor.getInt(1)
            val tanggal = cursor.getString(2)

            list.add(Triple(tipe, jumlah, tanggal))
        }

        cursor.close()
        return list
    }
}