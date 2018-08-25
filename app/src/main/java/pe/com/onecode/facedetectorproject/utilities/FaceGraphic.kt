/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pe.com.onecode.facedetectorproject.utilities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.google.firebase.ml.vision.face.FirebaseVisionFace

class FaceGraphic(overlay: GraphicOverlay) : GraphicOverlay.Graphic(overlay) {

    companion object {
        private const val FACE_POSITION_RADIUS = 10.0f
        private const val FACE_Y_OFFSET = 50.0f
        private const val FACE_X_OFFSET = 10.0f
        private const val ID_TEXT_SIZE = 28.0f
        private const val SMILE_X_OFFSET = -30.0f
        private const val SMILE_Y_OFFSET = 50.0f
        // private const val ID_Y_OFFSET = 50.0f
        // private const val ID_X_OFFSET = -50.0f
        private const val BOX_STROKE_WIDTH = 5.0f

        private val COLOR_CHOICES = intArrayOf(Color.MAGENTA, Color.BLUE, Color.CYAN, Color.GREEN, Color.RED, Color.WHITE, Color.YELLOW)
        private var mCurrentColorIndex = 0
    }

    private val mFacePositionPaint: Paint
    private val mIdPaint: Paint
    private val mBoxPaint: Paint

    @Volatile
    private var mFirebaseVisionFace: FirebaseVisionFace? = null

    // Initialize variables
    init {

        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.size
        val selectedColor = COLOR_CHOICES[mCurrentColorIndex]


        mFacePositionPaint = Paint()
        mFacePositionPaint.color = selectedColor

        mIdPaint = Paint()
        mIdPaint.color = selectedColor
        mIdPaint.textSize = ID_TEXT_SIZE

        mBoxPaint = Paint()
        mBoxPaint.color = selectedColor
        mBoxPaint.style = Paint.Style.STROKE
        mBoxPaint.strokeWidth = BOX_STROKE_WIDTH
    }

    /**
     * Updates the face instance from the detection of the most recent frame. Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    fun updateFace(face: FirebaseVisionFace) {
        mFirebaseVisionFace = face
        postInvalidate()
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    override fun draw(canvas: Canvas) {
        val face = mFirebaseVisionFace

        face?.let {
            //  val x = translateX(face.boundingBox.centerX().toFloat())
            //  val y = translateY(face.boundingBox.centerY().toFloat())

            // Dibuja el rectángulo de la cara detectada
            drawRect(canvas, face.boundingBox.centerX().toFloat() + FACE_X_OFFSET, face.boundingBox.centerY().toFloat() + FACE_Y_OFFSET)
            // Dibuja el Id
            drawText(canvas, face.boundingBox.centerX().toFloat(), face.boundingBox.centerY().toFloat(), "Id: ${face.trackingId}")
            // Dibuja probabilidad de sonreir
            drawText(canvas, face.boundingBox.centerX().toFloat() + SMILE_X_OFFSET, face.boundingBox.centerY().toFloat() + SMILE_Y_OFFSET, String.format("Happiness: %.2f%%", face.smilingProbability * 100))
        }
    }

    private fun drawCircle(canvas: Canvas, x: Float, y: Float) {
        canvas.drawCircle(x, y, FACE_POSITION_RADIUS, mFacePositionPaint)
    }

    private fun drawText(canvas: Canvas, x: Float, y: Float, text: String) {
        canvas.drawText(text, x, y, mIdPaint)
    }

    private fun drawRect(canvas: Canvas, x: Float, y: Float) {
        val rectangle = calculateBoxDimens(x, y)
        canvas.drawRect(rectangle.left, rectangle.top, rectangle.right, rectangle.bottom, mBoxPaint)
    }

    private fun calculateBoxDimens(x: Float, y: Float): Rectangle {

        val xOffset = scaleX(mFirebaseVisionFace!!.boundingBox.width() / 2.0f)
        val yOffset = scaleY(mFirebaseVisionFace!!.boundingBox.height() / 2.0f)
        val left = x - xOffset
        val top = y - yOffset
        val right = x + xOffset
        val bottom = y + yOffset

        return Rectangle(left, top, right, bottom)
    }

    private data class Rectangle(val left: Float, val top: Float, val right: Float, val bottom: Float)
}