package com.canberksudoku.sudoku

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class Sudoku(context: Context) {
    var solved = false
    var ready = false
    var puzzle: Array<IntArray>? = null
    var solution: Array<IntArray>? = null
    var checked = false
    private var curContext: Context = context

    private fun Cizim(string: String) {
        var i = 0;
        this.puzzle = Array(9) { IntArray(9) }
        this.solution = Array(9) { IntArray(9) }
        while (i < string.length) {
            if (string[i] == '.') {
                this.puzzle!![i / 9][i % 9] = -1
                this.solution!![i / 9][i % 9] = -1
            } else {
                this.puzzle!![i / 9][i % 9] = Character.getNumericValue(string[i])
                this.solution!![i / 9][i % 9] = Character.getNumericValue(string[i])
            }
            i++
        }
        ready = true
    }
    fun Baslama(): Boolean {
        return solved
    }
    private fun kutucukSecme() {
        this.checked = true
    }
    fun gecersizKil() {
        this.checked = false
    }
    private fun secim() {
        this.solved = true
    }
    fun gecersizKilindi(): Boolean {
        return checked
    }
    fun doldurulcakKonum (i: Int, j: Int): Boolean {
        return this.puzzle?.get(i)?.get(j) == -1
    }
    fun isHazir(): Boolean {
        return ready
    }

    //BURADA AŞŞAĞIDA BELİRLENMİŞ LİNKE GİDİP RANDOM SUDOKU SAYILARI ÇEKTİRİYORUZ.
    fun Kodcekme() {
        val queue = Volley.newRequestQueue(curContext)
        val url = "https://agarithm.com/sudoku/new"
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->
                Log.i("Bilgi", "Yanıt $response")
                Cizim(response)
            },
            Response.ErrorListener { error: VolleyError? ->
                Log.e("Hata", "Hatası: $error")
            }
        )
        queue.add(stringRequest)
    }


    //BURADA SUDOKU ÜZERİNDEKİ İŞLEMLERİ SIFIRLATIYORUZ
    fun SudkouSifirlama() {
        var counter = 0
        while (counter < 81) {
            this.puzzle?.get(counter/9)?.get(counter%9)?.let {
                this.solution?.get(counter/9)?.set(counter%9,
                    it
                )
            };
            counter++
        }
    }
    //SUDOKU KONTROL EDİYORUZ HER SATIRI VE SUTUNU KONTROL ETTİRİYORUZ.
    fun sudokuKontrol() {
        this.kutucukSecme()
        var hash : HashMap<Int, Int> = HashMap<Int, Int>()
        for(i in 1..9)
            hash[i] = 0

        for (i in 0..8) {
            for (j in 1..9) {
                hash[j] = 0
            }
            for(j in 0..8) {
                if (solution?.get(i)?.get(j) != -1)
                    hash[solution?.get(i)?.get(j)]?.plus(1)
            }
            for (j in 1..9) {
                if (hash[j] != 1)
                    return
            }
        }


        for (i in 0..8) {
            for (j in 1..9) {
                hash[j] = 0
            }
            for(j in 0..8) {
                if (solution?.get(j)?.get(i) != -1)
                    hash[solution?.get(j)?.get(i)]?.plus(1)
            }
            for (j in 1..9) {
                if (hash[j] != 1)
                    return
            }
        }


        for (i in 0..2) {
            for(j in 0..2) {
                for (k in 1..9) {
                    hash[k] = 0
                }

                for (k in 0..2) {
                    for(l in 0..2) {
                        if (solution?.get(i * 3 + k)?.get(j * 3 + l) != -1)
                            hash[solution?.get(i * 3 + k)?.get(j * 3 + l)]?.plus(1)
                    }
                }
                for (k in 1..9) {
                    if (hash[k] != 1)
                        return
                }
            }
        }
        this.secim() // değerimizi true çeviriyoruz.
    }
//RAKAMLARI SEÇTİĞİMİZ KUTUCUĞA EKLEME FONKSİYONU
    fun rakamEkleme(i: Int, j: Int, value: Int) {
        Log.i("INFO", "Adding $value")
        if (i != -1 && j != -1) {
            this.solution?.get(i)?.set(j, value)
        } else {
            Log.i("rakamEkleme", "Kutucuk seçin ve rakama tıklayın.")
        }
    }
    //RAKAMLARI İSTEDİĞİMİZ KUTUCUKTAN KALDIRMA VEYA KUTUCUKDAKİ RAKAMI DEĞİSTİRME
    fun rakamDegistirme(i: Int, j: Int) {
        Log.i("Degistirme", "Rakam Degistirme")
        if (i == -1 || j == -1) {
            Log.i("Degistirme","Kutucuk Seçilmediği için rakam eklenemedi")
        } else if (this.solution?.get(i)?.get(j) == -1) {
            Log.i("Degistirme","Rakam ekleme başarısız yanlış kutucuk seçimi")
        } else {
            this.solution?.get(i)?.set(j, -1)
            Log.i("Degistirme", "Rakam Kaldırma ($i, $j)")
        }
    }
}