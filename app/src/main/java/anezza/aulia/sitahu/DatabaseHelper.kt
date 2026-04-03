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
}