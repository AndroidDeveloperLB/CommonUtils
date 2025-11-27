package com.lb.common_utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.graphics.PathParser
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.graphics.drawable.toDrawable

object AdaptiveIconUtils {

    /**
     * Defines the desired shape for the icon.
     */
    sealed class IconShape {
        /**
         * API 26+: Uses the device's native shape (Circle, Squircle, etc.).
         * Legacy: Falls back to Circle.
         */
        object System : IconShape()

        /**
         * Forces a Circle shape on ALL versions.
         */
        object Circle : IconShape()

        /**
         * Forces a standard Square shape.
         */
        object Square : IconShape()

        /**
         * Forces a Rounded Square (Standard Android style).
         * @param cornerRadiusRatio Radius as a percentage of width (0.0 to 0.5). Default 20%.
         */
        data class RoundedSquare(val cornerRadiusRatio: Float = 0.2f) : IconShape()

        // --- New Shapes ---
        object Squircle : IconShape()
        object Rounded : IconShape() // Soft rounded shape
        object TearDrop : IconShape()
        object Shield : IconShape()
        object Lemon : IconShape()

        /**
         * Uses a custom Drawable to mask the icon.
         */
        data class Custom(val maskDrawable: Drawable) : IconShape()
    }

    // ============================================================================================
    // SOLUTION 1: For ShapeableImageView
    // ============================================================================================

    /**
     * Use this when using ShapeableImageView.
     * The View handles the shape masking.
     */
    fun getIconForShapeable(
        context: Context,
        background: Drawable?,
        foreground: Drawable?
    ): Drawable {
        val (safeBg, safeFg) = getSafeDrawables(background, foreground)
        val iconBitmap = createLegacyIconBitmap(safeBg, safeFg)
        return iconBitmap.toDrawable(context.resources)
    }

    // ============================================================================================
    // SOLUTION 2: For Standard ImageView
    // ============================================================================================

    fun getIcon(
        context: Context,
        background: Drawable?,
        foreground: Drawable?,
        shape: IconShape
    ): Drawable {
        val (safeBg, safeFg) = getSafeDrawables(background, foreground)
        // 1. Handle "System" shape preference (Native behavior)
        if (shape is IconShape.System && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return AdaptiveIconDrawable(safeBg, safeFg)
        }
        // 2. Create the legacy bitmap (zoomed and cropped)
        val contentBitmap = createLegacyIconBitmap(safeBg, safeFg)
        return when (shape) {
            is IconShape.System, is IconShape.Circle -> {
                RoundedBitmapDrawableFactory.create(context.resources, contentBitmap).apply {
                    isCircular = true
                }
            }

            is IconShape.RoundedSquare -> {
                RoundedBitmapDrawableFactory.create(context.resources, contentBitmap).apply {
                    cornerRadius = contentBitmap.width * shape.cornerRadiusRatio
                }
            }

            is IconShape.Custom -> {
                createMaskedDrawable(context, contentBitmap, shape.maskDrawable)
            }

            is IconShape.Squircle, is IconShape.Rounded, is IconShape.Square,
            is IconShape.TearDrop, is IconShape.Shield, is IconShape.Lemon -> {
                val path = getPathForShape(shape, contentBitmap.width, contentBitmap.height)
                createMaskedDrawable(context, contentBitmap, path)
            }
        }
    }

    // ============================================================================================
    // INTERNAL HELPERS
    // ============================================================================================

    private fun getSafeDrawables(bg: Drawable?, fg: Drawable?): Pair<Drawable, Drawable> {
        val safeBg = bg ?: Color.WHITE.toDrawable()
        val safeFg = fg ?: Color.TRANSPARENT.toDrawable()
        return Pair(safeBg, safeFg)
    }

    private fun createLegacyIconBitmap(background: Drawable, foreground: Drawable): Bitmap {
        val fullSize = 432 // 108dp * 4
        val viewportSize = (fullSize / 1.5f).toInt() // 72dp equivalent
        val offset = (fullSize - viewportSize) / 2
        val fullBitmap = createBitmap(fullSize, fullSize)
        val canvas = Canvas(fullBitmap)
        background.setBounds(0, 0, fullSize, fullSize)
        background.draw(canvas)
        foreground.setBounds(0, 0, fullSize, fullSize)
        foreground.draw(canvas)
        return Bitmap.createBitmap(fullBitmap, offset, offset, viewportSize, viewportSize)
    }

    /**
     * Generates a scaled Path for the requested shape.
     * based on https://stackoverflow.com/a/61670885
     */
    private fun getPathForShape(shape: IconShape, width: Int, height: Int): Path {
        val pathData = when (shape) {
            is IconShape.Squircle -> "M 50,0 C 10,0 0,10 0,50 C 0,90 10,100 50,100 C 90,100 100,90 100,50 C 100,10 90,0 50,0 Z"
            is IconShape.Rounded -> "M 50,0 L 70,0 A 30,30,0,0 1 100,30 L 100,70 A 30,30,0,0 1 70,100 L 30,100 A 30,30,0,0 1 0,70 L 0,30 A 30,30,0,0 1 30,0 z"
            is IconShape.TearDrop -> "M 50,0 A 50,50,0,0 1 100,50 L 100,85 A 15,15,0,0 1 85,100 L 50,100 A 50,50,0,0 1 50,0 z"
            is IconShape.Shield -> "m6.6146,13.2292a6.6146,6.6146 0,0 0,6.6146 -6.6146v-5.3645c0,-0.6925 -0.5576,-1.25 -1.2501,-1.25L6.6146,-0 1.2501,-0C0.5576,0 0,0.5575 0,1.25v5.3645A6.6146,6.6146 0,0 0,6.6146 13.2292Z"
            is IconShape.Lemon -> "M1.2501,0C0.5576,0 0,0.5576 0,1.2501L0,6.6146A6.6146,6.6146 135,0 0,6.6146 13.2292L11.9791,13.2292C12.6716,13.2292 13.2292,12.6716 13.2292,11.9791L13.2292,6.6146A6.6146,6.6146 45,0 0,6.6146 0L1.2501,0z"
            else -> "" // Should not happen for handled shapes
        }
        val path = if (shape is IconShape.Square) {
            Path().apply { addRect(0f, 0f, 50f, 50f, Path.Direction.CW) }
        } else {
            PathParser.createPathFromPathData(pathData)
        }
        // Scale the path to fit the destination width/height
        val bounds = RectF()
        path.computeBounds(bounds, true)
        val matrix = Matrix()
        matrix.setRectToRect(
            bounds,
            RectF(0f, 0f, width.toFloat(), height.toFloat()),
            Matrix.ScaleToFit.FILL
        )
        path.transform(matrix)
        return path
    }

    /**
     * Applies a Path mask to the content Bitmap.
     */
    private fun createMaskedDrawable(
        context: Context,
        contentBitmap: Bitmap,
        maskPath: Path
    ): Drawable {
        val width = contentBitmap.width
        val height = contentBitmap.height
        val resultBitmap = createBitmap(width, height)
        val canvas = Canvas(resultBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        // 1. Draw Mask
        canvas.drawPath(maskPath, paint)
        // 2. Draw Content with SRC_IN (Source=Content, Dest=Mask)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(contentBitmap, 0f, 0f, paint)
        return resultBitmap.toDrawable(context.resources)
    }

    /**
     * Applies a specific Drawable mask.
     */
    private fun createMaskedDrawable(
        context: Context,
        contentBitmap: Bitmap,
        maskDrawable: Drawable
    ): Drawable {
        val width = contentBitmap.width
        val height = contentBitmap.height
        val resultBitmap = createBitmap(width, height)
        val canvas = Canvas(resultBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        // 1. Draw Mask
        maskDrawable.setBounds(0, 0, width, height)
        maskDrawable.draw(canvas)
        // 2. Draw Content with SRC_IN
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(contentBitmap, 0f, 0f, paint)
        return resultBitmap.toDrawable(context.resources)
    }
}
