package io.github.cvrunmin.crmkjk

import java.util.*

class CRMKJK{}

    /**
     * source: http://snipplr.com/view/35806/
     */
    fun String.isASCII () : Boolean
    {
        // ASCII encoding replaces non-ascii with question marks, so we use UTF8 to see if multi-byte sequences are there
        return this.toByteArray().size == this.length
    }

    fun String.escapeToUnicode() : String
    {
        var sb = StringBuilder()
        var chars = this.toCharArray()
        for(i in chars.indices)
        {
            var cb = chars[i].toString().toByteArray()
            sb.append("\\u${cb[1]}x${cb[0]}x")
        }
        return sb.toString()
    }

    fun String.trapToUnicode () : String
    {
        var a = this.split( '\\', 'u' )
        var sb = StringBuilder()
        for (c in a)
        {
            var bytes = byteArrayOf(0,0)
            var c1 = c.padStart(4, '0')
            bytes[1] = c1.substring(0,1).toByte(16)
            bytes[0] = c1.substring(2,3).toByte(16)
            sb.append(bytes.toString(Charsets.UTF_8))
        }
        return sb.toString()
    }

//    *
//     * src: http://stackoverflow.com/questions/7343465/compression-decompression-string-with-c-sharp
//     *
//    public static string ZipString (string str)
//    {
//        var bytes = Encoding.UTF8.GetBytes(str);
//
//        using(var msi = new MemoryStream(bytes))
//        using(var mso = new MemoryStream())
//        {
//            using(var gs = new GZipStream(mso, CompressionMode.Compress))
//            {
//                msi.CopyTo(gs);
//            }
//
//            return Convert.ToBase64String(mso.ToArray());
//        }
//    }
//
//    *
//     * src: http://stackoverflow.com/questions/7343465/compression-decompression-string-with-c-sharp
//     *
//    public static string UnzipString (string str)
//    {
//        byte[] bytes = Convert . FromBase64String (str);
//        using(var msi = new MemoryStream(bytes))
//        using(var mso = new MemoryStream())
//        {
//            using(var gs = new GZipStream(msi, CompressionMode.Decompress))
//            {
//                gs.CopyTo(mso);
//            }
//
//            return Encoding.UTF8.GetString(mso.ToArray(), 0, (int) mso . Length);
//        }
//    }

    fun <T : Any>Shuffle (a: Array<T>,b : Array<T>) : Unit
    {
        var r = Random()
        for (i in a.indices.reversed().minus(0))
        {
            var j = r.nextInt(i)
            var k = a [j]
            var k1 = b [j]
            a[j] = a[i - 1]
            b[j] = b[i - 1]
            a[i - 1] = k
            b[i - 1] = k1
        }
    }
    fun String.IndexesOf (c : Char) : List<Int>
    {
        if (this.isBlank()) return emptyList()
        val list = listOf<Int>()
        var index = 0
        while (index != -1)
        {
            index = this.indexOf(c, index)
            if (index == -1) break
            list.plus(index)
        }
        return list
    }
