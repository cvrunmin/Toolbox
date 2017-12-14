package io.github.cvrunmin.crmkjk

import android.util.Base64
import android.util.Patterns
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.util.*
import java.util.regex.Pattern
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.collections.HashSet

data class KeyPair<T,V>(val a:T, val b:V)

    val UNICODE = 0b1
    val ENCODE_TEXT_BASE64_ENCODE = 0b10
    var random = Random()
    fun encode(src : String, seed : Int, encoding: Charset = Charsets.UTF_16, state : Int = 0) : String{
        return encode(src, Random(seed.toLong()), encoding, state)
    }
    fun encode(src : String, rand : Random = random, encoding: Charset = Charsets.UTF_16, state : Int = 0) : String{
        var src = src
        var state = state
        if(!src.isASCII() or ((state and  UNICODE) != 0)){
            src = src.escapeToUnicode()
            state = state or UNICODE
        }
        var chars = src.toCharArray()
        var (encoded, origin) = genKeyPair(rand, chars)
        return encode(src,origin, encoded, encoding, state)
    }

    fun genKeyPair(rand : Random, chars : CharArray) : KeyPair<CharArray, CharArray>{
        var set = HashSet<Char>(chars.distinct())
        var encodeRequired = set.size
        var kjk = HashSet<Byte>()
        while (kjk.size < encodeRequired)
            kjk.add((rand.nextInt(96-32) + 32).toByte())
        var encoded = kjk.toByteArray().toString(Charsets.UTF_8).toCharArray()
        var origin = set.toCharArray()
        val pair = shuffle(encoded.toTypedArray(), origin.toTypedArray())
        return KeyPair(pair.a.toCharArray(), pair.b.toCharArray())
    }
    fun genUniversalKeyPair(rand: Random) : KeyPair<CharArray, CharArray>{
        var set = HashSet<Char>()
        for(i in 32..95)
            set.add(i.toChar())
        var encodeRequired = set.size
        var kjk = HashSet<Byte>()
        for(i in 32..95)
            kjk.add((rand.nextInt(96-32) + 32).toByte())
        var encoded = kjk.toByteArray().toString(Charsets.UTF_8).toCharArray()
        var origin = set.toCharArray()
        val pair = shuffle(encoded.toTypedArray(), origin.toTypedArray())
        return KeyPair(pair.a.toCharArray(), pair.b.toCharArray())
    }

    fun encode(semipro:String, origin : CharArray, encoded:CharArray, encoding: Charset, state: Int = 0) : String{
        if(semipro.isBlank())return ""
        var unicode = false
        var etb64e = false
        if ((state and UNICODE) != 0)
        {
            unicode = true
        }
        if ((state and ENCODE_TEXT_BASE64_ENCODE) != 0) etb64e = true

        var enchant = CharArray(semipro.length)

        for (c in encoded.indices)
            for(i in semipro.indexesOf(origin[c]))
            enchant[i] = encoded[c]

        var sb = StringBuilder()
        sb.append(origin).append(encoded)
        var fullencodedre = 0
        var encodeRequired = origin.size
        if(etb64e){
            var tmp = Base64.encodeToString((String.format("%03d", encodeRequired) + "=" + sb.toString()).toByteArray(encoding), Base64.DEFAULT)
            sb.delete(0, sb.length).append(tmp)
            fullencodedre = sb.length
        }
        sb.append(enchant)
        var zip = false
        if(sb.length > 0x80000){ //Too large for data storage
            zip = true
            var zipped = zipString(sb.toString())
            sb.delete(0,sb.length).append(zipped)
        }
        sb.insert(0, '=').insert(0, String.format(if(etb64e) "%05d" else "%03d", if(etb64e) fullencodedre else encodeRequired));
        sb.insert(0, if(zip) "z" else "").insert(0, if(unicode) "u" else "").insert(0, if(etb64e) "b" else "")

        return sb.toString()
    }
    fun decode(src : String, encoding : Charset = Charsets.UTF_16) : String{
        if(src.isNullOrBlank()) return ""
        var src = src
        var match = Pattern.compile("(b)?(u)?(z)?(\\d{3,10}?)(=)?").matcher(src)
        if(!match.find())
            throw UnexpectedCRMKJKEncodeException()
        var encodeTextB64Encoded = match.group(1) != null
        var unicode = match.group(2) != null
        var zip = match.group(3)!=  null
        var encodeRequired : Int
        try
        {
            encodeRequired = match.group(4).toInt()
        }
        catch (e : NumberFormatException)
        {
            throw UnexpectedCRMKJKEncodeException("Unexpected exception in converting", e)
        }
        src = match.replaceFirst("")
        if (zip) src = unzipString(src);
        if (encodeTextB64Encoded)
        {
            var tmp1 = src.substring(0, encodeRequired)
            var a = Base64.decode(tmp1, Base64.DEFAULT)
            try
            {
                tmp1 = a.toString(encoding)
                var regex = Pattern.compile("(\\d{3}?(?==))").matcher(tmp1)
                if (regex.find())
                {
                    src =  tmp1 + src.substring(encodeRequired)
                    src = regex.replaceFirst("")
                    encodeRequired = regex.group(0).toInt()
                }
                regex = null;
            }
            catch (e : Exception)
            {

                throw UnexpectedCRMKJKEncodeException("", e)
            }

        }
        var oe = src.substring(0, encodeRequired * 2).toCharArray()
        var origin = oe.copyOfRange(0, encodeRequired)
        var encode = oe.copyOfRange(encodeRequired, encodeRequired * 2)

        var yummystring = src.substring(encodeRequired * 2);
        var enchant = CharArray(yummystring.length)
        for(i in origin.indices)
        {
            var enuma = yummystring.indexesOf(encode[i])
            for (indices in enuma)
            {
                enchant[indices] = origin[i];
            }
        }
        var result = String(enchant)
        if (unicode) result = result.trapToUnicode();
        return result
    }



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
            sb.append("\\u%02x%02x".format(cb[1],cb[0]))
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

    /*
     * src: http://stackoverflow.com/questions/7343465/compression-decompression-string-with-c-sharp
     */
    fun zipString(str : String) : String
    {
        var bytes = str.toByteArray()

        ByteArrayOutputStream().use {
        GZIPOutputStream(it).use{
            var output = it
            ByteArrayInputStream(bytes).use {
                it.copyTo(output)
            }
        }
            return Base64.encodeToString(it.toByteArray(), Base64.DEFAULT)
        }
    }

    /*
     * src: http://stackoverflow.com/questions/7343465/compression-decompression-string-with-c-sharp
     */
    fun unzipString (str : String) : String{
        var bytes = Base64.decode(str, Base64.DEFAULT)

        ByteArrayOutputStream().use {
            var output = it
            ByteArrayInputStream(bytes).use{
                GZIPInputStream(it).use {
                    it.copyTo(output)
                }
            }
            return it.toString("utf-8")
        }
    }


    fun <T : Any>shuffle (a: Array<T>,b : Array<T>) : KeyPair<Array<T>,Array<T>>
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
        return KeyPair(a,b)
    }
    fun String.indexesOf (c : Char) : List<Int>
    {
        if (this.isBlank()) return emptyList()
        var list = arrayListOf<Int>()
        var index = 0
        while (index != -1)
        {
            index = this.indexOf(c, index)
            if (index == -1) break
            list.add(index)
            index++
        }
        return list
    }
