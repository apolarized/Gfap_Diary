package com.example.diplomatiki.utils

import android.content.Context
import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.diplomatiki.data.Item
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PdfUtils {
    companion object {
        fun createAndSharePdf(context: Context, data: List<Item>) {
            try {
                // Create PDF
                val pdfDocument = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
                val page = pdfDocument.startPage(pageInfo)
                var canvas = page.canvas
                
                // Set up title paint with larger text size
                val titlePaint = android.graphics.Paint().apply {
                    textSize = 18f
                    color = android.graphics.Color.BLACK
                    isFakeBoldText = true // Make the title bold
                }
                
                // Set up header paint for table headers
                val headerPaint = android.graphics.Paint().apply {
                    textSize = 14f
                    color = android.graphics.Color.BLACK
                    isFakeBoldText = true
                }
                
                // Set up cell paint for table cells
                val cellPaint = android.graphics.Paint().apply {
                    textSize = 12f
                    color = android.graphics.Color.BLACK
                }
                
                // Set up line paint for table borders
                val linePaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    strokeWidth = 1f
                    style = android.graphics.Paint.Style.STROKE
                }
                
                // Define table dimensions
                val leftMargin = 50f
                val topMargin = 100f
                val cellPadding = 10f
                val dateWidth = 100f
                val timeWidth = 80f
                val valueWidth = 100f
                val commentWidth = 215f
                val rowHeight = 40f
                
                // Calculate total table width
                val tableWidth = dateWidth + timeWidth + valueWidth + commentWidth
                
                // Add title to PDF
                canvas.drawText("GFAP Protein Diary Report", leftMargin, 50f, titlePaint)
                
                // Add date of report generation
                val currentDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).format(Date())
                canvas.drawText("Report generated: $currentDate", leftMargin, 75f, cellPaint)
                
                // Draw table header row
                var currentY = topMargin
                var currentX = leftMargin
                
                // Draw header cells
                // Date header
                canvas.drawRect(currentX, currentY, currentX + dateWidth, currentY + rowHeight, linePaint)
                canvas.drawText("Date", currentX + cellPadding, currentY + rowHeight - cellPadding, headerPaint)
                currentX += dateWidth
                
                // Time header
                canvas.drawRect(currentX, currentY, currentX + timeWidth, currentY + rowHeight, linePaint)
                canvas.drawText("Time", currentX + cellPadding, currentY + rowHeight - cellPadding, headerPaint)
                currentX += timeWidth
                
                // Value header
                canvas.drawRect(currentX, currentY, currentX + valueWidth, currentY + rowHeight, linePaint)
                canvas.drawText("Value", currentX + cellPadding, currentY + rowHeight - cellPadding, headerPaint)
                currentX += valueWidth
                
                // Comment header
                canvas.drawRect(currentX, currentY, currentX + commentWidth, currentY + rowHeight, linePaint)
                canvas.drawText("Comments", currentX + cellPadding, currentY + rowHeight - cellPadding, headerPaint)
                
                // Move to next row
                currentY += rowHeight
                
                // Draw data rows
                data.forEach { item ->
                    currentX = leftMargin
                    
                    // Split timestamp into date and time
                    val timestamp = item.formattedTimestamp()
                    val date = timestamp.substringBefore(" ")
                    val time = timestamp.substringAfter(" ")
                    
                    // Date cell
                    canvas.drawRect(currentX, currentY, currentX + dateWidth, currentY + rowHeight, linePaint)
                    canvas.drawText(date, currentX + cellPadding, currentY + rowHeight - cellPadding, cellPaint)
                    currentX += dateWidth
                    
                    // Time cell
                    canvas.drawRect(currentX, currentY, currentX + timeWidth, currentY + rowHeight, linePaint)
                    canvas.drawText(time, currentX + cellPadding, currentY + rowHeight - cellPadding, cellPaint)
                    currentX += timeWidth
                    
                    // Value cell
                    canvas.drawRect(currentX, currentY, currentX + valueWidth, currentY + rowHeight, linePaint)
                    canvas.drawText("${item.formatedPrice()} ng/ml", currentX + cellPadding, currentY + rowHeight - cellPadding, cellPaint)
                    currentX += valueWidth
                    
                    // Comment cell
                    canvas.drawRect(currentX, currentY, currentX + commentWidth, currentY + rowHeight, linePaint)
                    if (item.hasComment()) {
                        // Truncate comment if too long for the cell
                        val comment = item.comment
                        val maxChars = 30 // Approximate max chars that fit in the cell
                        val displayComment = if (comment.length > maxChars) 
                            "${comment.substring(0, maxChars)}..." 
                        else 
                            comment
                        canvas.drawText(displayComment, currentX + cellPadding, currentY + rowHeight - cellPadding, cellPaint)
                    }
                    
                    // Move to next row
                    currentY += rowHeight
                    
                    // Check if we need to start a new page
                    if (currentY > 780f) { // Near bottom of page
                        pdfDocument.finishPage(page)
                        val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pdfDocument.pages.size + 1).create()
                        val newPage = pdfDocument.startPage(newPageInfo)
                        canvas = newPage.canvas
                        currentY = topMargin
                        
                        // Redraw header on new page
                        currentX = leftMargin
                        
                        // Date header
                        canvas.drawRect(currentX, currentY, currentX + dateWidth, currentY + rowHeight, linePaint)
                        canvas.drawText("Date", currentX + cellPadding, currentY + rowHeight - cellPadding, headerPaint)
                        currentX += dateWidth
                        
                        // Time header
                        canvas.drawRect(currentX, currentY, currentX + timeWidth, currentY + rowHeight, linePaint)
                        canvas.drawText("Time", currentX + cellPadding, currentY + rowHeight - cellPadding, headerPaint)
                        currentX += timeWidth
                        
                        // Value header
                        canvas.drawRect(currentX, currentY, currentX + valueWidth, currentY + rowHeight, linePaint)
                        canvas.drawText("Value", currentX + cellPadding, currentY + rowHeight - cellPadding, headerPaint)
                        currentX += valueWidth
                        
                        // Comment header
                        canvas.drawRect(currentX, currentY, currentX + commentWidth, currentY + rowHeight, linePaint)
                        canvas.drawText("Comments", currentX + cellPadding, currentY + rowHeight - cellPadding, headerPaint)
                        
                        // Move to next row
                        currentY += rowHeight
                    }
                }

                pdfDocument.finishPage(page)

                // Save PDF
                val fileName = "GFAP_Report_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.pdf"
                val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
                pdfDocument.writeTo(FileOutputStream(file))
                pdfDocument.close()

                // Share PDF
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )

                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                context.startActivity(Intent.createChooser(shareIntent, "Share GFAP Report"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun exportToCsv(context: Context, data: List<Item>) {
            try {
                val fileName = "GFAP_Export_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.csv"
                val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
                
                FileOutputStream(file).use { fos ->
                    // Write CSV header
                    fos.write("Date,Time,GFAP Value (ng/ml),Comments\n".toByteArray())
                    
                    // Write data rows
                    data.forEach { item ->
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy,HH:mm:ss", Locale.US)
                        val dateTime = dateFormat.format(Date(item.timestamp))
                        val line = "$dateTime,${String.format(Locale.US, "%.2f", item.gfapval)},${item.comment}\n"
                        fos.write(line.toByteArray())
                    }
                }

                // Share the CSV file
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )

                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                context.startActivity(Intent.createChooser(shareIntent, "Share GFAP CSV Data"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private val paint = android.graphics.Paint().apply {
            textSize = 12f
            color = android.graphics.Color.BLACK
        }
    }
} 