package com.canberksudoku.sudoku

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat

class SudokuCanvas(context: Context, private var sudoku: Sudoku) : View(context) {
    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap
    private var puzzleTileLength = 100
    private val topMargin = 100;
    private val leftMargin = 100
    private val backgroundColor = ResourcesCompat.getColor(resources, R.color.backgroundColor, null)
    private val drawColor = ResourcesCompat.getColor(resources, R.color.drawColor, null)
    private val boldColor = ResourcesCompat.getColor(resources, R.color.colorPrimary, null)
    private val boxBoldWidth = 10f
    private val keyboardTextWidth = 25f
    private var defaultPaint = Paint().apply {
        color = drawColor

        isAntiAlias = true

        isDither = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }
    //KUTUCUK SEÇİM RENGİ
    private val outlinePaint: Paint = Paint().apply {
        color = Color.BLUE
        isAntiAlias = true

        isDither = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = boxBoldWidth
    }
    private val boldPaint = Paint().apply {
        color = boldColor
        isAntiAlias = true

        isDither = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = boxBoldWidth
    }
    //KUTUCUK İÇİ RAKAMLARI RENGİ
    private val textSize = 50f
    //RAKAMLARIN RENGİ VE BOYUTLARI
    private val textPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
        textSize = 80f
    }
    //SATIR VE SUTUNLARIN RENGİ VE BOYUTLARI
    private var constantPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
        textSize = 80f
    }
    //HATA MESAJI RENGİ VE BOYUTLARI
    private val messagePaint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.FILL
        textSize = 100f
    }
    //BUTON RENGİ
    private val buttonPaint = Paint().apply{
        style = Paint.Style.FILL
        color = Color.TRANSPARENT
    }
    private var currJ = -1
    private var currI = -1
    private var startedRequest = false
    private var keyboardOffset = 10
    private var keyboardKeyLength = 70

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (::extraBitmap.isInitialized)    extraBitmap.recycle()
        extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)
        extraCanvas.drawColor(backgroundColor)
        if (!startedRequest) {
            sudoku.Kodcekme()
            startedRequest = true
        }
    }

    //YÜKLEME EKRANIM
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas != null && this.sudoku.isHazir()) {
            canvas.drawBitmap(extraBitmap, 0f, 0f, defaultPaint)
            dikdortgenCiz(9, 9, canvas, defaultPaint, puzzleTileLength, leftMargin, topMargin)
            dikdortgenCiz(3, 3, canvas, boldPaint, puzzleTileLength*3, leftMargin, topMargin)
            numaraAlindi(9, 9, canvas, textPaint, constantPaint, outlinePaint, puzzleTileLength, leftMargin, topMargin,
                this.sudoku.puzzle, this.sudoku.solution)
            klavyeAlmak(canvas, buttonPaint, textPaint)
        } else if (canvas != null) {
            canvas.drawBitmap(extraBitmap, 0f, 8f, defaultPaint)
            canvas.drawText("Yükleniyor biraz bekleyiniz", this.leftMargin.toFloat(), this.topMargin.toFloat(), this.textPaint)//YÜKLEME EKRANINDA Kİ BİLRİRİM

            Handler().postDelayed(
                { this@SudokuCanvas.invalidate() }, 5000 //YÜKLEME EKRANINDAKİ DELAY
            )
        }
    }

    private fun klavyeAlmak(canvas: Canvas, buttonPaint: Paint, textPaint: Paint) {
        var leftX = leftMargin
        var topX = topMargin* 2 + this.puzzleTileLength * 9
        var length = this.keyboardKeyLength
        var leftOffset = this.keyboardOffset
        var textSize = this.keyboardTextWidth
        var i = 0
        while (i <= 9) {
            val r = Rect(leftX, topX, leftX+length, topX+length)
            canvas.drawRect(r, buttonPaint)
            leftX += length + leftOffset
            i++
        }
        leftX = leftMargin
        i = 0
        while (i <= 9) {
            val text = if (i == 0) {
                ""
            } else {
                i.toString()
            }
            canvas.drawText(text, (leftX + length/2.0 - textSize/2).toFloat(),
                (topX + length/2.0 + 3*textSize/4).toFloat(), textPaint)
            i++
            leftX += length + leftOffset
        }

        leftX = leftMargin ;
        topX = topMargin * 3 + this.puzzleTileLength * 10
    //SIFIRLA VE KONTROL BUTONLARIM
        val resetButton = Rect(leftX, topX, leftX + this.puzzleTileLength* 5 ,topX + this.puzzleTileLength)
        val resetText = "Sıfırla"
        val checkText = "Kontrol"
        val checkButton = Rect(leftX + this.leftMargin + this.puzzleTileLength * 3, topX,
            leftX + this.leftMargin + this.puzzleTileLength * 4 ,topX + this.puzzleTileLength)

        canvas.drawRect(resetButton, buttonPaint)
        canvas.drawText(resetText,
            (leftX + this.puzzleTileLength - 3*this.textSize/2.0).toFloat(),
            (topX + this.puzzleTileLength/2.0 + this.textSize/2.0).toFloat(), textPaint)
        canvas.drawRect(checkButton, buttonPaint)
        canvas.drawText(checkText,
            (leftX + this.leftMargin + this.puzzleTileLength * 2 + this.puzzleTileLength - 3*this.textSize/2.0).toFloat(),
            (topX + this.puzzleTileLength/2.0 + this.textSize/2.0).toFloat(), textPaint)
        if (this.sudoku.gecersizKilindi()) {
            this.sudoku.gecersizKil()
            var text = "Yanlış !"//SUDOKU DOĞRULUK ONAYI
            if (this.sudoku.Baslama()) {
                text = "Doğru !"//SUDOKU DOĞRULUK ONAYI
            }
            canvas.drawText(
                text,
                leftMargin.toFloat(),
                (topMargin * 4 + this.puzzleTileLength * 11).toFloat(),
                messagePaint
            )
        }
    }

    private fun numaraAlindi(rows: Int, columns: Int, canvas: Canvas, defaultPaint: Paint, constantPaint: Paint, outlinePaint: Paint,
                             length: Int, leftOffset: Int, topOffset: Int, puzzle: Array<IntArray>?,
                             solution: Array<IntArray>?) {
        var topX = topOffset;
        var leftX = leftOffset;
        var counter = 0

        while(counter < rows * columns) {
            var i = counter/9
            var j = counter%9
            if (solution!![i][j] != -1) {
                var text = solution[i][j].toString()
                var paint = defaultPaint
                if (puzzle!![i][j] != -1)
                    paint = constantPaint

                canvas.drawText(
                    text,
                    (leftX + length / 2.0 - this.textSize/2).toFloat(), (topX + length / 2.0 + this.textSize/2).toFloat(), paint
                )
            }
            if (i == currI && j == currJ) {
                var r = Rect(leftX, topX, leftX + length, topX + length)
                canvas.drawRect(r, outlinePaint)
            }
            counter++
            leftX += length
            if (counter % rows == 0) {
                topX += length
                leftX = leftOffset
            }
        }
    }
    //DİKDÖRTGEN ÇİZEN FONKSİYONUM
    private fun dikdortgenCiz(rows: Int, columns: Int, canvas: Canvas, paint: Paint,
                              incr: Int, leftOffset: Int, topOffset: Int) {
        var topX = topOffset
        var leftX = leftOffset
        var counter = 0
        while(counter < rows*columns) {
            val r = Rect(leftX, topX, leftX+incr, topX+incr)
            canvas.drawRect(r, paint)
            counter++
            leftX += incr
            if (counter % rows == 0) {
                topX += incr
                leftX = leftOffset
            }
        }
    }
    //TIKLAMA FONKSİYONUMUZ
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        if (event!= null) {
            var action = event.action
            if(action == MotionEvent.ACTION_DOWN) {
                var j = ((event.x-leftMargin)/this.puzzleTileLength).toInt()
                var i = ((event.y-topMargin)/this.puzzleTileLength).toInt()
                if (i < 9 && j < 9) {
                    if (this.sudoku.doldurulcakKonum(i, j)) {
                        currI = i
                        currJ = j
                    } else {
                        currJ = -1
                        currI = -1
                    }
                } else {
                    var indexY = this.puzzleTileLength*9 + this.topMargin*2
                    Log.i("Tuştakimi", "Etkinlik gerçekleşti(${event.y} , ${event.x})")
                    if (event.y >= indexY && event.y <= indexY + this.keyboardKeyLength) {
                        var counter = 0
                        while((leftMargin + (this.keyboardOffset + this.keyboardKeyLength) * counter) < event.x)
                            counter++
                        Log.i("Tuştakimi", "Sayaç $counter")
                        if (counter in 1..10) {
                            var indexX = leftMargin + (this.keyboardOffset + this.keyboardKeyLength) * (counter - 1)
                            Log.i("Tuştakimi", "Y Endeksi: $indexX ve Y etkinliği ${event.x}")
                            if (event.x <= indexX + this.keyboardKeyLength) {
                                if (counter == 1)
                                    this.sudoku.rakamDegistirme(this.currI, this.currJ)
                                else
                                    this.sudoku.rakamEkleme(this.currI, this.currJ, counter - 1)
                            }
                            Log.i("Tuştakimi", "Değer $i, $j is ${this.sudoku.solution?.get(currI)?.get(currJ)}")
                        }
                    } else {
                        indexY = this.puzzleTileLength*10 + this.topMargin*3
                        if (event.y >= indexY && event.y <= indexY + this.puzzleTileLength) {
                            var resetX = this.leftMargin
                            var checkX = resetX + this.leftMargin + this.puzzleTileLength * 2
                            if (event.x >= resetX && event.x <= resetX + this.puzzleTileLength * 2) {
                                this.sudoku.SudkouSifirlama();
                            } else if (event.x >= checkX && event.x <= checkX + this.puzzleTileLength * 2) {
                                this.sudoku.sudokuKontrol();
                            }
                        }
                    }
                }
            }
        }
        this.invalidate()
        return  true
    }
}