/**
 * Copyright 2017 Dave Heyborne
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package cs4530.cs4530assignment0

import android.content.Context
import java.io.*
import java.nio.charset.Charset

private const val byteArraySize: Int = 32768

internal fun String.Companion.fromAsset(context: Context, assetPath: String, charset: Charset = Charsets.UTF_8): String? {
    return try {
        BufferedInputStream(context.assets.open(assetPath)).use { bufferedInputStream: BufferedInputStream ->
            String.fromInputStream(bufferedInputStream, charset)
        }
    } catch (exception: IOException) {
        if (BuildConfig.DEBUG) {
            exception.printStackTrace()
        }
        null
    }
}

internal fun String.Companion.fromFile(filePath: String, charset: Charset = Charsets.UTF_8): String? {
    return try {
        BufferedInputStream(File(filePath).inputStream()).use { bufferedInputStream: BufferedInputStream ->
            String.fromInputStream(bufferedInputStream, charset)
        }
    } catch (exception: IOException) {
        if (BuildConfig.DEBUG) {
            exception.printStackTrace()
        }
        null
    }
}

internal fun String.toFile(filePath: String, appendToExistingFile: Boolean = false, charset: Charset = Charsets.UTF_8) {
    try {
        BufferedWriter(OutputStreamWriter(FileOutputStream(filePath, appendToExistingFile), charset)).use { bufferedWriter: BufferedWriter ->
            bufferedWriter.write(this)
        }
    } catch (exception: IOException) {
        if (BuildConfig.DEBUG) {
            exception.printStackTrace()
        }
    }
}

internal fun String.Companion.fromInputStream(inputStream: InputStream, charset: Charset = Charsets.UTF_8): String? {
    return try {
        ByteArrayOutputStream().use { byteArrayOutputStream: ByteArrayOutputStream ->
            val buffer = ByteArray(byteArraySize)
            var readLength: Int = inputStream.read(buffer)
            while (readLength != -1) {
                byteArrayOutputStream.write(buffer, 0, readLength)
                readLength = inputStream.read(buffer)
            }
            byteArrayOutputStream.toString(charset.name())
        }
    } catch (exception: IOException) {
        if (BuildConfig.DEBUG) {
            exception.printStackTrace()
        }
        null
    }
}
